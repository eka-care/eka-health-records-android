package eka.care.documents.ui.presentation.activity.secretLocker

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import eka.care.documents.databinding.ActivitySecretLockerWelcomeBinding
import eka.care.documents.ui.bgSecretLocker
import eka.care.documents.ui.iconPrimary
import eka.care.documents.ui.iconSecondary
import eka.care.documents.ui.presentation.activity.DocumentActivity
import eka.care.documents.ui.presentation.activity.MedicalRecordParams
import eka.care.documents.ui.presentation.activity.RecordsViewModelFactory
import eka.care.documents.ui.presentation.model.RecordParamsModel
import eka.care.documents.ui.presentation.state.GetRecordsState
import eka.care.documents.ui.presentation.viewmodel.RecordsViewModel
import eka.care.documents.ui.touchHeadlineBold
import eka.care.documents.ui.touchTitle4Bold
import eka.care.documents.ui.utility.EkaViewDebounceClickListener
import org.json.JSONObject

class SecretLockerActivity : AppCompatActivity(), Player.Listener {
    private lateinit var viewModel: RecordsViewModel
    private lateinit var binding: ActivitySecretLockerWelcomeBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var isShowSecretLockerIntro = false
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var params: RecordParamsModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecretLockerWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isShowSecretLockerIntro = intent.getBooleanExtra("show_secret_locker_intro", false)
        sharedPreferences = getSharedPreferences("secret_locker_prefs", Context.MODE_PRIVATE)

        val application = applicationContext as Application
        viewModel = ViewModelProvider(
            this,
            RecordsViewModelFactory(application)
        ).get(RecordsViewModel::class.java)

        params = RecordParamsModel(
            patientId = intent.getStringExtra(MedicalRecordParams.PATIENT_ID.key) ?: "",
            doctorId = intent.getStringExtra(MedicalRecordParams.DOCTOR_ID.key) ?: "",
            name = intent.getStringExtra(MedicalRecordParams.PATIENT_NAME.key),
            uuid = intent.getStringExtra(MedicalRecordParams.PATIENT_UUID.key) ?: "",
            age = intent.getIntExtra(MedicalRecordParams.PATIENT_AGE.key, -1),
            gender = intent.getStringExtra(MedicalRecordParams.PATIENT_GENDER.key),
        )

        initUI()
    }

    private fun navigateToNextScreen() {
        val intent = Intent(
            this@SecretLockerActivity,
            DocumentActivity::class.java
        ).apply {
            putExtra(MedicalRecordParams.FROM_SECRET_LOCKER.key, true)
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

    override fun onStart() {
        super.onStart()
        window.statusBarColor = bgSecretLocker.toArgb()
        if (::exoPlayer.isInitialized) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            exoPlayer.play()
        }
    }

    override fun onStop() {
        super.onStop()
        if (::exoPlayer.isInitialized) {
            exoPlayer.pause()
        }
    }

    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(applicationContext).build()
        binding.introVideo.player = exoPlayer
        exoPlayer.apply {
            playWhenReady = true
            seekTo(0, 0)
            addListener(this@SecretLockerActivity)
        }

        exoPlayer.addMediaItem(
            MediaItem.fromUri(
                FirebaseRemoteConfig.getInstance()
                    .getString("secret_vault_intro_video_url")
            )
        )
        exoPlayer.prepare()
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        when (playbackState) {
            Player.STATE_BUFFERING -> {
                binding.buffering.visibility = View.VISIBLE
            }

            Player.STATE_READY -> {
                binding.buffering.visibility = View.GONE
            }

            Player.STATE_ENDED -> {
                animateLayoutOnVideoComplete()
            }

            else -> {}
        }
    }

    private fun animateLayoutOnVideoComplete() {
        binding.clLockerWelcomeRoot.visibility = View.VISIBLE
        val introVideoAnimator =
            ObjectAnimator.ofFloat(binding.introVideo, View.ALPHA, 1f, 0f).apply {
                duration = 650
            }

        val welcomeLayoutAnimator =
            ObjectAnimator.ofFloat(binding.clLockerWelcomeRoot, View.ALPHA, 0f, 1f).apply {
                duration = 250
            }

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(introVideoAnimator, welcomeLayoutAnimator)
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {}
            override fun onAnimationEnd(p0: Animator) {
                binding.introVideo.visibility = View.GONE
            }

            override fun onAnimationCancel(p0: Animator) {}
            override fun onAnimationRepeat(p0: Animator) {}
        })
        animatorSet.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::exoPlayer.isInitialized && exoPlayer.isPlaying) {
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    private fun initUI() {
        try {
            binding.apply {
                if (isShowSecretLockerIntro) {
                    binding.buffering.visibility = View.VISIBLE
                    initializePlayer()
                } else {
                    introVideo.visibility = View.GONE
                    clLockerWelcomeRoot.visibility = View.VISIBLE
                    clLockerWelcomeRoot.alpha = 1f

                    sharedPreferences.edit().putBoolean("is_intro_shown", true).apply()
                }

                ivBack.setOnClickListener {
                    finish()
                }

                cvShowRecords.setContent {
                    val encryptedRecordsState= viewModel.getEncryptedRecordsState.collectAsState()
                    val count = (encryptedRecordsState as? GetRecordsState.Success)?.resp ?: emptyList()
                    showMedicalRecordsCount(count = count.size)
                }

                ivInfo.setOnClickListener(EkaViewDebounceClickListener({
                    val eventParams = JSONObject()
                    eventParams.put("type", "faq")
                    val params = JSONObject()
                    params.put("url", "https://www.eka.care/secret-locker")
                }))

                btnGenerateKey.setOnClickListener {
                    navigateToNextScreen()
                }
            }
        } catch (ex: Exception) {

        }
    }
}

@Composable
fun showMedicalRecordsCount(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(iconPrimary)
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "$count", style = touchTitle4Bold, color = iconSecondary)
        }
        Text(
            text = "You have $count Encrypted Records in your Secret Locker",
            maxLines = 2,
            color = Color.White,
            modifier = Modifier.padding(start = 4.dp).fillMaxWidth()
        )
    }
}