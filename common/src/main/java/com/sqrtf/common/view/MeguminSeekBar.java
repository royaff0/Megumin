package com.sqrtf.common.view;

import android.content.Context;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;

/**
 * Created by roya on 2017/6/8.
 */

public class MeguminSeekBar extends AppCompatSeekBar {
    public MeguminSeekBar(Context context) {
        super(context, null);
    }

    public MeguminSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public MeguminSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        setPadding(0, 0, 0, 0);
    }
}
