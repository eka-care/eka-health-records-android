package eka.care.documents

import android.content.Context
import com.eka.network.ConverterFactoryType
import com.eka.network.Networking
import eka.care.documents.data.db.database.DocumentDatabase

object Document {
    private var configuration: DocumentConfiguration? = null
    private var db: DocumentDatabase? = null

    fun init(context: Context, documentConfiguration: DocumentConfiguration) {
        configuration = documentConfiguration
        configuration?.let {
            Networking.init(it.host, it.okHttpSetup, converterFactoryType = ConverterFactoryType.PROTO)
        }
        db = DocumentDatabase.getInstance(context)
    }

    fun destroy(){
        db?.clearAllTables()
    }

    fun getConfiguration() = configuration
}