package com.sqrtf.megumin

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import com.sqrtf.common.activity.BaseActivity
import com.sqrtf.common.api.ApiHelper
import java.text.SimpleDateFormat

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

    private var draging = false
    var breakPoint = -1
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }

    var errorTextView: TextView? = null

    var videoReady = false
    var fixedUrl = ""

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
        val playButton = findViewById(R.id.playButton) as CheckBox

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mContentView!!.setOnClickListener { toggle() }

        mVisible = true

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        delayedHide(1000)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun outputMessage(msg: String) {
        errorTextView!!.text = errorTextView!!.text.toString() + msg + "\n"
    }

    fun clearMessage() {
        errorTextView!!.text = null
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
