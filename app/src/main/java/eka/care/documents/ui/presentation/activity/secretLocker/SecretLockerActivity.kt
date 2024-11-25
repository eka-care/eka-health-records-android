package eka.care.documents.ui.presentation.activity.secretLocker

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.orbi.eka.base.IAmCommon
import com.orbi.eka.base.OrbiLogger
import com.orbi.eka.base.utility.AnalyticsUtil
import com.orbi.eka.base.utility.EkaViewDebounceClickListener
import eka.care.doctor.features.documents.features.drive.databinding.ActivitySecretLockerWelcomeBinding
import eka.care.doctor.features.documents.features.drive.presentation.activity.secretLocker.SecretLockerSavePrivateKeyActivity
import eka.care.doctor.features.documents.features.drive.utility.WordsCollection
import org.json.JSONObject
import kotlin.random.Random

class SecretLockerActivity : AppCompatActivity(), Player.Listener {
    private lateinit var binding: ActivitySecretLockerWelcomeBinding
    private var isShowSecretLockerIntro = false
    private lateinit var exoPlayer: ExoPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecretLockerWelcomeBinding.inflate(layoutInflater)
        isShowSecretLockerIntro = intent.getBooleanExtra("show_secret_locker_intro", false)
        setContentView(binding.root)
        initUI()
    }

    override fun onStart() {
        super.onStart()
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
            val isIntroVideoShown =
                (application as IAmCommon).getValue("show_secret_locker_intro", false)
                    ?: false
            binding.apply {
                if (isShowSecretLockerIntro && !isIntroVideoShown) {
                    AnalyticsUtil.sendPageViewEvent(
                        this@SecretLockerActivity, "secret_locker_video"
                    )
                    (application as IAmCommon).setValue("show_secret_locker_intro", true)
                    binding.buffering.visibility = View.VISIBLE
                    initializePlayer()
                } else {
                    AnalyticsUtil.sendPageViewEvent(
                        this@SecretLockerActivity, "secret_locker_intro"
                    )
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
                    AnalyticsUtil.sendEvent(
                        this@SecretLockerActivity,
                        "secret_locker_intro_page_clicks",
                        eventParams
                    )
                    val params = JSONObject()
                    params.put("url", "https://www.eka.care/secret-locker")
                    (application as IAmCommon).navigateTo(
                        this@SecretLockerActivity, "web_view",
                                params
                    )
                }))

                btnGenerateKey.setOnClickListener {
                    val eventParams = JSONObject()
                    eventParams.put("type", "generate_key")
                    AnalyticsUtil.sendEvent(
                        this@SecretLockerActivity,
                        "secret_locker_intro_page_clicks",
                        eventParams
                    )
                    val word1 = WordsCollection.words.random(Random(Random.nextLong()))
                    val word2 = WordsCollection.words.random(Random(Random.nextLong()))
                    val word3 = WordsCollection.words.random(Random(Random.nextLong()))
                    val word4 = WordsCollection.words.random(Random(Random.nextLong()))
                    val word5 = WordsCollection.words.random(Random(Random.nextLong()))
                    val word6 = WordsCollection.words.random(Random(Random.nextLong()))
                    val word7 = WordsCollection.words.random(Random(Random.nextLong()))
                    val word8 = WordsCollection.words.random(Random(Random.nextLong()))
                    val word9 = WordsCollection.words.random(Random(Random.nextLong()))
                    val word10 = WordsCollection.words.random(Random(Random.nextLong()))
                    val word11 = WordsCollection.words.random(Random(Random.nextLong()))
                    val word12 = WordsCollection.words.random(Random(Random.nextLong()))
                    val twelveWords =
                        listOf(
                            word1,
                            word2,
                            word3,
                            word4,
                            word5,
                            word6,
                            word7,
                            word8,
                            word9,
                            word10,
                            word11,
                            word12
                        )
                    val randomTwelveWords = twelveWords.shuffled(Random(System.nanoTime()))
                    Log.d("AYUSHI", randomTwelveWords.toString())

                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        val intent = Intent(
                            this@SecretLockerActivity,
                            SecretLockerSavePrivateKeyActivity::class.java
                        )
                        intent.putStringArrayListExtra(
                            "secret_key_words",
                            randomTwelveWords as ArrayList<String>
                        )
                        startActivity(intent)
                    }, 1500)
                }
            }
        } catch (ex: Exception) {
            OrbiLogger.e("log", "Exception in SecretLockerWelcomeActivity::initUI() = ", ex)
        }
    }
}