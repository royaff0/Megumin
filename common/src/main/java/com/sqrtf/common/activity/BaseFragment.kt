package com.sqrtf.common.activity


import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.sqrtf.common.R
import com.sqrtf.common.api.ApiClient
import com.trello.rxlifecycle2.android.FragmentEvent
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException


open class BaseFragment : RxLifecycleFragment() {

    var thisView: View? = null

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.thisView = view
    }

    protected fun findViewById(resId: Int): View {
        return thisView!!.findViewById(resId)
    }

    protected fun <T> withLifecycle(
            observable: Observable<T>,
            subscribeOn: Scheduler = Schedulers.io(),
            observeOn: Scheduler = AndroidSchedulers.mainThread(),
            untilEvent: FragmentEvent = FragmentEvent.DESTROY): Observable<T> {
        return observable
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .compose(bindUntilEvent(untilEvent))
    }

    protected fun ignoreErrors(): Consumer<in Throwable> {
        return Consumer {}
    }

    protected fun toastErrors(): Consumer<in Throwable> {
        return Consumer {
            var errorMessage = getString(R.string.unknown_error)

            if (it is HttpException) {
                val body = it.response().errorBody()
                val message = body?.let { it1 ->
                    ApiClient.converterErrorBody(it1)
                }

                if (message?.message() != null) {
                    errorMessage = message.message()
                }
            }

            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

}