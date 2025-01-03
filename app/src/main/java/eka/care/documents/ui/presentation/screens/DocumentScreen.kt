package eka.care.documents.ui.presentation.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.reader.presentation.states.PdfSource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import eka.care.documents.R
import eka.care.documents.data.utility.DocumentUtility.Companion.PARAM_RECORD_PARAMS_MODEL
import eka.care.documents.sync.workers.SyncFileWorker
import eka.care.documents.ui.BgWhite
import eka.care.documents.ui.Gray200
import eka.care.documents.ui.presentation.activity.FileViewerActivity
import eka.care.documents.ui.presentation.components.DocumentBottomSheetContent
import eka.care.documents.ui.presentation.components.DocumentBottomSheetType
import eka.care.documents.ui.presentation.components.DocumentFilter
import eka.care.documents.ui.presentation.components.DocumentScreenContent
import eka.care.documents.ui.presentation.components.DocumentsSort
import eka.care.documents.ui.presentation.components.TopAppBarSmall
import eka.care.documents.ui.presentation.model.RecordParamsModel
import eka.care.documents.ui.presentation.state.GetRecordsState
import eka.care.documents.ui.presentation.viewmodel.RecordsViewModel
import eka.care.documents.ui.utility.RecordsAction
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalPermissionsApi::class)
@Composable
fun DocumentScreen(
    params: RecordParamsModel,
    viewModel: RecordsViewModel
) {
    val context = LocalContext.current

    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        viewModel.observeNetworkStatus(context)
    }

    val isOnline by viewModel.isOnline.collectAsState()
    val photoUri by viewModel.photoUri.collectAsState()

    LaunchedEffect(cameraPermissionState.status) {
        if (cameraPermissionState.status != PermissionStatus.Granted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    val options = GmsDocumentScannerOptions.Builder()
        .setGalleryImportAllowed(true)
        .setPageLimit(4)
        .setResultFormats(RESULT_FORMAT_JPEG)
        .setScannerMode(SCANNER_MODE_FULL)
        .build()

    val scanner = GmsDocumentScanning.getClient(options)

    val documentViewerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                initData(
                    oid = params.patientId,
                    doctorId = params.doctorId,
                    viewModel = viewModel,
                    context = context,
                    patientUuid = params.uuid
                )
            }
        }


    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            photoUri?.let { uri ->
                val imageUris = ArrayList<String>().apply {
                    add(uri.toString())
                }

                Intent(context, FileViewerActivity::class.java).apply {
                    putStringArrayListExtra("IMAGE_URIS", imageUris)
                    putExtra(PARAM_RECORD_PARAMS_MODEL, params)
                }.also {
                    documentViewerLauncher.launch(it)
                }
            }
        }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                viewModel.pdfSource = PdfSource.Uri(it)
                Intent(context, FileViewerActivity::class.java).apply {
                    putExtra("PDF_URI", it.toString())
                    putExtra(PARAM_RECORD_PARAMS_MODEL, params)
                }.also { intent ->
                    documentViewerLauncher.launch(intent)
                }
            }
        }
    )

    val scannerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data =
                    GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                data?.pages?.let { pages ->
                    val imageUris = ArrayList<String>()
                    for (page in pages) {
                        imageUris.add(page.imageUri.toString())
                    }
                    Intent(
                        context,
                        FileViewerActivity::class.java
                    ).apply {
                        putStringArrayListExtra("IMAGE_URIS", imageUris)
                        putExtra(PARAM_RECORD_PARAMS_MODEL, params)
                    }.also {
                        documentViewerLauncher.launch(it)
                    }
                }
            }
        }

    val pickMultipleMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
            if (uris.isNotEmpty()) {
                val imageUris = ArrayList<String>()
                for (uri in uris) {
                    imageUris.add(uri.toString())
                }
                Intent(context, FileViewerActivity::class.java).apply {
                    putStringArrayListExtra("IMAGE_URIS", imageUris)
                    putExtra(PARAM_RECORD_PARAMS_MODEL, params)
                }.also {
                    documentViewerLauncher.launch(it)
                }
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    val modalBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true,
        confirmValueChange = { true }
    )

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val openSheet = {
        scope.launch {
            modalBottomSheetState.show()
        }
    }
    val closeSheet = {
        scope.launch {
            modalBottomSheetState.hide()
        }
    }

    val isRefreshing by viewModel.isRefreshing
    val recordsState by viewModel.getRecordsState.collectAsState()
    var firstVisibleItemIndex by remember { mutableIntStateOf(0) }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            initData(
                oid = params.patientId,
                doctorId = params.doctorId,
                viewModel = viewModel,
                context = context,
                patientUuid = params.uuid
            )
        }
    )

    LaunchedEffect(isOnline) {
        if (isOnline) {
            initData(
                oid = params.patientId,
                doctorId = params.doctorId,
                viewModel = viewModel,
                context = context,
                patientUuid = params.uuid
            )
        }
    }

    LaunchedEffect(key1 = viewModel.documentType.intValue) {
        viewModel.getAvailableDocTypes(oid = params.patientId, doctorId = params.doctorId)
        viewModel.getLocalRecords(
            oid = params.patientId,
            doctorId = params.doctorId,
            docType = viewModel.documentType.intValue
        )
    }

    LaunchedEffect(viewModel.documentBottomSheetType) {
        viewModel.documentBottomSheetType?.let {
            scope.launch {
                modalBottomSheetState.hide()
                modalBottomSheetState.show()
            }
        }
    }

    SideEffect {
        scope.launch {
            snapshotFlow { listState.firstVisibleItemIndex }.collect { index ->
                firstVisibleItemIndex = index
            }
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        AlertDialog(
            containerColor = Color.White,
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Confirm Delete") },
            text = { Text(text = stringResource(id = R.string.are_you_sure_you_want_to_delete_this_record)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDocument(
                            localId = viewModel.cardClickData.value?.localId ?: "",
                            oid = params.patientId,
                            doctorId = params.doctorId
                        )
                        showDeleteDialog = false
                    }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("No")
                }
            }
        )
    }
    val resp = (recordsState as? GetRecordsState.Success)?.resp ?: emptyList()
    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetContent = {
            DocumentBottomSheetContent(
                onClick = {
                    when (it.action) {
                        RecordsAction.ACTION_OPEN_SHEET -> {
                            openSheet()
                        }

                        RecordsAction.ACTION_CLOSE_SHEET -> {
                            closeSheet()
                        }

                        RecordsAction.ACTION_OPEN_DELETE_DIALOG -> {
                            showDeleteDialog = true
                        }
                    }
                },
                scanner = scanner,
                context = context,
                scannerLauncher = scannerLauncher,
                pdfPickerLauncher = pdfPickerLauncher,
                cameraLauncher = cameraLauncher,
                viewModel = viewModel,
                params = params,
                galleryLauncher = pickMultipleMedia
            )
            Spacer(modifier = Modifier.height(16.dp))
        },
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetElevation = 0.dp
    ) {
        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgWhite)
                ) {
                    TopAppBarSmall(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BgWhite),
                        title = params.name,
                        subTitle = if (params.age != null && params.age > 1) {
                            "${params.gender}, ${params.age}y"
                        } else {
                            params.gender ?: ""
                        },
                        leading = R.drawable.ic_back_arrow,
                        onLeadingClick = {
                            (context as? Activity)?.finish()
                        }
                    )
                    if (!isOnline) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = Gray200)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.no_cloud),
                                contentDescription = "",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "offline")
                        }
                    }
                    if (resp.isNotEmpty()) {
                        DocumentFilter(
                            viewModel = viewModel,
                            onClick = {
                                //    if (viewModel.documentType.intValue != it) {
                                viewModel.getLocalRecords(
                                    oid = params.patientId,
                                    doctorId = params.doctorId,
                                    docType = it
                                )
                                //    }
                            }
                        )
                        DocumentsSort(
                            viewModel = viewModel,
                            onClickSort = {
                                openSheet()
                                viewModel.documentBottomSheetType =
                                    DocumentBottomSheetType.DocumentSort
                            })
                    }
                }
            },
            content = {
                DocumentScreenContent(
                    paddingValues = it,
                    pullRefreshState = pullRefreshState,
                    recordsState = recordsState,
                    openSheet = openSheet,
                    viewModel = viewModel,
                    listState = listState,
                    isRefreshing = isRefreshing,
                    paramsModel = params
                )
            },
        )
    }
}

fun initData(
    patientUuid: String,
    oid: String,
    doctorId: String,
    viewModel: RecordsViewModel,
    context: Context,
) {
    val inputData = Data.Builder()
        .putString("p_uuid", patientUuid)
        .putString("oid", oid)
        .putString("doctorId", doctorId)
        .build()

    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val uniqueWorkName = "syncFileWorker_${patientUuid}_$oid$doctorId"

    val uniqueSyncWorkRequest =
        OneTimeWorkRequestBuilder<SyncFileWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .build()

    WorkManager.getInstance(context)
        .enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.KEEP,
            uniqueSyncWorkRequest
        )

    viewModel.getLocalRecords(
        oid = oid,
        doctorId = doctorId,
        docType = viewModel.documentType.intValue
    )
    viewModel.syncDeletedDocuments(oid = oid, doctorId = doctorId)
    viewModel.syncEditedDocuments(oid = oid, doctorId = doctorId)
}