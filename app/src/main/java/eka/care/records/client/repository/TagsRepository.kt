package eka.care.records.client.repository

import eka.care.records.client.model.TagModel
import kotlinx.coroutines.flow.Flow

interface TagsRepository {
    suspend fun addTag(recordId: String, tag: String)

    fun getTags(businessId: String, ownerIds: List<String>): Flow<List<TagModel>>

    fun generateTags(recordId: String)
}