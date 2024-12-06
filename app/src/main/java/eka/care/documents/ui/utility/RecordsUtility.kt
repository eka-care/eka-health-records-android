package eka.care.documents.ui.utility

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        fun convertLongToFormattedDate(timestamp: Long): String {
            val date = Date(timestamp * 1000)
            val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            return outputFormat.format(date)
        }

        fun formatLocalDateToCustomFormat(date: Date): String? {
            val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            return formatter.format(date)
        }

        fun timestampToLong(timestamp: String, format: String = "EEE, dd MMM, yyyy"): Long {
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
            val date = dateFormat.parse(timestamp) ?: throw IllegalArgumentException("Invalid date format")
            return date.time / 1000
        }

        fun convertLongToDateString(time: Long?): String {
            if (time == null) return ""
            val date = Date(time * 1000)
            val format = SimpleDateFormat("dd EEE yyyy", Locale.getDefault())
            return format.format(date)
        }

        fun changeDateFormat(inputDate: String?): String {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val date = inputFormat.parse(inputDate)
            return date?.let { outputFormat.format(it) } ?: ""
        }

        fun isOnline(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return true
                }
            }
            return false
        }
    }
}