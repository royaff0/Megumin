package com.sqrtf.megumin

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import android.widget.TextView
import com.sqrtf.common.activity.BaseActivity
import com.sqrtf.common.api.ApiClient
import com.sqrtf.common.api.LoginRequest
import com.sqrtf.common.cache.MeguminPreferences
import io.reactivex.functions.Consumer
import okhttp3.HttpUrl

class FirstConfigActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_config)

        val textServer = findViewById(R.id.server) as EditText
        val textUser = findViewById(R.id.user) as EditText
        val textPw = findViewById(R.id.pw) as EditText

        textServer.setText(MeguminPreferences.getServer(), TextView.BufferType.EDITABLE)

        findViewById(R.id.floatingActionButton).setOnClickListener {
            var url = textServer.text.toString()

            if (HttpUrl.parse(url) == null) {
                showToast("url not verified")
                return@setOnClickListener
            }

            showToast("请稍候...")

            ApiClient.init(this, url)
            ApiClient.getInstance().login(LoginRequest(textUser.text.toString(), textPw.text.toString(), true))
                    .withLifecycle()
                    .subscribe(Consumer {
                        MeguminPreferences.setServer(url)
                        MeguminPreferences.setUsername(textUser.text.toString())
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }, toastErrors())
        }
    }
}
