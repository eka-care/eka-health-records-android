package eka.care.documents.ui.presentation.activity

//import com.example.reader.PdfReaderManager
//import com.example.reader.presentation.states.PdfSource
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import eka.care.documents.ui.DarwinTouchNeutral1000
import eka.care.documents.ui.DarwinTouchPrimaryBgLight
import eka.care.documents.ui.touchLabelBold
import java.io.IOException

class FileViewerActivity : AppCompatActivity() {

//    private val pdfReaderManager: PdfReaderManager by lazy { PdfReaderManager(this) }
//    private var fileToUpload: File? = null
//
//    @OptIn(ExperimentalMaterialApi::class)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val recordsViewModel: RecordsViewModel by viewModels()
//
//        val paramsRecord = intent.getParcelableExtra<RecordParamsModel>(PARAM_RECORD_PARAMS_MODEL)
//        if (paramsRecord == null) {
//            finish()
//            return
//        }
//
//        val pdfUriString = intent.getStringExtra("PDF_URI")
//        val imageUris: ArrayList<String>? = intent.getStringArrayListExtra("IMAGE_URIS")
//
//        val filesPreviewList = arrayListOf<File>()
//
//        if (pdfUriString != null) {
//            val pdfUri = Uri.parse(pdfUriString)
//            recordsViewModel.pdfSource = PdfSource.Uri(pdfUri)
//            fileToUpload = uriToFile(pdfUri)
//        } else if (!imageUris.isNullOrEmpty()) {
//            for (uriString in imageUris) {
//                val imageUri = Uri.parse(uriString)
//                val cameraPic = RecordsUtility.loadFromUri(this, imageUri)
//                cameraPic?.let {
//                    val file = getFileFromUri(imageUri)
//                    file?.let { filesPreviewList.add(it) }
//                }
//            }
//        }
//
//        setContent {
//            val context = LocalContext.current
//            val modalBottomSheetState = ModalBottomSheetState(
//                initialValue = ModalBottomSheetValue.Hidden,
//                density = Density(context),
//                confirmValueChange = { true },
//                isSkipHalfExpanded = true,
//            )
//
//            val scope = rememberCoroutineScope()
//
//            val openSheet = {
//                scope.launch {
//                    modalBottomSheetState.show()
//                }
//            }
//            val closeSheet = {
//                scope.launch {
//                    modalBottomSheetState.hide()
//                }
//            }
//
//            ModalBottomSheetLayout(
//                sheetState = modalBottomSheetState,
//                sheetContent = {
//                    val pdfFile = fileToUpload ?: File("")
//                    EnterDetailsBottomSheet(
//                        onCLick = {
//                            (context as Activity).setResult(RESULT_OK, intent)
//                            closeSheet()
//                        },
//                        viewModel = recordsViewModel,
//                        fileType = if (pdfUriString != null) FileType.PDF.ordinal else FileType.IMAGE.ordinal,
//                        fileList = if (pdfUriString != null) arrayListOf(pdfFile) else filesPreviewList,
//                        paramsModel = paramsRecord,
//                        editDocument = false
//                    )
//                },
//                sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
//                sheetElevation = 0.dp,
//                content = {
//                    PreviewComponent(
//                        pdfReaderManager = if (pdfUriString != null) pdfReaderManager else null,
//                        recordsViewModel = recordsViewModel,
//                        onClick = {
//                            (context as Activity).setResult(RESULT_OK, intent)
//                            openSheet()
//                        },
//                        filePreviewList = filesPreviewList,
//                        pdfUriString = pdfUriString ?: ""
//                    )
//                }
//            )
//        }
//    }
//
//    private fun uriToFile(uri: Uri): File {
//        val fileName = "temp_pdf_${System.currentTimeMillis()}.pdf"
//        val file = File(applicationContext.cacheDir, fileName)
//
//        return try {
//            val inputStream = contentResolver.openInputStream(uri) ?: throw IOException("Unable to open input stream for URI: $uri")
//            val outputStream = file.outputStream()
//            inputStream.use { input ->
//                outputStream.use { output ->
//                    input.copyTo(output)
//                }
//            }
//            file
//        } catch (e: Exception) {
//            e.printStackTrace()
//            throw IOException("Failed to convert URI to file: ${e.message}", e)
//        }
//    }
//
//    private fun getFileFromUri(uri: Uri): File? {
//        return try {
//            val inputStream = contentResolver.openInputStream(uri)
//            val tempFile = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
//            tempFile.outputStream().use { output ->
//                inputStream?.copyTo(output)
//            }
//            tempFile
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
}

//@Composable
//fun PreviewComponent(
//    pdfReaderManager: PdfReaderManager?,
//    recordsViewModel: RecordsViewModel,
//    onClick: () -> Unit,
//    filePreviewList: List<File>,
//    pdfUriString: String
//) {
//    Box(
//        modifier = Modifier.background(DarwinTouchNeutral1000),
//        contentAlignment = Alignment.BottomCenter
//    ) {
//        if (pdfUriString.isEmpty()) {
//            LazyColumn(modifier = Modifier.fillMaxSize()) {
//                items(filePreviewList) { file ->
//                    val bitmap = BitmapFactory.decodeFile(file.path)
//                        ?.let { fixImageOrientation(it, file.path) }
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(8.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        AsyncImage(
//                            model = bitmap,
//                            contentDescription = null,
//                            modifier = Modifier.fillMaxWidth()
//                        )
//                    }
//                }
//                item {
//                    Spacer(modifier = Modifier.padding(80.dp))
//                }
//            }
//        } else {
//            recordsViewModel.pdfSource?.let { source ->
//                Column(verticalArrangement = Arrangement.Center) {
//                    Box(modifier = Modifier.fillMaxSize()) {
//                        pdfReaderManager?.PdfViewer(pdfSource = source)
//                    }
//                    Spacer(modifier = Modifier.height(1000.dp))
//                }
//            }
//        }
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
//                .background(DarwinTouchNeutral0)
//                .wrapContentSize()
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(48.dp)
//        ) {
//            CircularImageComponent(
//                image = R.drawable.ic_files_selection_done_tick,
//                modifier = Modifier,
//                onClick = onClick,
//                action = "Upload"
//            )
//        }
//    }
//}

fun fixImageOrientation(bitmap: Bitmap, filePath: String): Bitmap {
    try {
        val exif = ExifInterface(filePath)
        val orientation =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return bitmap
}


@Composable
fun CircularImageComponent(image: Int, modifier: Modifier, onClick: () -> Unit, action: String) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(50))
                .clickable {
                    onClick()
                }
                .background(DarwinTouchPrimaryBgLight)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = image),
                contentDescription = "",
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text = action,
            style = touchLabelBold,
            color = DarwinTouchNeutral1000,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}