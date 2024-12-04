package eka.care.documents

import com.eka.network.ConverterFactoryType
import com.eka.network.IOkHttpSetup
import com.eka.network.Networking

object Document {
    private var configuration: DocumentConfiguration? = null

    fun init(documentConfiguration: DocumentConfiguration) {
        configuration = documentConfiguration
        configuration?.let {
            Networking.init(it.host, it.okHttpSetup, converterFactoryType = ConverterFactoryType.PROTO)
        }
    }

    fun getConfiguration() = configuration
}