package com.sqrtf.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.sqrtf.common.R;

/**
 * Created by roya on 2017/6/23.
 */

public class ProgressCoverView extends View {

    private float progress = 0f;
    private Paint colorPaint;
    private Paint colorSecondaryPaint;

    public ProgressCoverView(Context context) {
        this(context, null);
    }

    public ProgressCoverView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressCoverView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressCoverView);
        progress = a.getFloat(R.styleable.ProgressCoverView_progress, 0f);
        int color = a.getColor(R.styleable.ProgressCoverView_color, 0);
        int colorSecondary = a.getColor(R.styleable.ProgressCoverView_colorSecondary, 0);
        a.recycle();

        colorPaint = new Paint();
        colorPaint.setColor(color);

        colorSecondaryPaint = new Paint();
        colorSecondaryPaint.setColor(colorSecondary);
    }

    public void setColor(int color) {
        colorPaint = new Paint();
        colorPaint.setColor(color);
        postInvalidate();
    }

    public void setColorSecondary(int colorSecondary) {
        colorSecondaryPaint = new Paint();
        colorSecondaryPaint.setColor(colorSecondary);
        postInvalidate();
    }

    public void setProgress(float progress) {
        this.progress = progress;
        postInvalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int r1 = (int) (getMeasuredWidth() * progress);
        canvas.drawRect(0, 0, r1, getMeasuredHeight(), colorPaint);
        canvas.drawRect(r1, 0, getMeasuredWidth(), getMeasuredHeight(), colorSecondaryPaint);
    }
}
