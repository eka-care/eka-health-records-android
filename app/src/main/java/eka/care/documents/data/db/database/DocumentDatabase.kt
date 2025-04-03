package eka.care.documents.data.db.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import eka.care.documents.data.dao.UpdatedAtDao
import eka.care.documents.data.dao.VaultDao
import eka.care.documents.data.db.converter.Converters
import eka.care.documents.data.db.entity.UpdatedAtEntity
import eka.care.documents.data.db.entity.VaultEntity
import eka.care.records.data.dao.RecordFilesDao
import eka.care.records.data.dao.RecordsDao
import eka.care.records.data.entity.RecordEntity
import eka.care.records.data.entity.RecordFile

@Database(
    entities = [
        VaultEntity::class,
        UpdatedAtEntity::class,
        RecordEntity::class,
        RecordFile::class
    ],
    version = 15,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DocumentDatabase : RoomDatabase() {
    abstract fun vaultDao(): VaultDao
    abstract fun updatedAtDao(): UpdatedAtDao
    abstract fun recordsDao(): RecordsDao
    abstract fun recordFilesDao(): RecordFilesDao

    companion object {
        private var mInstance: DocumentDatabase? = null


        @Synchronized
        fun getInstance(context: Context): DocumentDatabase {
            if (mInstance == null)
                mInstance = Room.databaseBuilder(
                    context, DocumentDatabase::class.java,
                    "document_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

            return mInstance!!
        }
    }
}