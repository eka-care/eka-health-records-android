package eka.care.records.client.model

import androidx.annotation.Keep

@Keep
enum class SortOrder(val value: String, val order: String) {
    CREATED_AT_ASC("created_at", "ASC"),
    CREATED_AT_DSC("created_at", "DESC"),
    DOC_DATE_ASC("document_date", "ASC"),
    DOC_DATE_DSC("document_date", "DESC"),
    UPDATED_AT_ASC("updated_at", "ASC"),
    UPDATED_AT_DSC("updated_at", "DESC"),
}