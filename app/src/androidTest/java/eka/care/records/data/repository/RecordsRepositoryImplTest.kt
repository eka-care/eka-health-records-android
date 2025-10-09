package eka.care.records.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.eka.networking.token.TokenStorage
import eka.care.records.client.model.SortOrder
import eka.care.records.client.utils.Document
import eka.care.records.client.utils.DocumentConfiguration
import eka.care.records.data.dao.EncounterRecordDao
import eka.care.records.data.dao.RecordsDao
import eka.care.records.data.db.RecordsDatabase
import eka.care.records.data.entity.CaseStatus
import eka.care.records.data.entity.CaseUiState
import eka.care.records.data.entity.RecordEntity
import eka.care.records.data.entity.RecordStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecordsRepositoryImplTest {
    private lateinit var db: RecordsDatabase
    private lateinit var recordsDao: RecordsDao
    private lateinit var encounterDao: EncounterRecordDao
    private lateinit var context: Context

    @Before
    fun setup() {
        Document.init(
            DocumentConfiguration(
                appId = "testApp",
                baseUrl = "http://localhost:8080",
                isDebugApp = true,
                headers = mapOf(
                    "Content-Type" to "application/json",
                    "Accept" to "application/json"
                ),
                tokenStorage = object : TokenStorage {
                    override fun getAccessToken(): String {
                        return "testAccessToken"
                    }

                    override fun getRefreshToken(): String {
                        return "testRefreshToken"
                    }

                    override fun saveTokens(
                        accessToken: String,
                        refreshToken: String
                    ) {
                    }

                    override fun onSessionExpired() {}
                },
                appVersionName = "1.0",
                appVersionCode = 1,
                provider = "eka.care.records.test"
            )
        )
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(
            context,
            RecordsDatabase::class.java
        )
            .allowMainThreadQueries() // for testing only
            .build()

        recordsDao = db.recordsDao()
        encounterDao = db.encounterDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun now() = System.currentTimeMillis()

    @Test
    fun createRecordsInsertsAllRecords() = runBlocking {
        val repo = RecordsRepositoryImpl(context)
        val records = listOf(
            RecordEntity(
                documentId = "rec1",
                businessId = "biz1",
                ownerId = "own1",
                createdAt = 1L,
                updatedAt = 1L
            ),
            RecordEntity(
                documentId = "rec2",
                businessId = "biz1",
                ownerId = "own1",
                createdAt = 2L,
                updatedAt = 2L
            )
        )
        repo.createRecords(records)
        val fetched1 = repo.getRecordById("rec1")
        val fetched2 = repo.getRecordById("rec2")
        assertNotNull(fetched1)
        assertNotNull(fetched2)
    }

    @Test
    fun createRecordReturnsNullIfFilesEmpty() = runBlocking {
        val repo = RecordsRepositoryImpl(context)
        val result = repo.createRecord(
            files = emptyList(),
            businessId = "biz2",
            ownerId = "own2",
            caseId = null,
            documentType = "typeA",
            documentDate = null,
            tags = emptyList()
        )
        assertNull(result)
    }

    @Test
    fun getRecordByIdReturnsNullForNonExistentRecord() = runBlocking {
        val repo = RecordsRepositoryImpl(context)
        val result = repo.getRecordById("nonexistent")
        assertNull(result)
    }

    @Test
    fun updateRecordWithNullFieldsReturnsNull() = runBlocking {
        val repo = RecordsRepositoryImpl(context)
        val result = repo.updateRecord(
            id = "recX",
            caseId = null,
            documentDate = null,
            documentType = null
        )
        assertNull(result)
    }

    @Test
    fun deleteRecordsMarksRecordsAsDeleted() = runBlocking {
        val repo = RecordsRepositoryImpl(context)
        val record = RecordEntity(
            documentId = "recDel",
            businessId = "biz3",
            ownerId = "own3",
            createdAt = 1L,
            updatedAt = 1L
        )
        repo.createRecords(listOf(record))
        repo.deleteRecords(listOf("recDel"))
        val deleted = repo.getRecordById("recDel")
        assertEquals(true, deleted?.status == RecordStatus.ARCHIVED)
    }

    @Test
    fun getLatestRecordUpdatedAtReturnsNullIfNoRecords() = runBlocking {
        val repo = RecordsRepositoryImpl(context)
        val latest = repo.getLatestRecordUpdatedAt("bizX", "ownX")
        assertNull(latest)
    }

    @Test
    fun getCaseByCaseIdReturnsNullIfNotFound() = runBlocking {
        val repo = RecordsRepositoryImpl(context)
        val result = repo.getCaseByCaseId("missingCase")
        assertNull(result)
    }

    @Test
    fun createCaseCreatesAndReturnsId() = runBlocking {
        val repo = RecordsRepositoryImpl(context)
        val id = repo.createCase(
            caseId = null,
            businessId = "biz4",
            ownerId = "own4",
            name = "CaseName",
            type = "TypeA",
            createdAt = now(),
            updatedAt = now(),
            status = CaseStatus.CREATED_LOCALLY,
            uiStatus = CaseUiState.WAITING_TO_UPLOAD,
        )
        assertNotNull(id)
        val case = repo.getCaseByCaseId(id)
        assertNotNull(case)
        assertEquals("CaseName", case?.encounter?.name)
    }

    @Test
    fun updateCaseReturnsNullIfCaseNotFound() = runBlocking {
        val repo = RecordsRepositoryImpl(context)
        val result =
            repo.updateCase("noCase", "newName", "newType", CaseStatus.NONE, CaseUiState.NONE)
        assertNull(result)
    }

    @Test
    fun assignRecordToCaseCreatesRelation() = runBlocking {
        val repo = RecordsRepositoryImpl(context)
        val caseId = repo.createCase(
            caseId = null,
            businessId = "biz5",
            ownerId = "own5",
            name = "CaseRel",
            type = "TypeRel",
            createdAt = now(),
            updatedAt = now(),
            status = CaseStatus.CREATED_LOCALLY,
            uiStatus = CaseUiState.WAITING_TO_UPLOAD,
        )
        val record = RecordEntity(
            documentId = "recRel",
            businessId = "biz5",
            ownerId = "own5",
            createdAt = 1L,
            updatedAt = 1L
        )
        repo.createRecords(listOf(record))
        repo.assignRecordToCase(caseId, "recRel")
        val caseModel = repo.getCaseWithRecords(caseId)
        assertTrue(caseModel?.records?.any { it.id == "recRel" } == true)
    }

    @Test
    fun readRecordsFilterByTags() = runBlocking {
        val repo = RecordsRepositoryImpl(context)
        val record1 = RecordEntity(
            documentId = "r1",
            businessId = "b1",
            ownerId = "o1",
            createdAt = 1L,
            updatedAt = 1L,
            documentType = "ot"
        )
        val record2 = RecordEntity(
            documentId = "r2",
            businessId = "b1",
            ownerId = "o1",
            createdAt = 2L,
            updatedAt = 2L,
            documentType = "ot"
        )
        repo.createRecords(listOf(record1, record2))

        repo.addTag("r1", "finance")
        repo.addTag("r1", "urgent")
        repo.addTag("r1", "hr")
        repo.addTag("r2", "hr")

        val tagsToFilter = listOf("finance", "hr", "urgent")
        val recordsFlow = repo.readRecords(
            businessId = "b1",
            ownerIds = listOf("o1"),
            caseId = null,
            includeDeleted = false,
            documentType = "ot",
            sortOrder = SortOrder.CREATED_AT_ASC,
            tags = tagsToFilter
        )

        val results = recordsFlow.first()

        assertEquals(2, results.size)
        assertEquals("r1", results[0].id)
    }
}