package com.sqrtf.megumin


import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE
import android.text.format.DateUtils.WEEK_IN_MILLIS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.sqrtf.common.activity.BaseFragment
import com.sqrtf.common.api.ApiClient
import com.sqrtf.common.api.ListResponse
import com.sqrtf.common.model.Bangumi
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import java.util.*


class HomeFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view!!.findViewById(R.id.recycler_view) as RecyclerView
        val homeDataAdapter = HomeDataAdapter(this)
        homeDataAdapter.attachTo(recyclerView)

        Observable.zip(withLifecycle(ApiClient.getInstance().getMyBangumi()),
                withLifecycle(ApiClient.getInstance().getAllBangumi()),
                BiFunction { t1: ListResponse<Bangumi>, t2: ListResponse<Bangumi> -> Pair(t1.getData(), t2.getData()) })
                .subscribe(Consumer {
                    val set = it.first.toHashSet()
//                    set.addAll(it.second)
                    val todayUpdate = set.filter {
                        val todayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                        val todayOfWeekCn = if (todayOfWeek == 1) 7 else todayOfWeek - 1
                        val lastDayOfWkCn = if (todayOfWeekCn == 1) 7 else todayOfWeekCn - 1
                        it.air_weekday == todayOfWeekCn || it.air_weekday == lastDayOfWkCn
                    }
                    if (todayUpdate.isNotEmpty()) {
                        homeDataAdapter.list.add(HomeDataAdapter.HomeData("Favorites update"))
                        homeDataAdapter.list.addAll(todayUpdate
                                .filter { it.unwatched_count >= 1 }
                                .map { HomeDataAdapter.HomeData(HomeDataAdapter.TYPE.MEDIUM, it) })
                    }

                    if (it.second.isNotEmpty()) {
                        homeDataAdapter.list.add(HomeDataAdapter.HomeData("Bangumi"))
                        homeDataAdapter.list.addAll(it.second.map { HomeDataAdapter.HomeData(HomeDataAdapter.TYPE.WIDE, it) })
                    }

                    homeDataAdapter.list.add(HomeDataAdapter.HomeData())
                    homeDataAdapter.notifyDataSetChanged()
                }, toastErrors())
    }

    private class HomeDataAdapter(val parent: Fragment) {
        val spanCount = parent.resources.getInteger(R.integer.home_screen_span_count)
        val list = arrayListOf<HomeData>()
        val adapter = HomeAdapter()
        val lm = HomeLayoutManager(parent.context)

        enum class TYPE(val value: Int) {TITLE(0), MEDIUM(1), WIDE(2), TAIL(3) }

        class HomeData private constructor(
                val type: TYPE,
                val bangumi: Bangumi? = null,
                val string: String? = null) {
            constructor () : this(TYPE.TAIL)
            constructor (type: TYPE, bangumi: Bangumi) : this(type, bangumi, null)
            constructor (string: String) : this(TYPE.TITLE, null, string)
        }

        fun attachTo(recyclerView: RecyclerView) {
            recyclerView.layoutManager = lm
            recyclerView.adapter = adapter
            recyclerView.addItemDecoration(HomeItemDecoration())
        }

        fun notifyDataSetChanged() {
            adapter.notifyDataSetChanged()
        }

        fun itemInLeft(positon: Int): Boolean {
            val countSize = (0..positon).sumBy { lm.spanSizeLookup.getSpanSize(it) }
            return countSize % spanCount == 1
        }

        fun itemInRight(positon: Int): Boolean {
            val countSize = (0..positon).sumBy { lm.spanSizeLookup.getSpanSize(it) }
            return countSize % spanCount == 0
        }

        private class MediumCardHolder(view: View) : RecyclerView.ViewHolder(view) {
            val image = view.findViewById(R.id.imageView) as ImageView
            val title = view.findViewById(R.id.title) as TextView
            val subtitle = view.findViewById(R.id.subtitle) as TextView
        }

        private class WideCardHolder(view: View) : RecyclerView.ViewHolder(view) {
            val image = view.findViewById(R.id.imageView) as ImageView
            val title = view.findViewById(R.id.title) as TextView
            val subtitle = view.findViewById(R.id.subtitle) as TextView
            val info = view.findViewById(R.id.info) as TextView
        }

        private class TitleHolder(view: View) : RecyclerView.ViewHolder(view) {
            val text = view.findViewById(R.id.textView) as TextView
        }

        private class TailHolder(view: View) : RecyclerView.ViewHolder(view)

        inner private class HomeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder?, position: Int) {

                val bangumi = list[position].bangumi
                when (viewHolder) {
                    is MediumCardHolder -> {
                        viewHolder.title.text = bangumi?.name_cn
                        viewHolder.subtitle.text = "未看 " + bangumi?.unwatched_count
                        Glide.with(parent)
                                .load(bangumi?.image)
                                .into(viewHolder.image)

                        viewHolder.itemView.setOnClickListener {
                            parent.startActivity(bangumi?.let { it1 -> DetailActivity.intent(parent.context, it1) })
                        }
                    }
                    is WideCardHolder -> {
                        viewHolder.title.text = bangumi?.name_cn
                        viewHolder.subtitle.text = bangumi?.name
                        viewHolder.info.text = bangumi?.air_date + ", " + bangumi?.eps + "集, " + bangumi?.air_weekday
                        Glide.with(parent)
                                .load(bangumi?.image)
                                .into(viewHolder.image)

                        viewHolder.itemView.setOnClickListener {
                            parent.startActivity(bangumi?.let { it1 -> DetailActivity.intent(parent.context, it1) })
                        }
                    }
                    is TitleHolder -> {
                        viewHolder.text.text = list[position].string
                    }
                }
            }

            override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): RecyclerView.ViewHolder {
                return when (p1) {
                    TYPE.TITLE.value -> TitleHolder(LayoutInflater.from(p0!!.context).inflate(R.layout.include_home_title, p0, false))
                    TYPE.WIDE.value -> WideCardHolder(LayoutInflater.from(p0!!.context).inflate(R.layout.include_bangumi_wide, p0, false))
                    TYPE.MEDIUM.value -> MediumCardHolder(LayoutInflater.from(p0!!.context).inflate(R.layout.include_bangumi_medium, p0, false))
                    TYPE.TAIL.value -> TailHolder(LayoutInflater.from(p0!!.context).inflate(R.layout.include_home_tail, p0, false))
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

        inner private class HomeLayoutManager(context: Context) : GridLayoutManager(context, spanCount) {
            init {
                spanSizeLookup = object : SpanSizeLookup() {
                    override fun getSpanSize(p0: Int): Int {
                        return when (list[p0].type) {
                            TYPE.MEDIUM -> 1
                            else -> spanCount
                        }
                    }
                }
            }
        }

        inner private class HomeItemDecoration : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
                val position = parent!!.getChildAdapterPosition(view)
                if (lm.spanSizeLookup.getSpanSize(position) == 1
                        && view != null
                        && outRect != null) {
                    if (itemInLeft(position)) {
                        outRect.left += view.resources.getDimensionPixelSize(R.dimen.spacing)
                        outRect.right -= view.resources.getDimensionPixelSize(R.dimen.spacing)
                    } else if (itemInRight(position)) {
                        outRect.left -= view.resources.getDimensionPixelSize(R.dimen.spacing)
                        outRect.right += view.resources.getDimensionPixelSize(R.dimen.spacing)
                    }
                }

            }
        }

    }
}
