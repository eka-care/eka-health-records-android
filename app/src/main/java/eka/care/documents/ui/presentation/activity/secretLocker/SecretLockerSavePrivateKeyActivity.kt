package eka.care.documents.ui.presentation.activity.secretLocker

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
import androidx.compose.ui.unit.dp
import eka.care.documents.databinding.ActivitySecretLockerSavePrivateKeyBinding
import eka.care.documents.ui.Gray200
import eka.care.documents.ui.bgSecretLocker
import eka.care.documents.ui.presentation.activity.DocumentActivity
import eka.care.documents.ui.presentation.activity.MedicalRecordParams
import eka.care.documents.ui.presentation.model.RecordParamsModel

class SecretLockerSavePrivateKeyActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecretLockerSavePrivateKeyBinding
    private var isLoginScreen by mutableStateOf(false)
    private lateinit var sharedPreferences: SharedPreferences
    private val enteredPasswordState = mutableStateOf("")
    private val passwordKey : String? = null
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
                    val enteredPassword = enteredPasswordState.value
                    if (enteredPassword.isBlank()) {
                        Toast.makeText(
                            this@SecretLockerSavePrivateKeyActivity,
                            "Password cannot be empty!",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
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
                                "Password created successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            redirectToDocumentActivity()
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("Error", "Exception in initUI: $ex")
        }
    }

    private fun redirectToDocumentActivity() {
        val intent = Intent(
            this@SecretLockerSavePrivateKeyActivity,
            DocumentActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MedicalRecordParams.FROM_SECRET_LOCKER.key, true)
            putExtra(MedicalRecordParams.PASSWORD.key, passwordKey)
            putExtra(MedicalRecordParams.PATIENT_ID.key, params.patientId)
            putExtra(MedicalRecordParams.DOCTOR_ID.key, params.doctorId)
            putExtra(MedicalRecordParams.PATIENT_UUID.key, params.uuid)
            putExtra(MedicalRecordParams.PATIENT_NAME.key, params.name)
            putExtra(MedicalRecordParams.PATIENT_GENDER.key, params.gender)
            putExtra(MedicalRecordParams.PATIENT_AGE.key, params.age)
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
                        trailingIcon = {
                            val image = if (passwordVisible)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff

                            // Please provide localized description for accessibility services
                            val description = if (passwordVisible) "Hide password" else "Show password"

                            IconButton(onClick = {passwordVisible = !passwordVisible}){
                                Icon(imageVector  = image, description)
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
