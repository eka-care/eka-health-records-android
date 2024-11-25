package eka.care.doctor.features.documents.features.drive.presentation.activity.secretLocker

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.orbi.eka.base.OrbiLogger
import com.orbi.eka.base.utility.AnalyticsUtil
import com.orbi.eka.base.utility.EkaViewDebounceClickListener
import com.orbi.eka.user.UserSharedPref
import eka.care.doctor.features.documents.features.drive.databinding.ActivitySecretLockerSavePrivateKeyBinding
import eka.care.doctor.features.documents.features.drive.presentation.viewmodel.SecretLockerViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SecretLockerSavePrivateKeyActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecretLockerSavePrivateKeyBinding
    private val secretLockerViewModel: SecretLockerViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecretLockerSavePrivateKeyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val words = intent.getStringArrayListExtra("secret_key_words")
        AnalyticsUtil.sendPageViewEvent(this, "private_key_generation")
        if (words != null) {
            observeChanges()
            initUI(words)
        } else {
            finish()
        }
    }

    private fun observeChanges(){
        try{
            lifecycleScope.launch {
                secretLockerViewModel.loadingState.collect { isLoading ->
                    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                }
            }
            lifecycleScope.launch {
                secretLockerViewModel.secretLockerResp.collect { response ->
                    if (response?.ekaSecretLockerId != null) {
                        // Store the secret locker key ID to secured shared preferences
                        UserSharedPref(applicationContext).setValue(
                            "SECRET_LOCKER_KEY_ID",
                            response.ekaSecretLockerId!!
                        )

                        Log.d("AYUSHI", response.ekaSecretLockerId.toString())

                        // Retrieve the global QR code text from ViewModel
                        secretLockerViewModel.qrCodeText.collect { qrCodeText ->
                            if (!qrCodeText.isNullOrEmpty()) {
                                UserSharedPref(applicationContext)
                                    .setValue(UserSharedPref.SECRET_LOCKER_ENC_KEY, qrCodeText)

                                // Navigate to SecretLockerHomeActivity with a delay
                                Handler(Looper.getMainLooper()).postDelayed({
                                    val intent = Intent(
                                        applicationContext,
                                        SecretLockerHomeActivity::class.java
                                    ).apply {
                                        putExtra("secret_locker_creation_flow", true)
                                    }
                                    startActivity(intent)
                                }, 1500)
                            }
                        }
                    }
                }
            }

        }catch (e: Exception){

        }
    }

    private fun initUI(words: ArrayList<String>) {
        try {
            binding.apply {
                ivBack.setOnClickListener {
                    finish()
                }

                tvWord1.text = words[0]
                tvWord2.text = words[1]
                tvWord3.text = words[2]
                tvWord4.text = words[3]
                tvWord5.text = words[4]
                tvWord6.text = words[5]
                tvWord7.text = words[6]
                tvWord8.text = words[7]
                tvWord9.text = words[8]
                tvWord10.text = words[9]
                tvWord11.text = words[10]
                tvWord12.text = words[11]

                var qrCodeText = ""
                words.forEach { word ->
                    qrCodeText += "$word "
                }

                // read the context key from firebase
                val remoteConfig = FirebaseRemoteConfig.getInstance()
                remoteConfig.fetchAndActivate()
                val contextKey = remoteConfig.getString("secret_vault_context_key")

                // QR code will contain 12 words + string from the remote config
                qrCodeText += contextKey
                OrbiLogger.d("log", "qrCodeText = $qrCodeText")

                secretLockerViewModel.setQRCodeText(qrCodeText)

                val writer = QRCodeWriter()
                var qrCodeBitmap: Bitmap? = null
                try {
                    val bitMatrix = writer.encode(qrCodeText, BarcodeFormat.QR_CODE, 512, 512)
                    val width = bitMatrix.width
                    val height = bitMatrix.height
                    qrCodeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    for (x in 0 until width) {
                        for (y in 0 until height) {
                            qrCodeBitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                        }
                    }
                } catch (we: WriterException) {
                    OrbiLogger.e("log", "QRCodeWriterException = ", we)
                }

                btnProceed.setOnClickListener(EkaViewDebounceClickListener({
                    val eventParams = JSONObject()
                    eventParams.put("type", "proceed")
                    AnalyticsUtil.sendEvent(
                        this@SecretLockerSavePrivateKeyActivity,
                        "private_key_generation_clicks", eventParams
                    )

                    secretLockerViewModel.createSecretLocker()
                }))
            }
        } catch (ex: Exception) {
            OrbiLogger.e("log", "Exception in SecretLockerSavePrivateKeyActivity::initUI() = ", ex)
        }
    }
}