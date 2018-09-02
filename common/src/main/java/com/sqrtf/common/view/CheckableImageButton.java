package com.sqrtf.common.view;

import android.content.Context;
import android.support.annotation.RestrictTo;
import android.support.v4.view.AccessibilityDelegateCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Checkable;

@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public class CheckableImageButton extends AppCompatImageButton implements Checkable {
    private static final int[] DRAWABLE_STATE_CHECKED = new int[]{16842912};
    private boolean mChecked;

    private OnCheckedChangeListener mOnCheckedChangeListener;

    public CheckableImageButton(Context context) {
        this(context, (AttributeSet) null);
    }

    public CheckableImageButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.support.v7.appcompat.R.attr.imageButtonStyle);
    }

    public CheckableImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ViewCompat.setAccessibilityDelegate(this, new AccessibilityDelegateCompat() {
            public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
                super.onInitializeAccessibilityEvent(host, event);
                event.setChecked(CheckableImageButton.this.isChecked());
            }

            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setCheckable(true);
                info.setChecked(CheckableImageButton.this.isChecked());
            }
        });

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setChecked(!mChecked);
            }
        });
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    public void setChecked(boolean checked) {
        if (this.mChecked != checked) {
            this.mChecked = checked;
            this.refreshDrawableState();
            this.sendAccessibilityEvent(2048);

            if (mOnCheckedChangeListener != null) {
                mOnCheckedChangeListener.onCheckedChanged(this, mChecked);
            }
        }

    }

    public boolean isChecked() {
        return this.mChecked;
    }

    public void toggle() {
        this.setChecked(!this.mChecked);
    }

    public int[] onCreateDrawableState(int extraSpace) {
        return this.mChecked ? mergeDrawableStates(super.onCreateDrawableState(extraSpace + DRAWABLE_STATE_CHECKED.length), DRAWABLE_STATE_CHECKED) : super.onCreateDrawableState(extraSpace);
    }

    public static interface OnCheckedChangeListener {
        void onCheckedChanged(View buttonView, boolean isChecked);
    }

}
