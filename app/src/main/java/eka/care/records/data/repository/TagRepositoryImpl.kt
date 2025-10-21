package eka.care.records.data.repository

import android.content.Context
import android.net.Uri
import eka.care.records.client.model.TagModel
import eka.care.records.client.repository.RecordsRepository
import eka.care.records.client.repository.TagsRepository
import eka.care.records.client.utils.RecordsUtility
import eka.care.records.data.db.RecordsDatabase
import eka.care.records.data.entity.TagEntity
import eka.care.records.data.mlkit.OCRTextExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class TagRepositoryImpl(
    private val context: Context,
    private val recordsRepository: RecordsRepository
) : TagsRepository {
    private val dao = RecordsDatabase.getInstance(context).recordsDao()

    override suspend fun addTag(recordId: String, tag: String) {
        dao.insertTag(TagEntity(documentId = recordId, tag = tag))
    }

    override fun getTags(businessId: String, ownerIds: List<String>): Flow<List<TagModel>> {
        return flow {
            val data = dao.getDocumentTagsForBusinessAndOwners(
                businessId = businessId,
                ownerIds = ownerIds,
            )
            emitAll(data)
        }.catch { e ->
            emit(emptyList())
        }
    }

    override fun generateTags(recordId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val recordDetails = recordsRepository.getRecordDetails(recordId)
            val generatedTags = mutableListOf<String>()
            recordDetails?.files?.forEach { file ->
                if (!RecordsUtility.isImage(file.fileType)) {
                    file.filePath?.let { path ->
                        val result = OCRTextExtractor.extractTagsFromDocument(
                            context = context,
                            imageUri = Uri.fromFile(
                                RecordsUtility.getFileByPath(
                                    context = context,
                                    path = path
                                )
                            )
                        )
                        result.onSuccess {
                            generatedTags.addAll(it)
                        }
                    }
                }
            }
            generatedTags.forEach {
                addTag(recordId = recordId, tag = it)
            }
        }
    }
}