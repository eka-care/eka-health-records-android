package eka.care.documents.ui.utility

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class ThumbnailGenerator {
    companion object {
        fun getThumbnailFromPdf(app: Application, pdfFile: File): String? {
            try {
                // Reading pdf in READ Only mode.
                val fileDescriptor =
                    ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)

                // Initializing PDFRenderer object.
                val renderer = PdfRenderer(fileDescriptor)

                // Getting Page object by opening page.
                val page = renderer.openPage(0)

                // Creating empty bitmap. Bitmap.Config can be changed.
                val bitmap =
                    Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)

                // Creating Canvas from bitmap.
                val canvas = Canvas(bitmap)

                // Set White background color.
                canvas.drawColor(Color.WHITE)

                // Draw bitmap.
                canvas.drawBitmap(bitmap, 0f, 0f, null)

                // Render bitmap and can change mode too.
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                // closing page
                page.close()

                // saving image into sdcard.
                val file = File(app.cacheDir, "image${System.currentTimeMillis()}.png")

                // check if file already exists, then delete it.
                if (file.exists()) file.delete()

                // Saving image in PNG format with 100% quality.
                try {
                    val out = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    Log.d("log", "Saved Image - ${file.absolutePath}")
                    out.flush()
                    out.close()
                } catch (e: Exception) {
                    Log.e("log", "Exception in saving file = ", e)
                }
                return file.path
            } catch (ex: Exception) {
                return null
            }
        }
    }
}