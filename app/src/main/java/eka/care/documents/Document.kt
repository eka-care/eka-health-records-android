package eka.care.documents

import android.util.Log
import eka.care.documents.network.Networking
import eka.care.documents.network.OkHttpSetup

object Document {
    private var configuration: DocumentConfiguration? = null

    fun init(chatInitConfiguration: DocumentConfiguration) {
        configuration = chatInitConfiguration
        configuration?.okHttpSetup?.let {
            Networking.init("https://vault.eka.care/", okHttpSetup = OkHttpSetup())
            Log.v("DocumentSDK","OkHttpSetup Configured")
        }
    }
}