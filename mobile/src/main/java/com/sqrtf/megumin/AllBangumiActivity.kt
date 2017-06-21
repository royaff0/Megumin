package com.sqrtf.megumin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.sqrtf.common.api.ApiClient
import com.sqrtf.common.model.Bangumi
import io.reactivex.Observable

/**
 * Created by roya on 2017/6/6.
 */
class AllBangumiActivity : ListActivity() {

    companion object {
        fun intent(context: Context): Intent {
            return Intent(context, AllBangumiActivity::class.java)
        }

        private val TASK_ID_LOAD = 1
    }

    var loaded = false
    var pageNow = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.title_bangumi)
    }

    override fun onLoadData(): Observable<List<Bangumi>> {
        if (!loaded) {
            loaded = true
            Log.i("AllBangumiActivity", "onLoadData:" + pageNow)
            return ApiClient.getInstance().getSearchBangumi(pageNow, 20, "air_date", "desc", null)
                    .onlyRunOneInstance(TASK_ID_LOAD, false)
                    .flatMap {
                        pageNow += 1
                        loaded = it.getData().isEmpty()
                        Observable.just(it.getData())
                    }
        } else {
            return Observable.create<List<Bangumi>> { it.onComplete() }
        }
    }

}