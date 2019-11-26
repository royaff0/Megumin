package com.sqrtf.megumin

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.AppCompatSpinner
import android.text.TextUtils
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.sqrtf.common.activity.BaseActivity
import com.sqrtf.common.api.ApiClient
import com.sqrtf.common.api.LoginRequest
import com.sqrtf.common.cache.MeguminPreferences
import io.reactivex.functions.Consumer
import okhttp3.HttpUrl
import retrofit2.HttpException

class FirstConfigActivity : BaseActivity() {

    private val spinner by lazy { findViewById<AppCompatSpinner>(R.id.spinner) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_config)

        val sp = ArrayAdapter.createFromResource(this,
                R.array.array_link_type, R.layout.spinner_item)
        sp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = sp
        spinner.setSelection(1)

        val textServer = findViewById<EditText>(R.id.server)
        val textUser = findViewById<EditText>(R.id.user)
        val textPw = findViewById<EditText>(R.id.pw)

        textServer.setText(MeguminPreferences.getServer(), TextView.BufferType.EDITABLE)

        val toast = Toast.makeText(this, getString(R.string.connecting), Toast.LENGTH_LONG)

        findViewById<View>(R.id.loginButton).setOnClickListener {
            val host = StringBuilder()
            val domain = textServer.text.toString()

            if (domain.isEmpty()) {
                showToast("Please enter domain")
                return@setOnClickListener
            }

            val isHttps = spinner.selectedItemPosition == 1
            host.append(if (isHttps) "https://" else "http://")
            host.append(domain)

            if (!domain.endsWith("/"))
                host.append("/")

            toast.setText(getString(R.string.connecting))
            toast.show()

            ApiClient.init(this, host.toString())
            ApiClient.getInstance().login(LoginRequest(textUser.text.toString(), textPw.text.toString(), true))
                    .withLifecycle()
                    .subscribe({
                        MeguminPreferences.setServer(host.toString())
                        MeguminPreferences.setUsername(textUser.text.toString())
                        startActivity(Intent(this, HomeActivity::class.java))
                        toast.cancel()
                        finish()
                    }, {
                        var errorMessage = getString(R.string.network_error)

                        if (it is HttpException) {
                            val body = it.response().errorBody()
                            val message = body?.let { it1 ->
                                ApiClient.converterErrorBody(it1)
                            }

                            if (message?.message() != null) {
                                errorMessage = message.message()
                            }
                        }

                        toast.setText(errorMessage)
                        toast.show()
                    })
        }
    }
}
