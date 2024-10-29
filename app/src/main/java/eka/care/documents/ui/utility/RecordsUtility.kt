package eka.care.documents.ui.utility

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.io.IOException

class RecordsUtility {
    companion object{
        fun loadFromUri(context: Context, photoUri: Uri): Bitmap? {
            var bitmap: Bitmap? = null
            try {
                bitmap = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
                    val source: ImageDecoder.Source =
                        ImageDecoder.createSource(context.contentResolver, photoUri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, photoUri)
                }
            } catch (ex: IOException) {
                Log.e("log", "Exception in loadFromUri() = ", ex)
            }
            return bitmap
        }
    }
}