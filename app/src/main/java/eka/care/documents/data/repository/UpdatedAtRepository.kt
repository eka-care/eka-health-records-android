package eka.care.documents.data.repository

import eka.care.documents.data.db.entity.UpdatedAtEntity

interface UpdatedAtRepository {
    suspend fun getUpdatedAtByOid(filterId: String?, ownerId :String?): String?
    suspend fun insertUpdatedAtEntity(updatedAtEntity: UpdatedAtEntity)
    suspend fun updateUpdatedAtByOid(filterId: String?, updatedAt: String, ownerId :String?)
    suspend fun doesOidExist(filterId: String?, ownerId :String?): Boolean
}