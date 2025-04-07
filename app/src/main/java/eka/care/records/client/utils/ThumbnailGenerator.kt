package eka.care.records.client.utils

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileOutputStream

class ThumbnailGenerator {
    companion object {
        fun getThumbnailFromPdf(app: Application, pdfFile: File): String? {
            try {
                val fileDescriptor =
                    ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(fileDescriptor)
                val page = renderer.openPage(0)
                val bitmap =
                    Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                val file = File(app.cacheDir, "image${System.currentTimeMillis()}.png")
                if (file.exists()) file.delete()
                try {
                    val out = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                    out.close()
                } catch (e: Exception) {
                    Logger.e("Error saving thumbnail: ${e.message}")
                }
                return file.path
            } catch (ex: Exception) {
                Logger.e("Error generating thumbnail: ${ex.message}")
                return null
            }
        }
    }
}