package com.sqrtf.common.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.sqrtf.common.R
import com.sqrtf.common.api.ApiClient
import com.tbruyelle.rxpermissions2.RxPermissions
import com.trello.rxlifecycle2.android.ActivityEvent
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.util.*


/**
 * Created by roya on 2017/5/21.
 */

open class BaseActivity : RxLifecycleActivity() {

    private val rxPermissions: RxPermissions by lazy { RxPermissions(this) }
    private val runningMap by lazy { IdentityHashMap<Int, Disposable>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    protected fun showToast(s: String, t: Int = Toast.LENGTH_LONG) {
        Toast.makeText(this, s, t).show()
    }

    protected fun <T> Observable<T>.withLifecycle(
            subscribeOn: Scheduler = Schedulers.io(),
            observeOn: Scheduler = AndroidSchedulers.mainThread(),
            untilEvent: ActivityEvent = ActivityEvent.DESTROY): Observable<T> {
        return this
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .compose(bindUntilEvent(untilEvent))
    }

    protected fun <T> Observable<T>.onlyRunOneInstance(taskId: Int, displace: Boolean = true): Observable<T> {
        if (runningMap.containsKey(taskId)) {
            if (!displace) {
                return Observable.create<T> { wrapper -> wrapper.onComplete() }
            } else {
                runningMap[taskId]?.dispose()
                runningMap.remove(taskId)
            }
        }

        return Observable.create<T> { wrapper ->
            val obs = this.subscribe({
                wrapper.onNext(it)
            }, {
                runningMap.remove(taskId)
                wrapper.onError(it)
            }, {
                runningMap.remove(taskId)
                wrapper.onComplete()
            })

            if (!obs.isDisposed) {
                runningMap.put(taskId, obs)
            }
        }
    }

    protected fun ignoreErrors(): Consumer<in Throwable> {
        return Consumer {
            Log.w("toastErrors", it)
        }
    }

    protected fun toastErrors(): Consumer<in Throwable> {
        return Consumer {
            Log.w("toastErrors", it)
            var errorMessage = getString(R.string.unknown_error)

            if (it is HttpException) {
                val body = it.response().errorBody()
                val message = body?.let { it1 ->
                    ApiClient.converterErrorBody(it1)
                }

                if (message?.message() != null) {
                    errorMessage = message.message()!!
                }
            }

            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    protected fun requestPermissions(vararg permissions: String): Observable<Boolean> {
        return rxPermissions
                .request(*permissions)
                .withLifecycle(subscribeOn = AndroidSchedulers.mainThread(),
                        untilEvent = ActivityEvent.PAUSE)
    }
}
