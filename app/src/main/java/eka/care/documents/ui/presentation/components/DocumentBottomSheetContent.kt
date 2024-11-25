package eka.care.documents.ui.presentation.components

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
    galleryLauncher : ManagedActivityResultLauncher<PickVisualMediaRequest, List<@JvmSuppressWildcards Uri>>,
    openSheet: () -> Job,
    viewModel: RecordsViewModel,
    params: RecordParamsModel
) {
    val localId = viewModel.cardClickData.value?.localId
    val filePathState = viewModel.documentData.collectAsState()
    localId?.let { it1 ->
        viewModel.getDocumentData(oid =  params.patientId, localId = it1)
    }
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

                    RecordsAction.ACTION_CHOOSE_FROM_GALLERY -> {
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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

                    RecordsAction.ACTION_SHARE_DOCUMENT -> {
                        Log.d("AYUSHI", filePathState.value.first.toString())
                        if(filePathState.value.first?.isEmpty() == true){
                            Toast.makeText(context, "Syncing data, please wait!", Toast.LENGTH_SHORT).show()
                        }else{
                            FileSharing().shareFile(context, filePathState.value.first?.firstOrNull() ?: "")
                        }
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