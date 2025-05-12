package eka.care.records.client.utils

import com.eka.network.Networking

object Document {
    private var configuration: DocumentConfiguration? = null

    fun init(documentConfiguration: DocumentConfiguration) {
        configuration = documentConfiguration
        configuration?.let {
            Networking.init(
                it.host,
                it.okHttpSetup
            )
        }
    }
}