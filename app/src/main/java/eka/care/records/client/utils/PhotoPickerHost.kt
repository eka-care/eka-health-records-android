package eka.care.records.client.utils

import android.content.Intent
import android.net.Uri
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest

interface PhotoPickerHost {
    fun takePhoto(cameraIntent: Intent, uri: Uri)
    fun pickPhoto(request: PickVisualMediaRequest)
    fun pickPdf()
    fun scanDocuments(request: IntentSenderRequest)
}