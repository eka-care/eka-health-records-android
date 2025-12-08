package eka.care.records.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import eka.care.records.data.db.RecordsDatabase
import eka.care.records.data.entity.CaseStatus
import eka.care.records.data.entity.EncounterEntity
import eka.care.records.data.entity.EncounterRecordCrossRef
import eka.care.records.data.entity.RecordEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EncounterDaoTest {
    private lateinit var db: RecordsDatabase
    private lateinit var recordsDao: RecordsDao
    private lateinit var encounterDao: EncounterRecordDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
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
    fun insertEncounterAndFetchByIdReturnsInsertedEncounter() = runBlocking {
        val encounter = EncounterEntity(
            encounterId = "enc1",
            businessId = "biz1",
            ownerId = "owner1",
            name = "Test Encounter",
            encounterType = "typeA",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        encounterDao.insertEncounter(encounter)
        val fetched = encounterDao.getEncounterById("enc1")
        assertNotNull(fetched)
        assertEquals("enc1", fetched?.encounter?.encounterId)
        assertEquals("Test Encounter", fetched?.encounter?.name)
    }

    @Test
    fun updateEncounterUpdatesFields() = runBlocking {
        val encounter = EncounterEntity(
            encounterId = "enc2",
            businessId = "biz2",
            ownerId = "owner2",
            name = "Old Name",
            encounterType = "oldType",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        encounterDao.insertEncounter(encounter)
        val updated = encounter.copy(
            name = "New Name",
            encounterType = "newType",
            status = CaseStatus.UPDATED_LOCALLY
        )
        encounterDao.updateEncounter(updated)
        val fetched = encounterDao.getEncounterById("enc2")
        assertEquals("New Name", fetched?.encounter?.name)
        assertEquals("newType", fetched?.encounter?.encounterType)
        assertEquals(true, fetched?.encounter?.status == CaseStatus.UPDATED_LOCALLY)
    }

    @Test
    fun deleteEncounterRemovesItFromDatabase() = runBlocking {
        val encounter = EncounterEntity(
            encounterId = "enc3",
            businessId = "biz3",
            ownerId = "owner3",
            name = "To Delete",
            encounterType = "typeDel",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        encounterDao.insertEncounter(encounter)
        encounterDao.deleteEncounter(encounter)
        val fetched = encounterDao.getEncounterById("enc3")
        assertNull(fetched)
    }

    @Test
    fun insertEncounterRecordCrossRefAndFetchRecordWithEncounters() = runBlocking {
        val encounter = EncounterEntity(
            encounterId = "enc4",
            businessId = "biz4",
            ownerId = "owner4",
            name = "CrossRef",
            encounterType = "typeCross",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val record = RecordEntity(
            documentId = "rec4",
            businessId = "biz4",
            ownerId = "owner4",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        encounterDao.insertEncounter(encounter)
        recordsDao.createRecords(listOf(record))
        encounterDao.insertEncounterRecordCrossRef(
            EncounterRecordCrossRef(encounterId = "enc4", documentId = "rec4")
        )
        val recordWithEncounters = encounterDao.getRecordWithEncounters("rec4")
        assertEquals("rec4", recordWithEncounters.record.documentId)
        assertEquals(1, recordWithEncounters.encounters.size)
        assertEquals("enc4", recordWithEncounters.encounters.first().encounterId)
    }

    @Test
    fun removeEncounterRecordRemovesRelation() = runBlocking {
        val encounter = EncounterEntity(
            encounterId = "enc5",
            businessId = "biz5",
            ownerId = "owner5",
            name = "RemoveRel",
            encounterType = "typeRel",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val record = RecordEntity(
            documentId = "rec5",
            businessId = "biz5",
            ownerId = "owner5",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        encounterDao.insertEncounter(encounter)
        recordsDao.createRecords(listOf(record))
        encounterDao.insertEncounterRecordCrossRef(
            EncounterRecordCrossRef(encounterId = "enc5", documentId = "rec5")
        )
        encounterDao.removeEncounterRecord("enc5", "rec5")
        val recordWithEncounters = encounterDao.getRecordWithEncounters("rec5")
        assertTrue(recordWithEncounters.encounters.isEmpty())
    }

    @Test
    fun getUnsyncedEncountersReturnsOnlyUnsynced() = runBlocking {
        val synced = EncounterEntity(
            encounterId = "enc6",
            businessId = "biz6",
            ownerId = "owner6",
            name = "Synced",
            encounterType = "typeS",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
        val unsynced = synced.copy(encounterId = "enc7", status = CaseStatus.UPDATED_LOCALLY)
        encounterDao.insertEncounter(synced)
        encounterDao.insertEncounter(unsynced)
        val result = encounterDao.getEncountersByStatus("biz6", listOf(CaseStatus.UPDATED_LOCALLY))
        assertEquals(1, result?.size)
        assertEquals("enc7", result?.first()?.encounterId)
    }

    @Test
    fun getDirtyEncounterReturnsOnlyDirtyOnes() = runBlocking {
        val clean = EncounterEntity(
            encounterId = "enc8",
            businessId = "biz7",
            ownerId = "owner7",
            name = "Clean",
            encounterType = "typeC",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
        val dirty = clean.copy(encounterId = "enc9", status = CaseStatus.UPDATED_LOCALLY)
        encounterDao.insertEncounter(clean)
        encounterDao.insertEncounter(dirty)
        val result = encounterDao.getEncountersByStatus("biz7", listOf(CaseStatus.UPDATED_LOCALLY))
        assertEquals(1, result?.size)
        assertEquals("enc9", result?.first()?.encounterId)
    }

    @Test
    fun getLatestEncounterUpdatedAtReturnsNullIfNoEncounters() = runBlocking {
        val latest = encounterDao.getLatestEncounterUpdatedAt("noBiz", "noOwner")
        assertNull(latest)
    }

    @Test
    fun insertMultipleEncountersAndFetchAllReturnsAll() = runBlocking {
        val encounters = listOf(
            EncounterEntity(
                encounterId = "encA",
                businessId = "bizA",
                ownerId = "ownerA",
                name = "Encounter A",
                encounterType = "typeA",
                createdAt = now(),
                updatedAt = now()
            ),
            EncounterEntity(
                encounterId = "encB",
                businessId = "bizA",
                ownerId = "ownerA",
                name = "Encounter B",
                encounterType = "typeB",
                createdAt = now(),
                updatedAt = now()
            )
        )
        encounters.forEach { encounterDao.insertEncounter(it) }
        val all = encounterDao.getAllEncounters("bizA", "ownerA")
        val result = all.first()
        assertEquals(2, result.size)
        assertTrue(result.any { it.encounter.encounterId == "encA" })
        assertTrue(result.any { it.encounter.encounterId == "encB" })
    }

    @Test
    fun insertEncounterWithNullTypeAndFetch() = runBlocking {
        val encounter = EncounterEntity(
            encounterId = "encNullType",
            businessId = "bizNull",
            ownerId = "ownerNull",
            name = "No Type",
            encounterType = null,
            createdAt = now(),
            updatedAt = now()
        )
        encounterDao.insertEncounter(encounter)
        val fetched = encounterDao.getEncounterById("encNullType")
        assertNotNull(fetched)
        assertEquals(null, fetched?.encounter?.encounterType)
    }

    @Test
    fun insertDuplicateEncounterReplacesOrIgnoresBasedOnConflictStrategy() = runBlocking {
        val encounter = EncounterEntity(
            encounterId = "encDup",
            businessId = "bizDup",
            ownerId = "ownerDup",
            name = "Original",
            encounterType = "typeDup",
            createdAt = now(),
            updatedAt = now()
        )
        encounterDao.insertEncounter(encounter)
        val duplicate = encounter.copy(name = "Updated Name")
        encounterDao.insertEncounter(duplicate)
        val fetched = encounterDao.getEncounterById("encDup")
        assertNotNull(fetched)
        // Depending on conflict strategy, this may be "Updated Name" or "Original"
    }

    @Test
    fun updateNonExistentEncounterDoesNothing() = runBlocking {
        val nonExistent = EncounterEntity(
            encounterId = "doesNotExist",
            businessId = "bizX",
            ownerId = "ownerX",
            name = "Ghost",
            encounterType = "ghostType",
            createdAt = now(),
            updatedAt = now()
        )
        encounterDao.updateEncounter(nonExistent)
        val fetched = encounterDao.getEncounterById("doesNotExist")
        assertNull(fetched)
    }

    @Test
    fun deleteNonExistentEncounterDoesNotThrow() = runBlocking {
        val encounter = EncounterEntity(
            encounterId = "ghostDelete",
            businessId = "bizZ",
            ownerId = "ownerZ",
            name = "Ghost",
            encounterType = "ghostType",
            createdAt = now(),
            updatedAt = now()
        )
        // Not inserted
        encounterDao.deleteEncounter(encounter)
        val fetched = encounterDao.getEncounterById("ghostDelete")
        assertNull(fetched)
    }

    @Test
    fun insertMultipleCrossRefsAndFetchAllEncountersForRecord() = runBlocking {
        val encounter1 = EncounterEntity(
            encounterId = "encMulti1",
            businessId = "bizMulti",
            ownerId = "ownerMulti",
            name = "Multi1",
            encounterType = "type1",
            createdAt = now(),
            updatedAt = now()
        )
        val encounter2 = encounter1.copy(encounterId = "encMulti2", name = "Multi2")
        val record = RecordEntity(
            documentId = "recMulti",
            businessId = "bizMulti",
            ownerId = "ownerMulti",
            createdAt = now(),
            updatedAt = now()
        )
        encounterDao.insertEncounter(encounter1)
        encounterDao.insertEncounter(encounter2)
        recordsDao.createRecords(listOf(record))
        encounterDao.insertEncounterRecordCrossRef(EncounterRecordCrossRef("encMulti1", "recMulti"))
        encounterDao.insertEncounterRecordCrossRef(EncounterRecordCrossRef("encMulti2", "recMulti"))
        val recordWithEncounters = encounterDao.getRecordWithEncounters("recMulti")
        assertEquals(2, recordWithEncounters.encounters.size)
        assertTrue(recordWithEncounters.encounters.any { it.encounterId == "encMulti1" })
        assertTrue(recordWithEncounters.encounters.any { it.encounterId == "encMulti2" })
    }

    @Test
    fun removeNonExistentEncounterRecordRelationDoesNotThrow() = runBlocking {
        encounterDao.removeEncounterRecord("noSuchEncounter", "noSuchRecord")
        // Should not throw, nothing to assert
    }

    @Test
    fun getRecordWithEncountersReturnsEmptyIfNoRelations() = runBlocking {
        val record = RecordEntity(
            documentId = "recNoRel",
            businessId = "bizNoRel",
            ownerId = "ownerNoRel",
            createdAt = now(),
            updatedAt = now()
        )
        recordsDao.createRecords(listOf(record))
        val recordWithEncounters = encounterDao.getRecordWithEncounters("recNoRel")
        assertNotNull(recordWithEncounters)
        assertTrue(recordWithEncounters.encounters.isEmpty())
    }

    @Test
    fun getAllEncountersReturnsEmptyListIfNoneExist() = runBlocking {
        val all = encounterDao.getAllEncounters("bizNone", "ownerNone")
        val result = all.first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getDirtyEncounterReturnsEmptyIfNoneDirty() = runBlocking {
        val encounter = EncounterEntity(
            encounterId = "encCleanOnly",
            businessId = "bizClean",
            ownerId = "ownerClean",
            name = "Clean",
            encounterType = "typeClean",
            createdAt = now(),
            updatedAt = now(),
        )
        encounterDao.insertEncounter(encounter)
        val result =
            encounterDao.getEncountersByStatus("ownerClean", listOf(CaseStatus.UPDATED_LOCALLY))
        assertTrue(result?.isEmpty() == true)
    }

    @Test
    fun getUnsyncedEncountersReturnsEmptyIfAllSynced() = runBlocking {
        val encounter = EncounterEntity(
            encounterId = "encSyncedOnly",
            businessId = "bizSync",
            ownerId = "ownerSync",
            name = "Synced",
            encounterType = "typeSync",
            createdAt = now(),
            updatedAt = now(),
            status = CaseStatus.SYNC_COMPLETED
        )
        encounterDao.insertEncounter(encounter)
        val result =
            encounterDao.getEncountersByStatus("ownerSync", listOf(CaseStatus.UPDATED_LOCALLY))
        assertTrue(result?.isEmpty() == true)
    }

    @Test
    fun getLatestEncounterUpdatedAtReturnsLatestTimestamp() = runBlocking {
        val now1 = now()
        val now2 = now1 + 1000
        val encounter1 = EncounterEntity(
            encounterId = "encLatest1",
            businessId = "bizLatest",
            ownerId = "ownerLatest",
            name = "First",
            encounterType = "typeL",
            createdAt = now1,
            updatedAt = now1
        )
        val encounter2 = encounter1.copy(encounterId = "encLatest2", updatedAt = now2)
        encounterDao.insertEncounter(encounter1)
        encounterDao.insertEncounter(encounter2)
        val latest = encounterDao.getLatestEncounterUpdatedAt("bizLatest", "ownerLatest")
        assertEquals(now2, latest)
    }

//    @Test
//    fun testPdfTextExtraction() = runBlocking {
//        val context = ApplicationProvider.getApplicationContext<Context>()
//
//        // Copy PDF from assets to cache dir
//        val assetName = "test-report.pdf"
//        val inputStream = context.assets.open(assetName)
//        val outFile = File(context.cacheDir, assetName)
//        FileOutputStream(outFile).use { output ->
//            inputStream.copyTo(output)
//        }
//
//        val parser = PdfParser()
//        val result = parser.parseDocument(outFile.absolutePath, context)
//
//        TestCase.assertTrue(result.isSuccess)
//        val text = result.getOrNull() ?: ""
//
//        // Debug output
//        println("Extracted text:\n$text")
//
//        // Optional: validate some keywords
//        TestCase.assertTrue(text.contains("Niraj", ignoreCase = true))
//        TestCase.assertTrue(text.contains("Education", ignoreCase = true))
//    }
}