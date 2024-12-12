package eka.care.documents.data.repository

import eka.care.documents.data.db.database.DocumentDatabase
import eka.care.documents.data.db.entity.UpdatedAtEntity

class UpdatedAtRepositoryImpl(private val database: DocumentDatabase) : UpdatedAtRepository {

    override suspend fun getUpdatedAtByOid(oid: String, doctorId: String): String? {
        return database.updatedAtDao().getUpdatedAtByOid(oid = oid, doctorId = doctorId)
    }

    override suspend fun insertUpdatedAtEntity(updatedAtEntity: UpdatedAtEntity) {
        database.updatedAtDao().insertUpdatedAtEntity(updatedAtEntity = updatedAtEntity)
    }

    override suspend fun updateUpdatedAtByOid(oid: String, updatedAt: String, doctorId: String) {
        database.updatedAtDao().updateUpdatedAtByOid(oid = oid, updatedAt = updatedAt, doctorId = doctorId)
    }

    override suspend fun doesOidExist(oid: String, doctorId: String): Boolean {
        return database.updatedAtDao().doesOidExist(oid = oid, doctorId = doctorId)
    }
}