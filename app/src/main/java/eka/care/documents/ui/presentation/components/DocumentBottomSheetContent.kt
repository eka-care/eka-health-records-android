package eka.care.documents.ui.presentation.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.FileProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import eka.care.documents.ui.presentation.model.CTA
import eka.care.documents.ui.presentation.model.RecordParamsModel
import eka.care.documents.ui.presentation.screens.DocumentOptionsBottomSheet
import eka.care.documents.ui.presentation.screens.DocumentSortBottomSheet
import eka.care.documents.ui.presentation.screens.DocumentUploadBottomSheet
import eka.care.documents.ui.presentation.screens.EnterDetailsBottomSheet
import eka.care.documents.ui.presentation.viewmodel.RecordsViewModel
import eka.care.documents.ui.utility.RecordsAction
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DocumentBottomSheetContent(
    onClick: (CTA) -> Unit,
    scanner: GmsDocumentScanner,
    context: Context,
    scannerLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
    pdfPickerLauncher: ManagedActivityResultLauncher<Array<String>, Uri?>,
    cameraLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    galleryLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, List<@JvmSuppressWildcards Uri>>,
    viewModel: RecordsViewModel,
    params: RecordParamsModel,
    allFilterIds : List<String>
) {
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    when (viewModel.documentBottomSheetType) {
        DocumentBottomSheetType.DocumentUpload -> {
            DocumentUploadBottomSheet(onClick = {
                onClick(CTA(action = RecordsAction.ACTION_CLOSE_SHEET))
                when (it?.action) {
                    RecordsAction.ACTION_TAKE_PHOTO-> {
                        if (cameraPermissionState.status.isGranted) {
                            val imageFile = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                            val photoUri = FileProvider.getUriForFile(
                                context,
                                "eka.care.doctor.fileprovider",
                                imageFile
                            )
                            viewModel.updatePhotoUri(photoUri)
                            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                                putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                            }

                            cameraLauncher.launch(cameraIntent)
                        } else {
                            if (cameraPermissionState.status.shouldShowRationale) {
                                // Show rationale and call `cameraPermissionState.launchPermissionRequest()`
                                cameraPermissionState.launchPermissionRequest()
                            } else {
                                // Permission permanently denied. Guide user to app settings.
                                Toast.makeText(context, "Permission Permanently Denied!", Toast.LENGTH_SHORT).show()
                                navigateToAppSettings(context = context)
                                // Show a dialog or toast guiding the user to enable the permission from settings.
                            }
                        }
                    }
                    RecordsAction.ACTION_SCAN_A_DOCUMENT-> {
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
                onClick(CTA(action = RecordsAction.ACTION_CLOSE_SHEET))
                when (it?.action) {
                    RecordsAction.ACTION_EDIT_DOCUMENT -> {
                        onClick(CTA(action = RecordsAction.ACTION_OPEN_SHEET))
                        viewModel.documentBottomSheetType = DocumentBottomSheetType.EnterFileDetails
                    }

                    RecordsAction.ACTION_SHARE_DOCUMENT -> {
                        val filePaths = viewModel.cardClickData.value?.filePath ?: emptyList()
                        if(filePaths.isEmpty()){
                            Toast.makeText(context, "Syncing data, please wait!", Toast.LENGTH_SHORT).show()
                        }else{
                            FileSharing().shareFiles(context, filePaths)
                        }
                    }

                    RecordsAction.ACTION_DELETE_RECORD -> {
                        onClick(CTA(action = RecordsAction.ACTION_OPEN_DELETE_DIALOG))
                    }
                }
            })
        }

        DocumentBottomSheetType.DocumentSort -> {
            DocumentSortBottomSheet(
                selectedSort = viewModel.sortBy.value,
                onCloseClick = {
                    onClick(CTA(action = RecordsAction.ACTION_CLOSE_SHEET))
                },
                onClick = {
                    viewModel.sortBy.value = it
                    viewModel.getLocalRecords(
                        filterIds = allFilterIds,
                        docType = viewModel.documentType.value,
                        ownerId = params.ownerId
                    )
                    onClick(CTA(action = RecordsAction.ACTION_CLOSE_SHEET))
                },
            )
        }

        DocumentBottomSheetType.EnterFileDetails -> {
            EnterDetailsBottomSheet(
                onCLick = {
                    onClick(CTA(action = RecordsAction.ACTION_CLOSE_SHEET))
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

fun navigateToAppSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }catch (e : Exception){

    }
}