package com.sqrtf.megumin

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import com.sqrtf.common.activity.BaseActivity
import com.sqrtf.common.api.ApiHelper
import io.reactivex.Observable
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class PlayerActivity : BaseActivity() {
    private val mHideHandler = Handler()

    @SuppressLint("SimpleDateFormat")
    private val format = SimpleDateFormat("mm:ss")

    private var mContentView: SurfaceView? = null
    private val mHidePart2Runnable = Runnable {
        mContentView!!.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    private var mControlsView: View? = null
    private val mShowPart2Runnable = Runnable {
        supportActionBar?.show()
        mControlsView!!.visibility = View.VISIBLE
    }

    private var progress: SeekBar? = null
    private var lastTime: TextView? = null

    @SuppressLint("SetTextI18n")
    private val checkProgress = Runnable {
        if (player.duration <= 0) return@Runnable
        progress!!.max = player.duration.toInt()
        progress!!.progress = player.currentPosition.toInt()

        if (!draging) {
            lastTime!!.text = "-" + format.format(player.duration - player.currentPosition)
        }
        startCheck()
    }

    private var draging = false
    var breakPoint = -1
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }

    var errorTextView: TextView? = null

    var fixedUrl = ""
    val player = IjkMediaPlayer()
//    val player = AndroidMediaPlayer()


    companion object {
        fun intent(context: Context, url: String): Intent {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra(INTENT_KEY_URL, url)
            return intent
        }

        private val INTENT_KEY_URL = "INTENT_KEY_URL"

        private val AUTO_HIDE_DELAY_MILLIS = 3000
        private val AUTO_HIDE_DELAY_MILLIS_AFTER_DRAG = 5000

        private val UI_ANIMATION_DELAY = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_player)

        val url = intent.getStringExtra(INTENT_KEY_URL)
        if (TextUtils.isEmpty(url)) {
            throw IllegalArgumentException("Required url")
        }

        fixedUrl = ApiHelper.fixHttpUrl(url)
        Log.i(this.localClassName, "playing:" + fixedUrl)

        errorTextView = findViewById(R.id.error_message) as TextView
        mControlsView = findViewById(R.id.fullscreen_content_controls)
        mContentView = findViewById(R.id.fullscreen_content) as SurfaceView
        progress = findViewById(R.id.progress) as SeekBar?
        lastTime = findViewById(R.id.last_time) as TextView?

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        player.setOnErrorListener { mp, what, extra ->
            outputMessage("OnError what=$what,extra=$extra")

            Observable.timer(3, TimeUnit.SECONDS)
                    .withLifecycle()
                    .onlyRunOneInstance(taskId = 1)
                    .subscribe({
                        resetVideo()
                    }, {
                        outputMessage("Trying restart but: " + it.toString())
                    })

            false
        }

        mContentView!!.setOnClickListener { toggle() }
        mContentView!!.holder.addCallback(shCallback)

        mVisible = true

        progress!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                lastTime!!.text = "-" + format.format(seekBar!!.max - seekBar.progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                draging = true
                disableAutoHide()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                draging = false
                delayedHide(AUTO_HIDE_DELAY_MILLIS_AFTER_DRAG)
                stopCheck()
                player.seekTo(seekBar!!.progress.toLong())
            }
        })

        player.setOnBufferingUpdateListener { _, percent ->
            //            progress?.secondaryProgress = ((percent.toFloat() / (progress?.max ?: 100).toFloat()) * 100f).toInt()
            progress?.secondaryProgress = percent
        }

        player.setOnSeekCompleteListener {
            startCheck()
        }

        player.setOnPreparedListener {
            startPlay(player)
            if (breakPoint > 0) {
                player.seekTo(breakPoint.toLong())
                breakPoint = -1
            }
        }

        outputMessage("Player is " + player::class.java.simpleName)
        initVideo()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        delayedHide(1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    fun outputMessage(msg: String) {
        errorTextView!!.text = errorTextView!!.text.toString() + msg + "\n"
    }

    fun clearMessage() {
        errorTextView!!.text = null
    }

    fun startCheck() {
        mHideHandler.postDelayed(checkProgress, 1000)
    }

    fun stopCheck() {
        mHideHandler.removeCallbacks(checkProgress)
    }

    var sh: SurfaceHolder? = null
    val shCallback = object : SurfaceHolder.Callback {
        override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {}
        override fun surfaceDestroyed(holder: SurfaceHolder?) {
            player.setDisplay(null)
            stopPlay(player)
        }

        override fun surfaceCreated(holder: SurfaceHolder?) {
            sh = holder
            player.setDisplay(holder)
        }
    }

    fun resetVideo() {
        breakPoint = progress?.progress ?: -1
        player.reset()
        player.setDisplay(sh)
        initVideo()
    }

    fun initVideo() {
        player.dataSource = fixedUrl
        player.prepareAsync()
        outputMessage("Preparing")
    }

    fun startPlay(mp: IMediaPlayer) {
        stopCheck()
        startCheck()
        mp.start()
        clearMessage()
    }

    fun pausePlay(mp: IMediaPlayer) {
        stopCheck()
        mp.pause()
    }

    fun stopPlay(mp: IMediaPlayer) {
        stopCheck()
        mp.stop()
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        mControlsView!!.visibility = View.GONE
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        mContentView!!.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())

        delayedHide(AUTO_HIDE_DELAY_MILLIS)
    }

    private fun disableAutoHide() {
        mHideHandler.removeCallbacks(mHideRunnable)
    }

    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }
}
