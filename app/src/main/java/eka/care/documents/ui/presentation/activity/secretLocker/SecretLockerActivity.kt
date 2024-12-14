package eka.care.documents.ui.presentation.activity.secretLocker

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.toArgb
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import eka.care.documents.databinding.ActivitySecretLockerWelcomeBinding
import eka.care.documents.ui.bgSecretLocker
import eka.care.documents.ui.presentation.activity.MedicalRecordParams
import eka.care.documents.ui.presentation.model.RecordParamsModel
import eka.care.documents.ui.utility.EkaViewDebounceClickListener
import org.json.JSONObject

class SecretLockerActivity : AppCompatActivity(), Player.Listener {
    private lateinit var binding: ActivitySecretLockerWelcomeBinding
    private var isShowSecretLockerIntro = false
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var params: RecordParamsModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecretLockerWelcomeBinding.inflate(layoutInflater)
        isShowSecretLockerIntro = intent.getBooleanExtra("show_secret_locker_intro", false)
        setContentView(binding.root)
        params =  RecordParamsModel(
            patientId = intent.getStringExtra(MedicalRecordParams.PATIENT_ID.key) ?: "",
            doctorId = intent.getStringExtra(MedicalRecordParams.DOCTOR_ID.key) ?: "",
            name = intent.getStringExtra(MedicalRecordParams.PATIENT_NAME.key),
            uuid = intent.getStringExtra(MedicalRecordParams.PATIENT_UUID.key) ?: "",
            age = intent.getIntExtra(MedicalRecordParams.PATIENT_AGE.key, -1),
            gender = intent.getStringExtra(MedicalRecordParams.PATIENT_GENDER.key),
        )
        initUI()
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
            val isIntroVideoShown = false
            binding.apply {
                if (isShowSecretLockerIntro && !isIntroVideoShown) {
//                    (application as IAmCommon).setValue("show_secret_locker_intro", true)
                    binding.buffering.visibility = View.VISIBLE
                    initializePlayer()
                } else {
                    introVideo.visibility = View.GONE
                    clLockerWelcomeRoot.visibility = View.VISIBLE
                    clLockerWelcomeRoot.alpha = 1f
                }

                ivBack.setOnClickListener {
                    finish()
                }

                ivInfo.setOnClickListener(EkaViewDebounceClickListener({
                    val eventParams = JSONObject()
                    eventParams.put("type", "faq")
                    val params = JSONObject()
                    params.put("url", "https://www.eka.care/secret-locker")
                }))

                btnGenerateKey.setOnClickListener {
                    val intent = Intent(
                        this@SecretLockerActivity,
                        SecretLockerSavePrivateKeyActivity::class.java
                    ).apply {
                        putExtra(MedicalRecordParams.PATIENT_ID.key, params.patientId)
                        putExtra(MedicalRecordParams.DOCTOR_ID.key, params.doctorId)
                        putExtra(MedicalRecordParams.PATIENT_UUID.key, params.uuid)
                        putExtra(MedicalRecordParams.PATIENT_NAME.key, params.name)
                        putExtra(MedicalRecordParams.PATIENT_GENDER.key, params.gender)
                        putExtra(MedicalRecordParams.PATIENT_AGE.key, params.age)
                    }
                    startActivity(intent)
                }
            }
        } catch (ex: Exception) {

        }
    }
}