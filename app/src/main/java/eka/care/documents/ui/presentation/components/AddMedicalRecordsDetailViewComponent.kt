package eka.care.documents.ui.presentation.components

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Chip
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eka.care.documents.R
import eka.care.documents.data.db.entity.VaultEntity
import eka.care.documents.data.utility.DocumentUtility.Companion.docTypes
import eka.care.documents.ui.DarwinTouchNeutral0
import eka.care.documents.ui.DarwinTouchNeutral1000
import eka.care.documents.ui.DarwinTouchNeutral200
import eka.care.documents.ui.DarwinTouchNeutral400
import eka.care.documents.ui.DarwinTouchNeutral600
import eka.care.documents.ui.DarwinTouchNeutral800
import eka.care.documents.ui.DarwinTouchPrimary
import eka.care.documents.ui.DarwinTouchPrimaryBgDark
import eka.care.documents.ui.DarwinTouchPrimaryDark
import eka.care.documents.ui.DarwinTouchRed
import eka.care.documents.ui.presentation.activity.DocumentActivity
import eka.care.documents.ui.presentation.model.CTA
import eka.care.documents.ui.presentation.model.RecordParamsModel
import eka.care.documents.ui.presentation.viewmodel.RecordsViewModel
import eka.care.documents.ui.touchBodyBold
import eka.care.documents.ui.touchHeadlineBold
import eka.care.documents.ui.utility.RecordsUtility.Companion.formatLocalDateToCustomFormat
import eka.care.documents.ui.utility.RecordsUtility.Companion.timestampToLong
import eka.care.documents.ui.utility.ThumbnailGenerator
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun AddMedicalRecordsDetailViewComponent(
    onClick: (CTA) -> Unit,
    viewModel: RecordsViewModel,
    fileType: Int,
    fileList: ArrayList<File>,
    paramsModel: RecordParamsModel,
    editDocument: Boolean
) {
    init(viewModel = viewModel, userId =  paramsModel.patientId, docId = viewModel.cardClickData.value?.documentId)
    val context = LocalContext.current
    val compressedFiles by viewModel.compressedFiles.collectAsState(initial = emptyList())
    val initialSelectedDocType = viewModel.cardClickData.value?.documentType
    var selectedChipId by remember { mutableStateOf(initialSelectedDocType) }
    val selectedDate = remember { mutableStateOf("") }
    val openDialogRecord = remember { mutableStateOf(false) }
    val selectedTags by viewModel.selectedTags.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.compressFile(fileList, context)
    }
    LaunchedEffect(editDocument) {
        viewModel.loadSelectedTags(editDocument)
    }

    val unixTimestamp: Long? = viewModel.cardClickData.value?.documentDate
    val dateInMillis = unixTimestamp?.times(1000)
    val sdf = SimpleDateFormat("EEE, dd MMM, yyyy", Locale.getDefault())
    val formattedDate = dateInMillis?.let { Date(it) }?.let { sdf.format(it) }

    val date = if (editDocument) {
        if (selectedDate.value.length > 1) selectedDate.value else formattedDate
    } else {
        if (selectedDate.value.length > 1) selectedDate.value else "Add Date"
    }

    val onAddMedicalRecord = {

        if (editDocument) {
            viewModel.editDocument(
                localId = viewModel.cardClickData.value?.localId ?: "",
                docType = selectedChipId,
                oid = paramsModel.patientId,
                docDate = timestampToLong(date ?: System.currentTimeMillis().toString()),
                tags = selectedTags.joinToString(separator = ","),
                doctorId = paramsModel.doctorId
            )
            onClick(CTA(action = "onBackClick"))
        } else {
            if (selectedChipId != null) {
                val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
                val parsedDate = try {
                    sdf.parse(selectedDate.value)
                } catch (e: Exception) {
                    null
                }
                val unixTimestamp =
                    parsedDate?.time?.div(1000) ?: (System.currentTimeMillis() / 1000)
                val vaultEntity = VaultEntity(
                    localId = UUID.randomUUID().toString(),
                    documentId = null,
                    uuid = paramsModel.uuid,
                    oid = paramsModel.patientId,
                    filePath = if (fileType == FileType.IMAGE.ordinal) compressedFiles.map { it.path } else fileList.map { it.path },
                    fileType = if (fileType == FileType.IMAGE.ordinal) "img" else "pdf",
                    thumbnail = if (fileType == FileType.IMAGE.ordinal) {
                        compressedFiles[0].path
                    } else {
                        ThumbnailGenerator.getThumbnailFromPdf(
                            app = context.applicationContext as Application,
                            fileList[0]
                        )
                    },
                    createdAt = System.currentTimeMillis() / 1000,
                    source = null,
                    documentType = selectedChipId,
                    documentDate = unixTimestamp,
                    tags = selectedTags.joinToString(",").trimStart(','),
                    isABHALinked = false,
                    hashId = null,
                    cta = null,
                    doctorId = paramsModel.doctorId
                )

                viewModel.createVaultRecord(vaultEntity)
            }
            val intent = Intent(
                context,
                DocumentActivity::class.java
            )
            (context as Activity).setResult(Activity.RESULT_OK, intent)
            context.finish()
        }
    }

    if (openDialogRecord.value) {
        val datePickerStateRecord = rememberDatePickerState()
        val confirmEnabled = remember {
            derivedStateOf { datePickerStateRecord.selectedDateMillis != null }
        }

        LaunchedEffect(datePickerStateRecord.selectedDateMillis) {
            datePickerStateRecord.selectedDateMillis?.let { selectedMillis ->
                val selectedDateObj = Date(selectedMillis)
                selectedDate.value = formatLocalDateToCustomFormat(selectedDateObj) ?: ""
            }
        }

        DatePickerDialog(
            onDismissRequest = {
                openDialogRecord.value = false
            },
            colors = DatePickerDefaults.colors(
                containerColor = DarwinTouchNeutral0,
                titleContentColor = DarwinTouchNeutral800,
                headlineContentColor = DarwinTouchNeutral1000,
                todayDateBorderColor = DarwinTouchPrimary,
                todayContentColor = DarwinTouchPrimary,
                selectedDayContentColor = DarwinTouchNeutral0,
                selectedDayContainerColor = DarwinTouchPrimary
            ),
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerStateRecord.selectedDateMillis?.let { selectedMillis ->
                            val selectedDateObj = Date(selectedMillis)
                            selectedDate.value =
                                formatLocalDateToCustomFormat(selectedDateObj) ?: ""
                        }
                        openDialogRecord.value = false
                    }, enabled = confirmEnabled.value
                ) {
                    Text(
                        "OK",
                        style = touchBodyBold,
                        color = if (confirmEnabled.value) DarwinTouchPrimary else DarwinTouchNeutral400
                    )
                }
            }, dismissButton = {
                TextButton(onClick = { openDialogRecord.value = false }) {
                    Text(
                        "Cancel",
                        style = touchBodyBold,
                        color = DarwinTouchPrimary
                    )
                }
            }) {
            DatePicker(
                state = datePickerStateRecord, colors = DatePickerDefaults.colors(
                    containerColor = DarwinTouchNeutral200,
                    titleContentColor = DarwinTouchNeutral800,
                    headlineContentColor = DarwinTouchNeutral1000,
                    todayDateBorderColor = DarwinTouchPrimary,
                    todayContentColor = DarwinTouchPrimary,
                    selectedDayContentColor = DarwinTouchNeutral0,
                    selectedDayContainerColor = DarwinTouchPrimary
                )
            )
        }
    }

    Scaffold(
        topBar = {},
        content = {
            Column(modifier = Modifier.padding(it)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(content = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }, onClick = {
                        onClick(CTA(action = "onBackClick"))
                    })
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = if (editDocument) stringResource(id = R.string.edit_medical_record) else stringResource(
                            id = R.string.add_medical_records
                        ),
                        style = touchHeadlineBold,
                        color = DarwinTouchNeutral1000
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Image(
                            painter = painterResource(id = R.drawable.file),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(
                                DarwinTouchPrimary
                            ),
                            modifier = Modifier
                                .padding(top = 4.dp, end = 8.dp)
                                .size(16.dp)
                        )
                        Text(
                            text = "*",
                            color = DarwinTouchRed,
                            modifier = Modifier.padding(start = 16.dp, bottom = 22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(docTypes) { docType ->
                            Chip(
                                onClick = { selectedChipId = docType.idNew },
                                shape = RoundedCornerShape(16.dp),
                                border = if (selectedChipId == docType.idNew) BorderStroke(
                                    1.dp,
                                    DarwinTouchPrimaryBgDark
                                ) else BorderStroke(1.dp, DarwinTouchNeutral400),
                                colors = ChipDefaults.chipColors(
                                    contentColor = if (selectedChipId == docType.idNew) DarwinTouchPrimaryDark else DarwinTouchNeutral1000,
                                    backgroundColor = if (selectedChipId == docType.idNew) DarwinTouchPrimaryBgDark else DarwinTouchNeutral0
                                )
                            ) {
                                Text(
                                    text = docType.documentType.toString(),
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .clickable {
                            openDialogRecord.value = true
                        }
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_calendar),
                        contentDescription = "",
                        modifier = Modifier
                            .size(16.dp),
                        colorFilter = ColorFilter.tint(DarwinTouchPrimary)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = date ?: "",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = touchBodyBold
                    )
                }
//                TagsComponent(
//                    onChange = { newChips ->
//                        viewModel.updateSelectedTags(newChips)
//                    },
//                    selectedChips = selectedTags,
//                    source = SourceEnum.RECORDS
//                )
            }
        },
        bottomBar = {
            Button(
                colors = ButtonDefaults.buttonColors(
                    contentColor = DarwinTouchNeutral0,
                    containerColor = if (selectedChipId != null) DarwinTouchPrimary else DarwinTouchNeutral600
                ),
                onClick = {
                    onAddMedicalRecord()
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    )
}
private fun init(viewModel: RecordsViewModel, docId : String?, userId : String){
    if (docId != null) {
        viewModel.getTags(docId = docId, userId = userId)
    }
}

enum class FileType {
    IMAGE, PDF
}