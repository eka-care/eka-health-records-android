package eka.care.records.data.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import eka.care.records.data.db.RecordsDatabase
import eka.care.records.data.entity.FileEntity
import eka.care.records.data.entity.RecordEntity
import eka.care.records.data.entity.RecordStatus
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RecordsDaoTest {
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
    fun createRecordsAndFetchIt() = runBlocking {
        val record = RecordEntity(
            documentId = "rec1",
            businessId = "eka-doctor",
            ownerId = "owner1",
            createdAt = now(),
            updatedAt = now()
        )

        recordsDao.createRecords(listOf(record))
        val fetched = recordsDao.getRecordById("rec1")
        assertNotNull(fetched)
        assertEquals("rec1", fetched?.documentId)
    }

    @Test
    fun createRecordWithFilesAndFetchIt() = runBlocking {
        val record = RecordEntity(
            documentId = "rec1",
            businessId = "doc1",
            ownerId = "owner1",
            createdAt = now(),
            updatedAt = now()
        )
        val files = listOf(
            FileEntity(documentId = "rec1", filePath = "/tmp/file1", fileType = "prescription"),
            FileEntity(documentId = "rec1", filePath = "/tmp/file2", fileType = "invoice")
        )

        recordsDao.insertRecordWithFiles(record, files)
        val fetchedFiles = recordsDao.getRecordFile("rec1")
        assertEquals(2, fetchedFiles?.size)
    }

    @Test
    fun createMultipleRecordsAndFetchAll() = runBlocking {
        val records = listOf(
            RecordEntity(
                documentId = "recA",
                businessId = "biz1",
                ownerId = "ownerA",
                createdAt = now(),
                updatedAt = now()
            ),
            RecordEntity(
                documentId = "recB",
                businessId = "biz1",
                ownerId = "ownerB",
                createdAt = now(),
                updatedAt = now()
            )
        )
        recordsDao.createRecords(records)
        val fetchedA = recordsDao.getRecordById("recA")
        val fetchedB = recordsDao.getRecordById("recB")
        assertNotNull(fetchedA)
        assertNotNull(fetchedB)
        assertEquals("recA", fetchedA?.documentId)
        assertEquals("recB", fetchedB?.documentId)
    }

    @Test
    fun updateRecordsUpdatesFields() = runBlocking {
        val record = RecordEntity(
            documentId = "recUpdate",
            businessId = "biz2",
            ownerId = "owner2",
            createdAt = now(),
            updatedAt = now()
        )
        recordsDao.createRecords(listOf(record))
        val updated =
            record.copy(documentType = "updatedType", status = RecordStatus.UPDATED_LOCALLY)
        recordsDao.updateRecord(updated)
        val fetched = recordsDao.getRecordById("recUpdate")
        assertEquals("updatedType", fetched?.documentType)
        assertEquals(true, fetched?.status == RecordStatus.UPDATED_LOCALLY)
    }

    @Test
    fun deleteRecordRemovesItFromDatabase() = runBlocking {
        val record = RecordEntity(
            documentId = "recDelete",
            businessId = "biz3",
            ownerId = "owner3",
            createdAt = now(),
            updatedAt = now()
        )
        recordsDao.createRecords(listOf(record))
        val fetched = recordsDao.getRecordById("recDelete")
        assertNotNull(fetched)
        recordsDao.deleteRecord(record)
        val afterDelete = recordsDao.getRecordById("recDelete")
        assertEquals(null, afterDelete)
    }

    @Test
    fun getDirtyRecordsReturnsOnlyDirtyOnes() = runBlocking {
        val clean = RecordEntity(
            documentId = "recClean",
            businessId = "biz4",
            ownerId = "owner4",
            createdAt = now(),
            updatedAt = now()
        )
        val dirty = clean.copy(documentId = "recDirty", status = RecordStatus.UPDATED_LOCALLY)
        recordsDao.createRecords(listOf(clean, dirty))
        val dirtyRecords =
            recordsDao.getRecordsByStatus("biz4", listOf(RecordStatus.UPDATED_LOCALLY))
        assertEquals(1, dirtyRecords?.size)
        assertEquals("recDirty", dirtyRecords?.first()?.documentId)
    }

    @Test
    fun getDeletedRecordsReturnsOnlyArchivedOnes() = runBlocking {
        val active = RecordEntity(
            documentId = "recActive",
            businessId = "biz5",
            ownerId = "owner5",
            createdAt = now(),
            updatedAt = now()
        )
        val archived = active.copy(documentId = "recArchived", status = RecordStatus.ARCHIVED)
        recordsDao.createRecords(listOf(active, archived))
        val deletedRecords = recordsDao.getRecordsByStatus("biz5", listOf(RecordStatus.ARCHIVED))
        assertEquals(1, deletedRecords?.size)
        assertEquals("recArchived", deletedRecords?.first()?.documentId)
    }

    @Test
    fun insertRecordFileAndFetchIt() = runBlocking {
        val record = RecordEntity(
            documentId = "recFile",
            businessId = "biz6",
            ownerId = "owner6",
            createdAt = now(),
            updatedAt = now()
        )
        recordsDao.createRecords(listOf(record))
        val file = FileEntity(documentId = "recFile", filePath = "/tmp/fileX", fileType = "typeX")
        recordsDao.insertRecordFile(file)
        val files = recordsDao.getRecordFile("recFile")
        assertEquals(1, files?.size)
        assertEquals("/tmp/fileX", files?.first()?.filePath)
    }

    @Test
    fun getLatestRecordUpdatedAtReturnsNullIfNoRecords() = runBlocking {
        val latest = recordsDao.getLatestRecordUpdatedAt("nonexistent", "nobody")
        assertEquals(null, latest)
    }

    @Test
    fun updateNonExistentRecordDoesNothing() = runBlocking {
        val nonExistent = RecordEntity(
            documentId = "doesNotExist",
            businessId = "bizX",
            ownerId = "ownerX",
            createdAt = now(),
            updatedAt = now()
        )
        recordsDao.updateRecord(nonExistent)
        val fetched = recordsDao.getRecordById("doesNotExist")
        assertEquals(null, fetched)
    }

    @Test
    fun insertDuplicateRecordReplacesOrIgnoresBasedOnConflictStrategy() = runBlocking {
        val record = RecordEntity(
            documentId = "dupRec",
            businessId = "bizY",
            ownerId = "ownerY",
            createdAt = now(),
            updatedAt = now()
        )
        recordsDao.createRecords(listOf(record))
        val duplicate = record.copy(documentType = "newType")
        recordsDao.createRecords(listOf(duplicate))
        val fetched = recordsDao.getRecordById("dupRec")
        // Depending on conflict strategy, this may be "newType" or original
        assertNotNull(fetched)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertFileWithoutRecordThrowsConstraintException() = runBlocking {
        val file = FileEntity(
            documentId = "noRecord",
            filePath = "/tmp/ghost",
            fileType = "ghost"
        )
        recordsDao.insertRecordFile(file)
        val fetched = recordsDao.getRecordFile("noRecord")
        assertNull(fetched)
    }

    @Test
    fun getRecordByNonExistentIdReturnsNull() = runBlocking {
        val fetched = recordsDao.getRecordById("missingId")
        assertEquals(null, fetched)
    }

    @Test
    fun deleteNonExistentRecordDoesNotThrow() = runBlocking {
        val record = RecordEntity(
            documentId = "ghostDelete",
            businessId = "bizZ",
            ownerId = "ownerZ",
            createdAt = now(),
            updatedAt = now()
        )
        // Not inserted
        recordsDao.deleteRecord(record)
        val fetched = recordsDao.getRecordById("ghostDelete")
        assertEquals(null, fetched)
    }

    @Test
    fun insertMultipleFilesAndFetchAll() = runBlocking {
        val record = RecordEntity(
            documentId = "multiFileRec",
            businessId = "bizM",
            ownerId = "ownerM",
            createdAt = now(),
            updatedAt = now()
        )
        recordsDao.createRecords(listOf(record))
        val files = listOf(
            FileEntity(documentId = "multiFileRec", filePath = "/tmp/f1", fileType = "t1"),
            FileEntity(documentId = "multiFileRec", filePath = "/tmp/f2", fileType = "t2"),
            FileEntity(documentId = "multiFileRec", filePath = "/tmp/f3", fileType = "t3")
        )
        files.forEach { recordsDao.insertRecordFile(it) }
        val fetched = recordsDao.getRecordFile("multiFileRec")
        assertEquals(3, fetched?.size)
    }

    @Test
    fun updateRecordSetIsDeletedAndVerifyInDeletedRecords() = runBlocking {
        val record = RecordEntity(
            documentId = "toBeDeleted",
            businessId = "bizD",
            ownerId = "ownerD",
            createdAt = now(),
            updatedAt = now()
        )
        recordsDao.createRecords(listOf(record))
        val updated = record.copy(status = RecordStatus.ARCHIVED)
        recordsDao.updateRecord(updated)
        val deleted = recordsDao.getRecordsByStatus("bizD", listOf(RecordStatus.ARCHIVED))
        assertEquals(1, deleted?.size)
        assertEquals("toBeDeleted", deleted?.first()?.documentId)
    }

    @Test
    fun createRecordWithAllOptionalFieldsSet() = runBlocking {
        val record = RecordEntity(
            documentId = "fullRec",
            businessId = "bizF",
            ownerId = "ownerF",
            thumbnail = "thumb.png",
            createdAt = now(),
            updatedAt = now(),
            documentDate = now(),
            documentType = "typeF",
            documentHash = "hashF",
            source = "sourceF",
            status = RecordStatus.UPDATED_LOCALLY,
            isSmart = true,
            smartReport = "{\"field\":\"value\"}"
        )
        recordsDao.createRecords(listOf(record))
        val fetched = recordsDao.getRecordById("fullRec")
        assertNotNull(fetched)
        assertEquals("thumb.png", fetched?.thumbnail)
        assertEquals("typeF", fetched?.documentType)
        assertEquals(true, fetched?.status == RecordStatus.UPDATED_LOCALLY)
        assertEquals(true, fetched?.isSmart)
        assertEquals("{\"field\":\"value\"}", fetched?.smartReport)
    }

    // Tests for getFilesToDeleteByMaxSize SQL function
    @Test
    fun testGetFilesToDeleteByMaxSize_emptyDatabase_returnsEmpty() = runBlocking {
        val maxSize = 1000L
        val result = recordsDao.getFilesToDeleteByMaxSize(maxSize)
        assertEquals(0, result.size)
    }

    @Test
    fun testGetFilesToDeleteByMaxSize_singleDocumentUnderLimit_returnsEmpty() = runBlocking {
        val record = RecordEntity(
            documentId = "doc1",
            businessId = "biz1",
            ownerId = "owner1",
            createdAt = now(),
            updatedAt = now()
        )
        recordsDao.createRecords(listOf(record))

        val file1 = FileEntity(
            documentId = "doc1",
            filePath = "/tmp/file1",
            fileType = "pdf",
            lastUsed = 1000L,
            sizeBytes = 100L
        )
        recordsDao.insertRecordFile(file1)

        val maxSize = 1000L
        val result = recordsDao.getFilesToDeleteByMaxSize(maxSize)

        assertEquals(0, result.size)
    }

    @Test
    fun testGetFilesToDeleteByMaxSize_singleDocumentOverLimit_deletesIt() = runBlocking {
        val record = RecordEntity(
            documentId = "doc1",
            businessId = "biz1",
            ownerId = "owner1",
            createdAt = now(),
            updatedAt = now()
        )
        recordsDao.createRecords(listOf(record))

        val file1 = FileEntity(
            documentId = "doc1",
            filePath = "/tmp/file1",
            fileType = "pdf",
            lastUsed = 1000L,
            sizeBytes = 1500L
        )
        recordsDao.insertRecordFile(file1)

        val maxSize = 1000L
        val result = recordsDao.getFilesToDeleteByMaxSize(maxSize)

        // When a single document exceeds maxSize, it still gets deleted
        // User confirmed: SQL does not keep the last document if it exceeds maxSize
        assertEquals(1, result.size)
        assertEquals("doc1", result[0].documentId)
    }

    @Test
    fun testGetFilesToDeleteByMaxSize_multipleDocuments_deletesOldest() = runBlocking {
        val records = listOf(
            RecordEntity(
                documentId = "doc1",
                businessId = "biz1",
                ownerId = "owner1",
                createdAt = now(),
                updatedAt = now()
            ),
            RecordEntity(
                documentId = "doc2",
                businessId = "biz1",
                ownerId = "owner1",
                createdAt = now(),
                updatedAt = now()
            ),
            RecordEntity(
                documentId = "doc3",
                businessId = "biz1",
                ownerId = "owner1",
                createdAt = now(),
                updatedAt = now()
            )
        )
        recordsDao.createRecords(records)

        // doc1: oldest (1000L), size 100
        val file1 = FileEntity(
            documentId = "doc1",
            filePath = "/tmp/file1",
            fileType = "pdf",
            lastUsed = 1000L,
            sizeBytes = 100L
        )
        // doc2: middle (2000L), size 100
        val file2 = FileEntity(
            documentId = "doc2",
            filePath = "/tmp/file2",
            fileType = "pdf",
            lastUsed = 2000L,
            sizeBytes = 100L
        )
        // doc3: newest (3000L), size 100
        val file3 = FileEntity(
            documentId = "doc3",
            filePath = "/tmp/file3",
            fileType = "pdf",
            lastUsed = 3000L,
            sizeBytes = 100L
        )

        recordsDao.insertRecordFile(file1)
        recordsDao.insertRecordFile(file2)
        recordsDao.insertRecordFile(file3)

        val maxSize = 200L // Can keep 2 documents
        val result = recordsDao.getFilesToDeleteByMaxSize(maxSize)

        assertEquals(1, result.size)
        assertEquals("doc1", result[0].documentId)
    }

    @Test
    fun testGetFilesToDeleteByMaxSize_multipleFilesPerDocument() = runBlocking {
        val records = listOf(
            RecordEntity(
                documentId = "doc1",
                businessId = "biz1",
                ownerId = "owner1",
                createdAt = now(),
                updatedAt = now()
            ),
            RecordEntity(
                documentId = "doc2",
                businessId = "biz1",
                ownerId = "owner1",
                createdAt = now(),
                updatedAt = now()
            )
        )
        recordsDao.createRecords(records)

        // doc1: 2 files, total size 300, oldest lastUsed = 1000L
        val file1a = FileEntity(
            documentId = "doc1",
            filePath = "/tmp/file1a",
            fileType = "pdf",
            lastUsed = 1000L,
            sizeBytes = 100L
        )
        val file1b = FileEntity(
            documentId = "doc1",
            filePath = "/tmp/file1b",
            fileType = "pdf",
            lastUsed = 1500L,
            sizeBytes = 200L
        )

        // doc2: 2 files, total size 400, oldest lastUsed = 2000L
        val file2a = FileEntity(
            documentId = "doc2",
            filePath = "/tmp/file2a",
            fileType = "pdf",
            lastUsed = 2000L,
            sizeBytes = 150L
        )
        val file2b = FileEntity(
            documentId = "doc2",
            filePath = "/tmp/file2b",
            fileType = "pdf",
            lastUsed = 2500L,
            sizeBytes = 250L
        )

        recordsDao.insertRecordFile(file1a)
        recordsDao.insertRecordFile(file1b)
        recordsDao.insertRecordFile(file2a)
        recordsDao.insertRecordFile(file2b)

        val maxSize = 400L // Can only keep doc2
        val result = recordsDao.getFilesToDeleteByMaxSize(maxSize)

        assertEquals(2, result.size)
        assertEquals("doc1", result[0].documentId)
        assertEquals("doc1", result[1].documentId)
    }

    @Test
    fun testGetFilesToDeleteByMaxSize_complexScenario() = runBlocking {
        val records = listOf(
            RecordEntity(
                documentId = "doc1",
                businessId = "biz1",
                ownerId = "owner1",
                createdAt = now(),
                updatedAt = now()
            ),
            RecordEntity(
                documentId = "doc2",
                businessId = "biz1",
                ownerId = "owner1",
                createdAt = now(),
                updatedAt = now()
            ),
            RecordEntity(
                documentId = "doc3",
                businessId = "biz1",
                ownerId = "owner1",
                createdAt = now(),
                updatedAt = now()
            ),
            RecordEntity(
                documentId = "doc4",
                businessId = "biz1",
                ownerId = "owner1",
                createdAt = now(),
                updatedAt = now()
            ),
            RecordEntity(
                documentId = "doc5",
                businessId = "biz1",
                ownerId = "owner1",
                createdAt = now(),
                updatedAt = now()
            )
        )
        recordsDao.createRecords(records)

        // doc1: size 50, lastUsed 1000
        recordsDao.insertRecordFile(
            FileEntity(
                documentId = "doc1",
                filePath = "/tmp/f1",
                fileType = "pdf",
                lastUsed = 1000L,
                sizeBytes = 50L
            )
        )

        // doc2: size 150, lastUsed 2000
        recordsDao.insertRecordFile(
            FileEntity(
                documentId = "doc2",
                filePath = "/tmp/f2",
                fileType = "pdf",
                lastUsed = 2000L,
                sizeBytes = 150L
            )
        )

        // doc3: size 200, lastUsed 3000
        recordsDao.insertRecordFile(
            FileEntity(
                documentId = "doc3",
                filePath = "/tmp/f3a",
                fileType = "pdf",
                lastUsed = 3000L,
                sizeBytes = 100L
            )
        )
        recordsDao.insertRecordFile(
            FileEntity(
                documentId = "doc3",
                filePath = "/tmp/f3b",
                fileType = "pdf",
                lastUsed = 3500L,
                sizeBytes = 100L
            )
        )

        // doc4: size 300, lastUsed 4000
        recordsDao.insertRecordFile(
            FileEntity(
                documentId = "doc4",
                filePath = "/tmp/f4",
                fileType = "pdf",
                lastUsed = 4000L,
                sizeBytes = 300L
            )
        )

        // doc5: size 100, lastUsed 5000 (newest)
        recordsDao.insertRecordFile(
            FileEntity(
                documentId = "doc5",
                filePath = "/tmp/f5",
                fileType = "pdf",
                lastUsed = 5000L,
                sizeBytes = 100L
            )
        )

        val maxSize =
            600L // Should keep doc5(100) + doc4(300) + doc3(200) = 600, delete doc1 and doc2
        val result = recordsDao.getFilesToDeleteByMaxSize(maxSize)

        assertEquals(2, result.size)
        val deletedDocIds = result.map { it.documentId }.toSet()
        assertEquals(setOf("doc1", "doc2"), deletedDocIds)
    }

    @Test
    fun testGetFilesToDeleteByMaxSize_identicalLastUsedTimestamps() = runBlocking {
        val records = listOf(
            RecordEntity(
                documentId = "doc1",
                businessId = "biz1",
                ownerId = "owner1",
                createdAt = now(),
                updatedAt = now()
            ),
            RecordEntity(
                documentId = "doc2",
                businessId = "biz1",
                ownerId = "owner1",
                createdAt = now(),
                updatedAt = now()
            ),
            RecordEntity(
                documentId = "doc3",
                businessId = "biz1",
                ownerId = "owner1",
                createdAt = now(),
                updatedAt = now()
            )
        )
        recordsDao.createRecords(records)

        // All documents have the same lastUsed timestamp
        val file1 = FileEntity(
            documentId = "doc1",
            filePath = "/tmp/file1",
            fileType = "pdf",
            lastUsed = 1000L,
            sizeBytes = 100L
        )
        val file2 = FileEntity(
            documentId = "doc2",
            filePath = "/tmp/file2",
            fileType = "pdf",
            lastUsed = 1000L,
            sizeBytes = 100L
        )
        val file3 = FileEntity(
            documentId = "doc3",
            filePath = "/tmp/file3",
            fileType = "pdf",
            lastUsed = 1000L,
            sizeBytes = 100L
        )

        recordsDao.insertRecordFile(file1)
        recordsDao.insertRecordFile(file2)
        recordsDao.insertRecordFile(file3)

        val maxSize = 200L
        val result = recordsDao.getFilesToDeleteByMaxSize(maxSize)

        // Should delete 1 document, determined by document_id ordering
        assertEquals(1, result.size)
    }

    @Test
    fun testGetFilesToDeleteByMaxSize_zeroMaxSize() = runBlocking {
        val record = RecordEntity(
            documentId = "doc1",
            businessId = "biz1",
            ownerId = "owner1",
            createdAt = now(),
            updatedAt = now()
        )
        recordsDao.createRecords(listOf(record))

        val file1 = FileEntity(
            documentId = "doc1",
            filePath = "/tmp/file1",
            fileType = "pdf",
            lastUsed = 1000L,
            sizeBytes = 100L
        )
        recordsDao.insertRecordFile(file1)

        val maxSize = 0L
        val result = recordsDao.getFilesToDeleteByMaxSize(maxSize)

        // With maxSize = 0, SQL still deletes the document since it can't fit
        assertEquals(1, result.size)
        assertEquals("doc1", result[0].documentId)
    }

    @Test
    fun testGetFilesToDeleteByMaxSize_largeMaxSize() = runBlocking {
        val records = listOf(
            RecordEntity(
                documentId = "doc1",
                businessId = "biz1",
                ownerId = "owner1",
                createdAt = now(),
                updatedAt = now()
            ),
            RecordEntity(
                documentId = "doc2",
                businessId = "biz1",
                ownerId = "owner1",
                createdAt = now(),
                updatedAt = now()
            )
        )
        recordsDao.createRecords(records)

        val file1 = FileEntity(
            documentId = "doc1",
            filePath = "/tmp/file1",
            fileType = "pdf",
            lastUsed = 1000L,
            sizeBytes = 100L
        )
        val file2 = FileEntity(
            documentId = "doc2",
            filePath = "/tmp/file2",
            fileType = "pdf",
            lastUsed = 2000L,
            sizeBytes = 100L
        )

        recordsDao.insertRecordFile(file1)
        recordsDao.insertRecordFile(file2)

        val maxSize = 10000L // Much larger than total
        val result = recordsDao.getFilesToDeleteByMaxSize(maxSize)

        assertEquals(0, result.size)
    }
}