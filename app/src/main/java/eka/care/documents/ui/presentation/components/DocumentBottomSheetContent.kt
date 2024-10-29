package eka.care.documents.ui.presentation.components

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.Composable
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import eka.care.documents.ui.presentation.model.RecordParamsModel
import eka.care.documents.ui.presentation.screens.DocumentOptionsBottomSheet
import eka.care.documents.ui.presentation.screens.DocumentSortBottomSheet
import eka.care.documents.ui.presentation.screens.DocumentUploadBottomSheet
import eka.care.documents.ui.presentation.screens.EnterDetailsBottomSheet
import eka.care.documents.ui.presentation.viewmodel.RecordsViewModel
import eka.care.documents.ui.utility.RecordsAction
import kotlinx.coroutines.Job

@Composable
fun DocumentBottomSheetContent(
    closeSheet: () -> Job,
    scanner: GmsDocumentScanner,
    context: Context,
    scannerLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
    pdfPickerLauncher: ManagedActivityResultLauncher<Array<String>, Uri?>,
    openSheet: () -> Job,
    viewModel: RecordsViewModel,
    params: RecordParamsModel
) {
    when (viewModel.documentBottomSheetType) {
        DocumentBottomSheetType.DocumentUpload -> {
            DocumentUploadBottomSheet(onClick = {
                closeSheet()
                when (it?.action) {
                    RecordsAction.ACTION_TAKE_PHOTO -> {
                        scanner.getStartScanIntent(context as Activity)
                            .addOnSuccessListener { intentSender ->
                                scannerLauncher.launch(
                                    IntentSenderRequest.Builder(
                                        intentSender
                                    ).build()
                                )
                            }
                            .addOnFailureListener {
                                it.message
                            }
                    }

                    RecordsAction.ACTION_UPLOAD_PDF -> {
                        pdfPickerLauncher.launch(arrayOf("application/pdf"))
                    }
                }
            })
        }

        DocumentBottomSheetType.DocumentOptions -> {
            DocumentOptionsBottomSheet(onClick = {
                closeSheet()
                when (it?.action) {
                    RecordsAction.ACTION_EDIT_DOCUMENT -> {
                        openSheet()
                        viewModel.documentBottomSheetType = DocumentBottomSheetType.EnterFileDetails
                    }

                    RecordsAction.ACTION_DELETE_RECORD -> {
                        viewModel.deleteDocument(
                            localId = viewModel.localId.value,
                            oid = params.patientId,
                            doctorId = params.doctorId
                        )
                    }
                }
            })
        }

        DocumentBottomSheetType.DocumentSort -> {
            DocumentSortBottomSheet(
                selectedSort = viewModel.sortBy.value,
                onCloseClick = {
                    closeSheet()
                },
                onClick = {
                    viewModel.sortBy.value = it
                    viewModel.getLocalRecords(
                        oid = params.patientId,
                        viewModel.documentType.value,
                        doctorId = params.doctorId
                    )
                    closeSheet()
                },
            )
        }

        DocumentBottomSheetType.EnterFileDetails -> {
            EnterDetailsBottomSheet(
                onCLick = {
                    closeSheet()
                },
                fileList = ArrayList(),
                paramsModel = params,
                fileType = FileType.IMAGE.ordinal,
                viewModel = viewModel,
                editDocument = true
            )
        }

        null -> {}
    }
}