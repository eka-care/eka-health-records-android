package eka.care.documents.ui.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import eka.care.documents.ui.DarwinTouchNeutral0
import eka.care.documents.ui.DarwinTouchNeutral1000
import eka.care.documents.ui.presentation.components.AddMedicalRecordsDetailViewComponent
import eka.care.documents.ui.presentation.model.RecordParamsModel
import eka.care.documents.ui.presentation.viewmodel.RecordsViewModel
import java.io.File

@Composable
fun EnterDetailsBottomSheet(
    onCLick: () -> Unit,
    fileType: Int,
    fileList: ArrayList<File>,
    paramsModel: RecordParamsModel,
    editDocument: Boolean,
    localId : String
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(DarwinTouchNeutral0, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Divider(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .width(48.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    DarwinTouchNeutral1000
                )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier.heightIn(min = screenHeight.times(0.3f), max = screenHeight.times(0.5f)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AddMedicalRecordsDetailViewComponent(
                onClick = {
                    if (!it.action.isNullOrEmpty()) {
                        when (it.action) {
                            "onBackClick" -> {
                                onCLick()
                            }
                        }
                    }
                },
                fileType = fileType,
                fileList = fileList,
                paramsModel = paramsModel,
                editDocument = editDocument,
                localId  = localId
            )
        }
    }
}