package eka.care.documents.data.repository

import eka.care.documents.data.db.database.DocumentDatabase
import eka.care.documents.data.db.entity.UpdatedAtEntity

class UpdatedAtRepositoryImpl(private val database: DocumentDatabase) : UpdatedAtRepository {

    override suspend fun getUpdatedAtByOid(filterId: String?, ownerId :String?): String? {
        return database.updatedAtDao().getUpdatedAtByOid(filterId = filterId, ownerId = ownerId)
    }

    override suspend fun insertUpdatedAtEntity(updatedAtEntity: UpdatedAtEntity) {
        database.updatedAtDao().insertUpdatedAtEntity(updatedAtEntity = updatedAtEntity)
    }

    override suspend fun updateUpdatedAtByOid(filterId: String?, updatedAt: String, ownerId :String?) {
        database.updatedAtDao().updateUpdatedAtByOid(filterId = filterId, updatedAt = updatedAt, ownerId = ownerId)
    }

    override suspend fun doesOidExist(filterId: String?, ownerId :String?): Boolean {
        return database.updatedAtDao().doesOidExist(filterId = filterId, ownerId = ownerId)
    }
}