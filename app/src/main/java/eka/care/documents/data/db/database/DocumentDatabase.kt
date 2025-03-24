package eka.care.documents.data.db.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import eka.care.documents.data.dao.VaultDao
import eka.care.documents.data.db.converter.Converters
import eka.care.documents.data.db.entity.VaultEntity

@Database(
    entities = [VaultEntity::class],
    version = 13,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DocumentDatabase : RoomDatabase() {
    abstract fun vaultDao(): VaultDao

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