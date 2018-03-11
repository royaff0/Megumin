package com.sqrtf.megumin


import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.sqrtf.common.StringUtil
import com.sqrtf.common.activity.BaseFragment
import com.sqrtf.common.api.ApiClient
import com.sqrtf.common.api.ListResponse
import com.sqrtf.common.model.Announce
import com.sqrtf.common.model.Bangumi
import com.sqrtf.megumin.homefragment.HomeData
import com.sqrtf.megumin.homefragment.HomeHorizontalAdapter
import com.sqrtf.megumin.homefragment.HomeLargeAdapter
import io.reactivex.Observable
import io.reactivex.functions.Function3
import kotlinx.android.synthetic.main.include_bangumi_wide.view.*
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment : BaseFragment() {

    private val recyclerView by lazy { findViewById(R.id.recycler_view) as RecyclerView }
    private val swipeRefresh by lazy { findViewById(R.id.swipe_refresh) as SwipeRefreshLayout }
    private val homeDataAdapter by lazy { HomeDataAdapter(this) }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefresh.setColorSchemeResources(R.color.meguminRed)
        homeDataAdapter.attachTo(recyclerView)

        swipeRefresh.setOnRefreshListener {
            loadData()
        }

        loadData()
    }

    private fun loadData() {
        swipeRefresh.isRefreshing = true
        Observable.zip(
                withLifecycle(ApiClient.getInstance().getAnnounceBangumi()),
                withLifecycle(ApiClient.getInstance().getMyBangumi()),
                withLifecycle(ApiClient.getInstance().getAllBangumi()),
                Function3({ t1: ListResponse<Announce>, t2: ListResponse<Bangumi>, t3: ListResponse<Bangumi> ->
                    arrayOf(t1.getData().map { it.bangumi }, t2.getData(), t3.getData())
                }))
                .subscribe({
                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    homeDataAdapter.list.clear()

                    if (it[0].isNotEmpty()) {
                        homeDataAdapter.list.add(HomeData(HomeData.TYPE.LARGE, null, null, it[0]))
                    }

                    val todayUpdate = it[1]
                            .toHashSet()
                            .filter {
                                val week = (System.currentTimeMillis() - format.parse(it.air_date).time) / 604800000
                                return@filter week <= it.eps + 1
                            }
                            .sortedBy { it.unwatched_count }

                    if (todayUpdate.isNotEmpty()) {
                        homeDataAdapter.list.add(HomeData(getString(R.string.releasing)))
                        homeDataAdapter.list.add(HomeData(todayUpdate
                                .filter { it.unwatched_count >= 1 }))
                    }

                    if (it[2].isNotEmpty()) {
                        homeDataAdapter.list.add(HomeData(getString(R.string.title_bangumi)))
                        homeDataAdapter.list.addAll(it[2].map { HomeData(it) })
                    }

                    homeDataAdapter.list.add(HomeData())
                    homeDataAdapter.notifyDataSetChanged()
                    swipeRefresh.isRefreshing = false
                }, {
                    swipeRefresh.isRefreshing = false
                    toastErrors().accept(it)
                })
    }

    private class HomeDataAdapter(val parent: Fragment) {
        val spanCount = parent.resources.getInteger(R.integer.home_screen_span_count)
        val list = arrayListOf<HomeData>()
        val adapter = HomeAdapter()
        val lm = LinearLayoutManager(parent.context)

        fun attachTo(recyclerView: RecyclerView) {
            recyclerView.layoutManager = lm
            recyclerView.adapter = adapter
        }

        fun notifyDataSetChanged() {
            adapter.notifyDataSetChanged()
        }

        class HomeLineHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView) as RecyclerView
        }

        class HomeLargeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView) as RecyclerView
        }

        private class WideCardHolder(view: View) : RecyclerView.ViewHolder(view) {
            val image = view.findViewById(R.id.imageView) as ImageView
            val title = view.findViewById(R.id.title) as TextView
            val subtitle = view.findViewById(R.id.subtitle) as TextView
            val info = view.findViewById(R.id.info) as TextView
            val state = view.findViewById(R.id.state) as TextView
            val info2 = view.findViewById(R.id.info2) as TextView
        }

        private class TitleHolder(view: View) : RecyclerView.ViewHolder(view) {
            val text = view.findViewById(R.id.textView) as TextView
        }

        private class TailHolder(view: View) : RecyclerView.ViewHolder(view) {
            init {
                view.setOnClickListener { view.context.startActivity(AllBangumiActivity.intent(view.context)) }
            }
        }

        private class PaddingItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
            val dp = context.resources.getDimensionPixelSize(R.dimen.dp_8)

            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val position = parent.getChildAdapterPosition(view)
                val childCount = state.itemCount
                if (position == 0) {
                    outRect.left = outRect.left.plus(dp)
                } else if (position + 1 == childCount) {
                    outRect.right = outRect.right.plus(dp)
                }
            }
        }

        private inner class HomeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder?, position: Int) {

                val bangumi = list[position].bangumi
                when (viewHolder) {
                    is HomeLineHolder -> {
                        if (viewHolder.recyclerView.layoutManager == null) {
                            viewHolder.recyclerView.layoutManager = LinearLayoutManager(viewHolder
                                    .recyclerView.context, LinearLayoutManager.HORIZONTAL, false)
                            viewHolder.recyclerView.addItemDecoration(PaddingItemDecoration(viewHolder.recyclerView.context))
                        }

                        viewHolder.recyclerView.adapter =
                                HomeHorizontalAdapter(
                                        list[position], { parent.startActivity(DetailActivity.intent(parent.context, it)) })
                    }
                    is HomeLargeHolder -> {
                        if (viewHolder.recyclerView.layoutManager == null) {
                            viewHolder.recyclerView.layoutManager = LinearLayoutManager(viewHolder
                                    .recyclerView.context, LinearLayoutManager.HORIZONTAL, false)
                            viewHolder.recyclerView.addItemDecoration(PaddingItemDecoration(viewHolder.recyclerView.context))
                        }

                        viewHolder.recyclerView.adapter =
                                HomeLargeAdapter(
                                        list[position], { parent.startActivity(DetailActivity.intent(parent.context, it)) })
                    }
                    is WideCardHolder -> {
                        if (bangumi == null) {
                            return
                        }

                        viewHolder.title.text = StringUtil.mainTitle(bangumi)
                        viewHolder.subtitle.text = StringUtil.subTitle(bangumi)
                        viewHolder.info.text = viewHolder.info.resources.getString(R.string.update_info)
                                ?.format(bangumi.eps, bangumi.air_weekday.let { StringUtil.dayOfWeek(it) }, bangumi.air_date)

                        if (bangumi.favorite_status > 0) {
                            val array = viewHolder.state.resources.getStringArray(R.array.array_favorite)
                            if (array.size > bangumi.favorite_status) {
                                viewHolder.state.text = array[bangumi.favorite_status]
                            }
                        } else {
                            viewHolder.state.text = ""
                        }

                        viewHolder.info2.text = bangumi.summary.replace("\n", "")

                        Glide.with(parent)
                                .load(bangumi.image)
                                .into(viewHolder.image)

                        viewHolder.itemView.setOnClickListener {
                            parent.startActivity(bangumi.let { it1 -> DetailActivity.intent(parent.context, it1) })
                        }
                    }
                    is TitleHolder -> {
                        viewHolder.text.text = list[position].string
                    }
                }
            }

            override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): RecyclerView.ViewHolder {
                return when (p1) {
                    HomeData.TYPE.TITLE.value -> TitleHolder(LayoutInflater.from(p0!!.context).inflate(R.layout.include_home_title, p0, false))
                    HomeData.TYPE.WIDE.value -> WideCardHolder(LayoutInflater.from(p0!!.context).inflate(R.layout.include_bangumi_wide, p0, false))
                    HomeData.TYPE.LARGE.value -> HomeLargeHolder(LayoutInflater.from(p0!!.context).inflate(R.layout.include_home_line_container, p0, false))
                    HomeData.TYPE.CONTAINER.value -> HomeLineHolder(LayoutInflater.from(p0!!.context).inflate(R.layout.include_home_line_container, p0, false))
                    HomeData.TYPE.TAIL.value -> TailHolder(LayoutInflater.from(p0!!.context).inflate(R.layout.include_home_tail, p0, false))
                    else -> throw RuntimeException("unknown type")
                }
            }

            override fun getItemCount(): Int {
                return list.size
            }

            override fun getItemViewType(position: Int): Int {
                return list[position].type.value
            }
        }

    }
}
