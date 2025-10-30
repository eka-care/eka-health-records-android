package eka.care.records.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import eka.care.records.data.dao.EncounterRecordDao
import eka.care.records.data.dao.RecordsDao
import eka.care.records.data.entity.EncounterEntity
import eka.care.records.data.entity.EncounterRecordCrossRef
import eka.care.records.data.entity.FileEntity
import eka.care.records.data.entity.FileEntityFts
import eka.care.records.data.entity.RecordEntity
import eka.care.records.data.entity.TagEntity

@Database(
    entities = [
        RecordEntity::class,
        FileEntity::class,
        FileEntityFts::class,
        EncounterEntity::class,
        EncounterRecordCrossRef::class,
        TagEntity::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
internal abstract class RecordsDatabase : RoomDatabase() {
    abstract fun recordsDao(): RecordsDao
    abstract fun encounterDao(): EncounterRecordDao

    companion object {
        @Volatile
        private var mInstance: RecordsDatabase? = null

        fun getInstance(context: Context): RecordsDatabase {
            return mInstance ?: synchronized(this) {
                mInstance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RecordsDatabase::class.java,
                    "document_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build().also { mInstance = it }
            }
        }
    }
}