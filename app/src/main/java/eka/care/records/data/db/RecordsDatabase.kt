package eka.care.records.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import eka.care.records.data.dao.RecordsDao
import eka.care.records.data.entity.CaseEntity
import eka.care.records.data.entity.CaseRecordRelationEntity
import eka.care.records.data.entity.RecordEntity
import eka.care.records.data.entity.RecordFile

@Database(
    entities = [
        RecordEntity::class,
        RecordFile::class,
        CaseEntity::class,
        CaseRecordRelationEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RecordsDatabase : RoomDatabase() {
    abstract fun recordsDao(): RecordsDao

    companion object {
        private var mInstance: RecordsDatabase? = null


        @Synchronized
        fun getInstance(context: Context): RecordsDatabase {
            if (mInstance == null)
                mInstance = Room.databaseBuilder(
                    context, RecordsDatabase::class.java,
                    "document_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

            return mInstance!!
        }
    }
}