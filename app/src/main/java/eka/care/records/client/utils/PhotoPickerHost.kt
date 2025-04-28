package eka.care.records.client.utils

import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest

interface PhotoPickerHost {
    fun pickPhoto(request: PickVisualMediaRequest, callback: (List<Uri>) -> Unit)
}