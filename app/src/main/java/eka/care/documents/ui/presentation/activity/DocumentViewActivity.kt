package eka.care.documents.ui.presentation.activity

//import com.example.reader.PdfReaderManager
import androidx.activity.ComponentActivity

class DocumentViewActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val viewModel: DocumentPreviewViewModel by viewModels()
//        val pdfManager = PdfReaderManager(this)
//
//        try {
//            val localId = intent.getStringExtra("local_id")
//            val docId = intent.getStringExtra("doc_id")
//            val userId = intent.getStringExtra("user_id")
//            viewModel.getDocument(
//                userId = userId ?: "",
//                docId = docId ?: "",
//                localId = localId ?: ""
//            )
//        } catch (_: Exception) {
//        }
//
//        setContent {
//            val state by viewModel.document.collectAsState()
//            Content(state, pdfManager)
//        }
//    }
//
//    @OptIn(ExperimentalMaterialApi::class)
//    @Composable
//    private fun Content(
//        state: DocumentPreviewState,
//        pdfManager: PdfReaderManager
//    ) {
//        val context = LocalContext.current
//        var selectedUri by remember { mutableStateOf<Uri?>(null) }
//
//        ModalBottomSheetLayout(sheetContent = {}) {
//            Scaffold(
//                topBar = {
//                    TopAppBarSmall(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .background(BgWhite),
//                        title = "Document",
//                        leading = R.drawable.ic_back_arrow,
//                        onLeadingClick = { (context as? Activity)?.finish() },
//                        trailingIcon1 = R.drawable.ic_download_regular,
//                        onTrailingIcon1Click = {
//                            handleFileDownload(
//                                state = state,
//                                context = context,
//                                selectedUri = selectedUri
//                            )
//                        }
//                    )
//                },
//                content = {
//                    when (state) {
//                        is DocumentPreviewState.Loading -> LoadingState()
//                        is DocumentPreviewState.Error -> ErrorState(state.message)
//                        is DocumentPreviewState.Success -> {
//                            DocumentSuccessState(
//                                state = state,
//                                paddingValues = it,
//                                pdfManager = pdfManager,
//                                onUriSelected = { uri -> selectedUri = uri }
//                            )
//                        }
//                    }
//                }
//            )
//        }
//    }
}