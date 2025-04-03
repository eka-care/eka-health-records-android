package eka.care.records.client.repository

import eka.care.records.data.entity.RecordFile

interface RecordFileRepository {
    suspend fun insertRecordFile(file: RecordFile): Long
    suspend fun getRecordFile(localId: String): List<RecordFile>?
}