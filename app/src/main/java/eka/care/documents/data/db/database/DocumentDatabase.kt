package eka.care.documents.data.db.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import eka.care.documents.data.dao.VaultDao
import eka.care.documents.data.db.converter.Converters
import eka.care.documents.data.db.entity.VaultEntity

@Database(
    entities = [VaultEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DocumentDatabase : RoomDatabase() {
    abstract fun vaultDao() : VaultDao

    companion object {
        const val DATABASE_NAME = "document_db"
    }
}