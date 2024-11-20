package eka.care.documents

import com.eka.network.Networking

object Document {
    private var configuration: DocumentConfiguration? = null

    fun init(chatInitConfiguration: DocumentConfiguration) {
        configuration = chatInitConfiguration
        configuration?.okHttpSetup?.let {
            Networking.init("https://vault.eka.care/", it)
        }
    }
}