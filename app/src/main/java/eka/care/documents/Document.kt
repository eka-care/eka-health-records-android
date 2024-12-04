package eka.care.documents

import com.eka.network.ConverterFactoryType
import com.eka.network.IOkHttpSetup
import com.eka.network.Networking

object Document {
    private var configuration: DocumentConfiguration? = null

    fun init(documentConfiguration: DocumentConfiguration) {
        configuration = documentConfiguration
        val host = configuration?.host
        if(host == null) throw Exception("")
        configuration?.okHttpSetup?.let {
            Networking.init(host, it, converterFactoryType = ConverterFactoryType.PROTO)
        }
    }
}