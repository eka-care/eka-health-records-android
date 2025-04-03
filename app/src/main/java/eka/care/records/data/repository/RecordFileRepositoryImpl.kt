package eka.care.records.data.repository

import android.content.Context
import eka.care.documents.data.db.database.DocumentDatabase
import eka.care.records.client.repository.RecordFileRepository
import eka.care.records.data.entity.RecordFile

internal class RecordFileRepositoryImpl(context: Context): RecordFileRepository {
    private var dao = DocumentDatabase.getInstance(context).recordFilesDao()

    override suspend fun insertRecordFile(file: RecordFile): Long {
        return dao.insert(recordFile = file)
    }

    override suspend fun getRecordFile(localId: String): List<RecordFile>? {
        return dao.getRecordFile(localId = localId)
    }
}