package eka.care.documents

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import eka.care.documents.data.db.database.DocumentDatabase
import eka.care.documents.data.db.entity.VaultEntity
import eka.care.documents.data.repository.VaultRepositoryImpl
import eka.care.documents.ui.presentation.model.CTA
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
//@RunWith(AndroidJUnit4::class)
//class ExampleInstrumentedTest {
//    @Test
//    fun useAppContext() {
//        // Context of the app under test.
//        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
//        assertEquals("eka.care.documents", appContext.packageName)
//    }
//}

@RunWith(AndroidJUnit4::class)
class DocumentInstrumentedTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
//        val documentConfiguration = DocumentConfiguration(
//            host = "https://example.com",
//            okHttpSetup = OkHttpClient.Builder().build()
//        )
//        Document.init(context, documentConfiguration)
    }

    @After
    fun tearDown() {
        Document.destroy()
    }

    @Test
    fun testGetRecordById_validId_returnsRecordModel() = runBlocking {
        val testRecord = VaultEntity(
            localId = "123",
            documentId = "doc123",
            doctorId = "doc456",
            uuid = "uuid123",
            oid = "oid123",
            filePath = listOf("path1", "path2"),
            fileType = "pdf",
            thumbnail = "thumbnail_url",
            createdAt = System.currentTimeMillis(),
            source = 1,
            isEdited = false,
            isDeleted = false,
            documentType = 1,
            documentDate = System.currentTimeMillis(),
            tags = "tag1,tag2",
            cta = Gson().toJson(CTA(action = "view", title = "View Document")),
            hashId = "hash123",
            isABHALinked = false,
            shareWithDoctor = false,
            isAnalyzing = false
        )
        val repository = VaultRepositoryImpl(DocumentDatabase.getInstance(context))
        repository.storeDocuments(listOf( testRecord))

        val result = Document.getRecordById("doc123")

        assertEquals("doc123", result?.documentId)
    }

    @Test
    fun testGetRecordById_invalidId_returnsNull() = runBlocking {
        val result = Document.getRecordById("invalid_id")
        assertNull(result)
    }

    @Test
    fun testGetRecordById_nullId_returnsNull() = runBlocking {
        val result = Document.getRecordById(null)
        assertNull(result)
    }
}