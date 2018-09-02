package com.sqrtf.megumin

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View

class FadingBehavior : AppBarLayout.Behavior {
    constructor() : super()
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        context.resources?.getDimensionPixelSize(R.dimen.top_appbar_elevation)?.toFloat()?.let {
            elevationPx = it
        }
    }

    private var elevationPx = 9f
    private var overlapped = false

    private var lastElevationPx = -1f

    override fun onMeasureChild(parent: CoordinatorLayout?, child: AppBarLayout?, parentWidthMeasureSpec: Int, widthUsed: Int, parentHeightMeasureSpec: Int, heightUsed: Int): Boolean {
        child?.removeOnOffsetChangedListener(onOffsetChangedListener)
        child?.addOnOffsetChangedListener(onOffsetChangedListener)
        return super.onMeasureChild(parent, child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed)
    }

    private val onOffsetChangedListener: (AppBarLayout, Int) -> Unit = { appBarLayout: AppBarLayout, offset: Int ->
        if ((appBarLayout.totalScrollRange + offset) != 0 && overlapped) {
            setElevation(appBarLayout, elevationPx)
        } else if ((appBarLayout.totalScrollRange + offset) == 0) {
            setElevation(appBarLayout, 0f)
        }
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout?, child: AppBarLayout, target: View?, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
        overlapped = dyUnconsumed >= 0
        if (!overlapped) {
            setElevation(child, 0f)
        }
    }

    private fun setElevation(view: AppBarLayout, e: Float) {
        if (lastElevationPx != e) {
            lastElevationPx = e
            ViewCompat.setElevation(view, e)
        }
    }

}
