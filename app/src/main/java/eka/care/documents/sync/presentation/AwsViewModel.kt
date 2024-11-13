package eka.care.documents.sync.presentation

import android.app.Application
import android.webkit.MimeTypeMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import eka.care.documents.sync.data.repository.AwsRepository
import java.io.File
import java.util.Locale

class AwsViewModel(val app: Application) : AndroidViewModel(app) {

    private val repository = AwsRepository()

    private val _documentState = MutableLiveData<Pair<String, File>>()
    var documentState = _documentState

    private val _showLoader = MutableLiveData<Boolean>()
    var showLoader = _showLoader

    private val _error = MutableLiveData<String>()
    var error = _error

    private val _uploadTimeLiveData = MutableLiveData<String>()
    val uploadTimeLiveData: LiveData<String> = _uploadTimeLiveData


    fun uploadFile(files: List<File>, isMultiFile: Boolean = false, isEncrypted: Boolean = false) {
        // if isMultiFile is true, we will be uploading multiple files as a single document,
        // else each file will be uploaded as a separate document

//        viewModelScope.launch {
//            try {
//                val fileContentList = mutableListOf<FileType>()
//                files.forEach {
//                    fileContentList.add(FileType(contentType = it.getMimeType(), fileSize = it.length()))
//                }
//                val uploadInitResponse = repository.fileUploadInit(files = fileContentList, isMultiFile, isEncrypted, "", "")
//                if (uploadInitResponse?.error == true) {
//                    _error.postValue(uploadInitResponse.message ?: "Something went wrong")
//                    return@launch
//                }
//
//                uploadInitResponse?.uploadTime?.let {
//                    _uploadTimeLiveData.value = it
//                }
//
//
//                if (isMultiFile) {
//                    val batchResponse = uploadInitResponse?.batchResponse?.get(0)
//                    if (batchResponse == null) {
//                        _error.postValue("Something went wrong")
//                        return@launch
//                    }
//
//                    val response = repository.uploadFile(batch = batchResponse, fileList = files)
//                    if (response != null) {
//                        if (response.error) {
//                            _error.postValue(response.message ?: "Something went wrong")
//                        } else {
//                            _documentState.postValue(Pair(response.documentId as String, files[0]))
//                        }
//                    } else {
//                        _error.postValue("Something went wrong")
//                    }
//                } else {
//                    files.forEachIndexed { index, file ->
//                        val batchResponse = uploadInitResponse?.batchResponse?.get(index)
//                        if (batchResponse == null) {
//                            _error.postValue("Something went wrong")
//                            return@launch
//                        }
//
//                        val response = repository.uploadFile(file = file, batch = batchResponse)
//                        if (response != null) {
//                            if (response.error) {
//                                _error.postValue(response.message ?: "Something went wrong")
//                            } else {
//                                _documentState.postValue(Pair(response.documentId as String, file))
//                            }
//                        } else {
//                            _error.postValue("Something went wrong")
//                        }
//                    }
//                }
//            } catch (ex: Exception) {
//                _error.postValue("No Internet")
//            }
//        }
    }

    private fun File.getMimeType(fallback: String = "application/pdf"): String {
        return MimeTypeMap.getFileExtensionFromUrl(toString())
            ?.run {
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(lowercase(Locale.getDefault()))
            }
            ?: fallback
    }
}