package eka.care.documents.data.repository

import eka.care.documents.data.db.entity.UpdatedAtEntity

interface UpdatedAtRepository {
    suspend fun getUpdatedAtByOid(oid: String, doctorId :String): String?
    suspend fun insertUpdatedAtEntity(updatedAtEntity: UpdatedAtEntity)
    suspend fun updateUpdatedAtByOid(oid: String, updatedAt: String, doctorId :String)
    suspend fun doesOidExist(oid: String, doctorId :String): Boolean
}