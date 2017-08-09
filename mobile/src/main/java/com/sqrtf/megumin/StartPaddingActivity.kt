package com.sqrtf.megumin

import android.content.Intent
import android.os.Bundle
import com.sqrtf.common.activity.BaseActivity
import com.sqrtf.common.cache.MeguminPreferences

/**
 * Created by roya on 2017/5/26.
 */
class StartPaddingActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(Intent(this,
                if (MeguminPreferences.configured()) HomeActivity::class.java
                else FirstConfigActivity::class.java))
//        startActivity(PlayerActivity.intent(this, "224","24ref","faf"))
        finish()
    }
}