package com.sqrtf.megumin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.sqrtf.common.activity.BaseActivity
import com.sqrtf.common.api.ApiClient
import com.sqrtf.common.api.ApiHelper

/**
 * Created by roya on 2017/5/26.
 */
class PlayPaddingActivity : BaseActivity() {
    companion object {
        fun intent(context: Context, id: String): Intent {
            val i = Intent(context, PlayPaddingActivity::class.java)
            i.putExtra(KEY_INTENT_ID, id)
            return i
        }

        private val KEY_INTENT_ID = "PlayPaddingActivity:KEY_INTENT_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getStringExtra(KEY_INTENT_ID)
        assert(!TextUtils.isEmpty(id))

        ApiClient.getInstance().getEpisodeDetail(id)
                .withLifecycle()
                .subscribe({
                    startActivity(PlayerActivity.intent(this, it.video_files[0].url))
                    finish()
                }, {
                    toastErrors()
                    finish()
                })

    }
}