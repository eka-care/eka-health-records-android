package eka.care.records.client.utils

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

object MediaPickerManager {
    private var host: PhotoPickerHost? = null

    fun setHost(host: PhotoPickerHost) {
        this.host = host
    }

    fun requestImageFromHost() {
        val request = PickVisualMediaRequest.Builder()
            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
            .build()

        host?.pickPhoto(request) { uri ->
            println("SDK received image URI: $uri")
        } ?: println("Host not set!")
    }
}
