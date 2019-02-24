package com.sqrtf.megumin

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.widget.AppCompatSpinner
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.sqrtf.common.StringUtil
import com.sqrtf.common.activity.BaseActivity
import com.sqrtf.common.api.ApiClient
import com.sqrtf.common.api.ApiHelper
import com.sqrtf.common.api.ApiService
import com.sqrtf.common.model.Bangumi
import io.reactivex.Observable
import io.reactivex.functions.Consumer

class AllBangumiActivity : BaseThemeActivity() {
    companion object {
        fun intent(context: Context): Intent {
            return Intent(context, AllBangumiActivity::class.java)
        }

        private val TASK_ID_LOAD = 1
    }

    var loaded = false
    var pageNow = 1

    private val spinner by lazy { findViewById(R.id.spinner) as AppCompatSpinner }
    private val bangumiList = arrayListOf<Bangumi>()
    private val adapter = HomeAdapter()

    private val filterAll: (Bangumi) -> Boolean = { true }
    private val filterAnime: (Bangumi) -> Boolean = { it.type == ApiService.BANGUMI_TYPE_ANIME }
    private val filterTv: (Bangumi) -> Boolean = { it.type == ApiService.BANGUMI_TYPE_TV }

    private var filterNow = filterAll

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_bangumi)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.title_bangumi)

        val sp = ArrayAdapter.createFromResource(this,
                R.array.array_bangumi_type, R.layout.spinner_item)
        sp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = sp
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterNow = when (position) {
                    1 -> filterAnime
                    2 -> filterTv
                    else -> filterAll
                }
                reloadData()
            }

        }

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

    fun reloadData() {
        loaded = false
        pageNow = 1
        bangumiList.clear()
        adapter.notifyDataSetChanged()
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
            return ApiClient.getInstance().getSearchBangumi(pageNow, 300, "air_date", "desc", null)
                    .withLifecycle()
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
        val fl = list.filter(filterNow)
        bangumiList.addAll(fl)
        adapter.notifyDataSetChanged()
    }

    private class WideCardHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image = view.findViewById(R.id.imageView) as ImageView
        val title = view.findViewById(R.id.title) as TextView
//        val subtitle = view.findViewById(R.id.subtitle) as TextView
        val info = view.findViewById(R.id.info) as TextView
        val state = view.findViewById(R.id.state) as TextView
        val info2 = view.findViewById(R.id.info2) as TextView
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

    private inner class HomeAdapter : RecyclerView.Adapter<WideCardHolder>() {
        override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): WideCardHolder = WideCardHolder(LayoutInflater.from(this@AllBangumiActivity).inflate(R.layout.include_bangumi_wide, p0, false))

        override fun onBindViewHolder(viewHolder: WideCardHolder, p1: Int) {
            val bangumi = bangumiList[p1]
            viewHolder.title.text = StringUtil.mainTitle(bangumi)
//            viewHolder.subtitle.text = StringUtil.subTitle(bangumi)
            viewHolder.info.text = viewHolder.info.resources.getString(R.string.update_info)
                    ?.format(bangumi.eps, bangumi.air_weekday.let { StringUtil.dayOfWeek(it) }, bangumi.air_date)

            if (bangumi.favorite_status > 0) {
                val array = resources.getStringArray(R.array.array_favorite)
                if (array.size > bangumi.favorite_status) {
                    viewHolder.state.text = array[bangumi.favorite_status]
                }
            } else {
                viewHolder.state.text = ""
            }

            viewHolder.info2.text = bangumi.summary.replace("\n", "")

            Glide.with(this@AllBangumiActivity)
                    .load(bangumi.cover_image.fixedUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(viewHolder.image)

            viewHolder.itemView.setOnClickListener {
                this@AllBangumiActivity.startActivity(bangumi.let { it1 -> DetailActivity.intent(this@AllBangumiActivity, it1) })
            }
        }

        override fun getItemCount(): Int = bangumiList.size
    }
}
