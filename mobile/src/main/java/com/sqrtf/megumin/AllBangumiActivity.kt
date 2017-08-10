package com.sqrtf.megumin

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.sqrtf.common.StringUtil
import com.sqrtf.common.activity.BaseActivity
import com.sqrtf.common.api.ApiClient
import com.sqrtf.common.model.Bangumi
import io.reactivex.Observable
import io.reactivex.functions.Consumer

class AllBangumiActivity : BaseActivity() {
    companion object {
        fun intent(context: Context): Intent {
            return Intent(context, AllBangumiActivity::class.java)
        }

        private val TASK_ID_LOAD = 1
    }

    var loaded = false
    var pageNow = 1

    private val bangumiList = arrayListOf<Bangumi>()
    private val adapter = HomeAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.title_bangumi)

        val recyclerView = findViewById(R.id.recycler_view) as RecyclerView
        val mLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(PaddingItemDecoration())

        val mScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                val visibleItemCount = mLayoutManager.childCount
                val totalItemCount = mLayoutManager.itemCount
                val pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    loadData()
                }
            }
        }
        recyclerView.addOnScrollListener(mScrollListener)

        loadData()
    }

    fun loadData() {
        onLoadData()
                .withLifecycle()
                .subscribe(Consumer {
                    addToList(it)
                }, toastErrors())
    }

    fun onLoadData(): Observable<List<Bangumi>> {
        if (!loaded) {
            loaded = true
            Log.i("AllBangumiActivity", "onLoadData:" + pageNow)
            return ApiClient.getInstance().getSearchBangumi(pageNow, 20, "air_date", "desc", null)
                    .onlyRunOneInstance(AllBangumiActivity.TASK_ID_LOAD, false)
                    .flatMap {
                        pageNow += 1
                        loaded = it.getData().isEmpty()
                        Observable.just(it.getData())
                    }
        } else {
            return Observable.create<List<Bangumi>> { it.onComplete() }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun addToList(list: List<Bangumi>) {
//        bangumiList.clear()
        bangumiList.addAll(list)
        adapter.notifyDataSetChanged()
    }

    private class WideCardHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image = view.findViewById(R.id.imageView) as ImageView
        val title = view.findViewById(R.id.title) as TextView
        val subtitle = view.findViewById(R.id.subtitle) as TextView
        val info = view.findViewById(R.id.info) as TextView
        val state = view.findViewById(R.id.state) as TextView
    }

    private class PaddingItemDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
            val position = parent!!.getChildAdapterPosition(view)
            val childCount = state!!.itemCount
            if (position == 0) {
                outRect?.top = outRect?.top?.plus(view!!.resources.getDimensionPixelSize(R.dimen.spacing_list))
            } else if (position + 1 == childCount) {
                outRect?.bottom = outRect?.bottom?.plus(view!!.resources.getDimensionPixelSize(R.dimen.spacing_list_bottom))
            }
        }
    }

    inner private class HomeAdapter : RecyclerView.Adapter<WideCardHolder>() {
        override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): WideCardHolder
                = WideCardHolder(LayoutInflater.from(this@AllBangumiActivity).inflate(R.layout.include_bangumi_wide, p0, false))

        override fun onBindViewHolder(viewHolder: WideCardHolder, p1: Int) {
            val bangumi = bangumiList[p1]
            viewHolder.title.text = bangumi.name_cn
            viewHolder.subtitle.text = bangumi.name
            viewHolder.info.text = viewHolder.info.resources.getString(R.string.update_info)
                    ?.format(bangumi.air_date, bangumi.eps, bangumi.air_weekday.let { StringUtil.dayOfWeek(it) })

            if (bangumi.favorite_status > 0) {
                val array = resources.getStringArray(R.array.array_favorite)
                if (array.size > bangumi.favorite_status) {
                    viewHolder.state.text = array[bangumi.favorite_status]
                }
            } else {
                viewHolder.state.text = ""
            }

            Glide.with(this@AllBangumiActivity)
                    .load(bangumi.image)
                    .into(viewHolder.image)

            viewHolder.itemView.setOnClickListener {
                this@AllBangumiActivity.startActivity(bangumi.let { it1 -> DetailActivity.intent(this@AllBangumiActivity, it1) })
            }
        }

        override fun getItemCount(): Int = bangumiList.size
    }
}
