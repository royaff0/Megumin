package com.sqrtf.common.player

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.sqrtf.common.R
import com.sqrtf.common.StringUtil
import com.sqrtf.common.view.CheckableImageButton
import kotlin.properties.Delegates


/**
 * Created by roya on 2017/8/1.
 */

class MeguminExoPlayer : FrameLayout {

    val contentFrame by lazy { findViewById(R.id.arfl) as AspectRatioFrameLayout }
    val surfaceView by lazy { findViewById(R.id.surface_view) as SurfaceView }

    val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()
    val videoTrackSelectionFactory: TrackSelection.Factory = AdaptiveTrackSelection.Factory(bandwidthMeter)
    val trackSelector: TrackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
    val player: SimpleExoPlayer by lazy { ExoPlayerFactory.newSimpleInstance(context, trackSelector) }

    var progress: SeekBar? = null
    var position: TextView? = null
    var duration: TextView? = null
    var tracking = false

    private var mControllerCallback: ControllerCallback? = null
    private var mControllerView: View? = null
    private var mControllerVisibility by Delegates.observable(false, {
        prop, old, new ->
        mControllerCallback?.onControllerVisibilityChange(new)
    })

    constructor(context: Context) : this(context, null) {}

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.megumin_exo_player_view, this)
        player.setVideoListener(videoListener)
        player.setVideoSurfaceView(surfaceView)
    }

    val videoListener = object : SimpleExoPlayer.VideoListener {
        override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
            contentFrame.setAspectRatio(width.toFloat() / height)
        }

        override fun onRenderedFirstFrame() {

        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    fun setResizeMode(@AspectRatioFrameLayout.ResizeMode resizeMode: Int) {
        contentFrame.setResizeMode(resizeMode)
    }

    fun setSource(source: MediaSource) {
        player.prepare(source)
    }

    fun setPlayWhenReady(playWhenReady: Boolean) {
        player.playWhenReady = playWhenReady
        if (playWhenReady) {
            nextCheckProgress()
        } else {
            cancelCheckProgress()
        }
    }

    fun getPlayWhenReady(): Boolean {
        return player.playWhenReady
    }

    fun release() {
        cancelCheckProgress()
        player.release()
    }

    fun setControllerView(view: View?, views: ControllerViews?) {
        this.mControllerView = view
        if (view == null) {
            mControllerVisibility = false
            setOnClickListener(null)
            return
        }

        setOnClickListener {
            if (mControllerVisibility) {
                dismissController()
            } else {
                showController()
            }
        }

        views?.play?.setOnCheckedChangeListener {
            _, isChecked ->
            setPlayWhenReady(!isChecked)
        }


        views?.screen?.setOnCheckedChangeListener {
            _, isChecked ->
            setResizeMode(if (isChecked) AspectRatioFrameLayout.RESIZE_MODE_ZOOM else AspectRatioFrameLayout.RESIZE_MODE_FIT)
        }


        this.progress = views?.progressbar
        this.progress?.max = -1
        this.position = views?.timePosition
        this.duration = views?.timeDuration

        nextCheckProgress()
        views?.progressbar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                tracking = true
                position?.text = StringUtil.microsecondFormat(player.currentPosition)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                tracking = false
                seekTo((seekBar.progress * 1000).toLong())
                dismissDelay()
            }
        })

        showController()
    }

    fun seekTo(positionMs: Long) {
        if (player.duration <= 0) {
            return
        }

        cancelCheckProgress()
        player.seekTo(positionMs)
        nextCheckProgress(1500)
    }

    fun seekOffsetTo(offset: Int) {
        val currentPosition = player.currentPosition
        if ((offset + currentPosition) > player.duration) {
            seekTo(player.duration)
        } else if ((offset + currentPosition) <= 0) {
            seekTo(1)
        } else {
            seekTo(offset + currentPosition)
        }
    }

    private val checkProgress = {
        if (!tracking && player.duration > 0) {
            this.progress?.max = (player.duration / 1000).toInt()
            this.progress?.secondaryProgress = (player.bufferedPosition / 1000).toInt()
            this.progress?.progress = (player.currentPosition / 1000).toInt()
            this.duration?.text = StringUtil.microsecondFormat(player.duration)
            this.position?.text = StringUtil.microsecondFormat(player.currentPosition)
        }

        nextCheckProgress()
    }

    fun nextCheckProgress(delay: Long = 1000) {
        this.progress?.postDelayed(checkProgress, delay)
    }

    fun cancelCheckProgress() {
        this.progress?.removeCallbacks(checkProgress)
    }

    fun getControllerVisibility(): Boolean {
        return mControllerVisibility
    }

    fun setControllerCallback(callback: ControllerCallback) {
        this.mControllerCallback = callback
        mControllerCallback?.onControllerVisibilityChange(mControllerVisibility)
    }

    private val dismissDelayed = Runnable { dismissController() }

    private fun dismissDelay() {
        cancelDismissDelay()
        postDelayed(dismissDelayed, 5000)
    }

    private fun cancelDismissDelay() {
        removeCallbacks(dismissDelayed)
    }

    fun showController() {
        cancelDismissDelay()
        if (mControllerView != null) {
            mControllerVisibility = true
            mControllerView!!.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .withStartAction {
                        mControllerView?.visibility = View.VISIBLE
                    }
                    .start()
            dismissDelay()
        }
    }

    fun dismissController() {
        if (!tracking && mControllerView != null) {
            mControllerVisibility = false
            mControllerView!!.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        mControllerView?.visibility = View.INVISIBLE
                    }
                    .start()
        }
    }

    data class ControllerViews(val play: CheckableImageButton?,
                               val screen: CheckableImageButton?,
                               val progressbar: SeekBar?,
                               val timePosition: TextView?,
                               val timeDuration: TextView?)

    interface ControllerCallback {
        fun onControllerVisibilityChange(visible: Boolean)
    }
}
