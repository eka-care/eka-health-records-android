package eka.care.documents.ui.presentation.activity.secretLocker

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import eka.care.documents.databinding.ActivitySecretLockerSavePrivateKeyBinding
import eka.care.documents.ui.Gray200
import eka.care.documents.ui.bgSecretLocker
import eka.care.documents.ui.presentation.activity.DocumentActivity
import eka.care.documents.ui.presentation.activity.MedicalRecordParams
import eka.care.documents.ui.presentation.activity.RecordsViewModelFactory
import eka.care.documents.ui.presentation.model.RecordParamsModel
import eka.care.documents.ui.presentation.viewmodel.RecordsViewModel

class SecretLockerSavePrivateKeyActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecretLockerSavePrivateKeyBinding
    private lateinit var viewModel: RecordsViewModel
    private var isLoginScreen by mutableStateOf(false)
    private lateinit var sharedPreferences: SharedPreferences
    // to check if the user is logged in or not
    private val enteredPasswordState = mutableStateOf("")
    // password generated
    private val passwordKey: String = "secret_locker_password"
    // to store ekaSecretLockerId

    private lateinit var params: RecordParamsModel
    override fun onStart() {
        super.onStart()
        window.statusBarColor = bgSecretLocker.toArgb()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecretLockerSavePrivateKeyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        params = RecordParamsModel(
            patientId = intent.getStringExtra(MedicalRecordParams.PATIENT_ID.key) ?: "",
            doctorId = intent.getStringExtra(MedicalRecordParams.DOCTOR_ID.key) ?: "",
            name = intent.getStringExtra(MedicalRecordParams.PATIENT_NAME.key),
            uuid = intent.getStringExtra(MedicalRecordParams.PATIENT_UUID.key) ?: "",
            age = intent.getIntExtra(MedicalRecordParams.PATIENT_AGE.key, -1),
            gender = intent.getStringExtra(MedicalRecordParams.PATIENT_GENDER.key)
        )

        val application = applicationContext as Application
        viewModel = ViewModelProvider(
            this,
            RecordsViewModelFactory(application)
        ).get(RecordsViewModel::class.java)

        sharedPreferences = getSharedPreferences("secret_locker", Context.MODE_PRIVATE)
        isLoginScreen = sharedPreferences.contains(passwordKey)
        initUI()
    }

    private fun initUI() {
        try {
            binding.apply {
                ivBack.setOnClickListener {
                    finish()
                }

                composeView.setContent {
                    GeneratePassword()
                }

                btnProceed.setOnClickListener {
                    onClickProceed()
                }
            }
        } catch (ex: Exception) {
            Log.e("Error", "Exception in initUI: $ex")
        }
    }

    private fun onClickProceed() {
        val enteredPassword = enteredPasswordState.value
        if (enteredPassword.isBlank()) {
            Toast.makeText(
                this@SecretLockerSavePrivateKeyActivity,
                "Password cannot be empty!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (isLoginScreen) {
            val storedPassword = sharedPreferences.getString(passwordKey, null)
            if (storedPassword == enteredPassword) {
                redirectToDocumentActivity()
            } else {
                Toast.makeText(
                    this@SecretLockerSavePrivateKeyActivity,
                    "Incorrect password!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            if (sharedPreferences.contains(passwordKey)) {
                Toast.makeText(
                    this@SecretLockerSavePrivateKeyActivity,
                    "Password already exists. Please login.",
                    Toast.LENGTH_SHORT
                ).show()
                isLoginScreen = true
            } else {
                sharedPreferences.edit()
                    .putString(passwordKey, enteredPassword)
                    .apply()
                Toast.makeText(
                    this@SecretLockerSavePrivateKeyActivity,
                    "Creating your Secret Locker…!",
                    Toast.LENGTH_SHORT
                ).show()
                redirectToDocumentActivity()
            }
        }
    }

    private fun redirectToDocumentActivity() {
        val intent = Intent(
            this@SecretLockerSavePrivateKeyActivity,
            DocumentActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MedicalRecordParams.FROM_SECRET_LOCKER.key, true)
            putExtra(MedicalRecordParams.PATIENT_ID.key, params.patientId)
            putExtra(MedicalRecordParams.DOCTOR_ID.key, params.doctorId)
            putExtra(MedicalRecordParams.PATIENT_UUID.key, params.uuid)
            putExtra(MedicalRecordParams.PATIENT_NAME.key, params.name)
            putExtra(MedicalRecordParams.PATIENT_GENDER.key, params.gender)
            putExtra(MedicalRecordParams.PATIENT_AGE.key, params.age)
            putExtra(MedicalRecordParams.PASSWORD.key, enteredPasswordState.value)
        }
        startActivity(intent)
        finish()
    }

    @Composable
    fun GeneratePassword() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(targetState = isLoginScreen, label = "") { loginScreen ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    val placeholderText = if (loginScreen) "Enter Password" else "Create Password"
                    var text by rememberSaveable { mutableStateOf("") }
                    var passwordVisible by rememberSaveable { mutableStateOf(false) }
                    OutlinedTextField(
                        value = text,
                        maxLines = 1,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                onClickProceed()
                            }
                        ),
                        trailingIcon = {
                            val image = if (passwordVisible)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff

                            val description = if (passwordVisible) "Hide password" else "Show password"

                            IconButton(onClick = {passwordVisible = !passwordVisible}){
                                Icon(imageVector  = image, description, tint = Gray200)
                            }
                        },
                        onValueChange = {
                            text = it
                            enteredPasswordState.value = text
                        },
                        placeholder = { Text(text = placeholderText, color = Gray200) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(8.dp)
                    )

                    val bottomText = if (loginScreen) "New User?" else "Already have an account?"
                    Text(
                        text = bottomText,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clickable {
                                enteredPasswordState.value = ""
                                isLoginScreen = !isLoginScreen
                            }
                    )
                }
            }
        }
    }
}
