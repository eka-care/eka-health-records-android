package eka.care.records.data.mlkit
//
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import eka.care.records.data.mlkit.parsers.PdfParser
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class DocumentParserTest {

    @Test
    fun testPdfTextExtraction() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Copy PDF from assets to cache dir
        val assetName = "test-report.pdf"
        val outFile = File(context.cacheDir, assetName)

        context.assets.open(assetName).use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        }
        val header = outFile.inputStream().bufferedReader().readLine()
        println("HEADER: $header")

        val parser = PdfParser()
        val result = parser.parseDocument(outFile.absolutePath, context)

        assertTrue(result.isSuccess)
        val text = result.getOrNull() ?: ""

        // Debug output
        println("Extracted text:\n$text")
    }
}