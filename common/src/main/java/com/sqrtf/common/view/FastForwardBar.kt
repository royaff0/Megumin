package com.sqrtf.common.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.sqrtf.common.DisplayUtil

/**
 * Created by roya on 2017/8/9.
 */
class FastForwardBar(context: Context?,
                     attrs: AttributeSet?,
                     defStyleAttr: Int) : View(context, attrs, defStyleAttr) {

    constructor(context: Context?,
                attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?) : this(context, null)

    private val paint = Paint()

    var callback: FastForwardEventCallback? = null

    private var startPosition = Pair(0f, 0f)
    private var endPosition = Pair(0f, 0f)
    private var dragging = false

    init {
        paint.isAntiAlias = true
        paint.color = Color.WHITE
        paint.alpha = 0xE2
        paint.textSize = DisplayUtil.dp2px(resources, 24).toFloat()
//        paint.setShadowLayer(10.0f, 0.0f, 2.0f, 0xA0000000.toInt())
    }

    private fun isValid(): Boolean {
        return Math.abs(startPosition.first - endPosition.first) > DisplayUtil.dp2px(resources, 4)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startPosition = Pair(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                endPosition = Pair(event.x, event.y)
                if (isValid()) {
                    dragging = true
                }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                val range = calcRange(endPosition.first - startPosition.first)
                if (dragging && (range >= 1 || range <= -1)) {
                    callback?.onFastForward(range)
                } else if (!dragging) {
                    callback?.onClick(this)
                }
                startPosition = Pair(0f, 0f)
                endPosition = Pair(0f, 0f)
                invalidate()
                dragging = false
            }
        }

        return true
    }

    private fun calcRange(distance: Float): Int {
        return DisplayUtil.px2dp(resources, distance) / 6
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isValid() || isInEditMode) {
            return
        }

        val distance = endPosition.first - startPosition.first

        fun toRangeDisplay(): String {
            val range = calcRange(distance)
            if (range >= 0) {
                return "+" + range + "s"
            } else {
                return "" + range + "s"
            }
        }

        canvas.drawRect(
                if (distance > 0) startPosition.first else endPosition.first,
                startPosition.second - DisplayUtil.dp2px(resources, 2),
                if (distance > 0) endPosition.first else startPosition.first,
                startPosition.second + DisplayUtil.dp2px(resources, 2),
                paint)

        canvas.drawText(
                toRangeDisplay(),
                (startPosition.first + endPosition.first) / 2 - DisplayUtil.dp2px(resources, 8),
                startPosition.second - DisplayUtil.dp2px(resources, 6),
                paint)
    }

    interface FastForwardEventCallback {
        fun onFastForward(range: Int)
        fun onClick(view: View)
    }
}