package eka.care.documents.ui.presentation.state

import androidx.annotation.Keep
import eka.care.documents.data.db.entity.VaultEntity
import eka.care.documents.data.db.model.AvailableDocTypes
import eka.care.documents.ui.presentation.model.RecordModel

sealed class GetRecordsState {
    object Loading: GetRecordsState()
    data class Error(val error: String?): GetRecordsState()
    data class Success(val resp: List<RecordModel>): GetRecordsState()
    object EmptyState: GetRecordsState()
}

@Keep
data class GetAvailableDocTypesState(
    val isLoading: Boolean = false,
    val resp: List<AvailableDocTypes>? = null,
    val error: String? = null,
)