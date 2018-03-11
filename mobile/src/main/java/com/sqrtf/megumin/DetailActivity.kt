package com.sqrtf.megumin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.sqrtf.common.StringUtil
import com.sqrtf.common.activity.BaseActivity
import com.sqrtf.common.api.*
import com.sqrtf.common.cache.JsonUtil
import com.sqrtf.common.model.Bangumi
import com.sqrtf.common.model.BangumiDetail
import com.sqrtf.common.model.Episode
import com.sqrtf.common.view.ScrollStartLayoutManager
import com.sqrtf.common.view.ProgressCoverView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.*


class DetailActivity : BaseActivity() {
    val iv by lazy { findViewById(R.id.image) as ImageView? }
    //    val ivCover by lazy { findViewById(R.id.image_cover) as ImageView }
//    val ctitle by lazy { findViewById(R.id.title) as TextView }
    val subtitle by lazy { findViewById(R.id.subtitle) as TextView }
    val info by lazy { findViewById(R.id.info) as TextView }
    val summary by lazy { findViewById(R.id.summary) as TextView }
    val summary2 by lazy { findViewById(R.id.summary2) as TextView }
    val more by lazy { findViewById(R.id.button_more) as TextView }
    val spinner by lazy { findViewById(R.id.spinner) as Spinner }
    val recyclerView by lazy { findViewById(R.id.recycler_view) as RecyclerView }
    val summaryLayout by lazy { findViewById(R.id.summary_layout) }
    val btnBgmTv by lazy { findViewById(R.id.button_bgm_tv) }

    val episodeAdapter by lazy { EpisodeAdapter() }

    companion object {
        fun intent(context: Context, bgm: Bangumi): Intent {
            val intent = Intent(context, DetailActivity::class.java)
            val json = JsonUtil.toJson(bgm)
            intent.putExtra(INTENT_KEY_BANGMUMI, json)
            return intent
        }

        private val INTENT_KEY_BANGMUMI = "INTENT_KEY_BANGMUMI"
        private val REQUEST_CODE = 0x81
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = ""

        recyclerView.layoutManager = ScrollStartLayoutManager(this, ScrollStartLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = episodeAdapter

        val json = intent.getStringExtra(INTENT_KEY_BANGMUMI)
        checkNotNull(json)
        val bgm = JsonUtil.fromJson(json, Bangumi::class.java)
        checkNotNull(bgm)

        setData(bgm!!)
        loadData(bgm.id)
        title = StringUtil.mainTitle(bgm)
    }

    private fun loadData(bgmId: String) {
        ApiClient.getInstance().getBangumiDetail(bgmId)
                .withLifecycle()
                .subscribe({
                    setData(it.getData())
                }, {
                    toastErrors().accept(it)
                    finish()
                })
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

    private fun playVideo(episode: Episode) {
        assert(!TextUtils.isEmpty(episode.id))

        ApiClient.getInstance().getEpisodeDetail(episode.id)
                .withLifecycle()
                .subscribe({
                    startActivityForResult(PlayerActivity.intent(this,
                            it.video_files[0].url,
                            episode.id,
                            episode.bangumi_id),
                            DetailActivity.REQUEST_CODE)
                }, {
                    toastErrors()
                })
    }

    private fun markWatched(episode: Episode) {
        ApiClient.getInstance().uploadWatchHistory(
                HistoryChangeRequest(Collections.singletonList(HistoryChangeItem(episode.bangumi_id,
                        episode.id,
                        System.currentTimeMillis(),
                        0,
                        1f,
                        true))))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        Consumer {
                            loadData(episode.bangumi_id)
                        },
                        ignoreErrors())
    }

    private fun openWith(episode: Episode) {
        ApiClient.getInstance().getEpisodeDetail(episode.id)
                .withLifecycle()
                .subscribe({
                    val intent = Intent(Intent.ACTION_VIEW)
                    val url = Uri.encode(ApiHelper.fixHttpUrl(it.video_files[0]!!.url), "@#&=*+-_.,:!?()/~'%")
                    intent.setDataAndType(Uri.parse(url), "video/mp4")
                    startActivity(intent)
                }, {
                    toastErrors()
                })
    }

    @SuppressLint("SetTextI18n")
    private fun openMenu(episode: Episode) {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialog)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_episode_menu, null, false)

        val name = if (Locale.getDefault().displayLanguage == Locale.CHINESE.displayLanguage) {
            if (TextUtils.isEmpty(episode.name_cn)) episode.name else episode.name_cn
        } else {
            if (TextUtils.isEmpty(episode.name)) episode.name_cn else episode.name
        }

        (view.findViewById(R.id.title) as TextView).text =
                "${episode.episode_no}. $name"

        view.findViewById(R.id.button_mark_watched).setOnClickListener {
            markWatched(episode)
            dialog.dismiss()
        }
//        view.findViewById(R.id.button_mark_all_watched).setOnClickListener {
//            markAllBefore(episode)
//            dialog.dismiss()
//        }
        view.findViewById(R.id.button_open_external).setOnClickListener {
            openWith(episode)
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == DetailActivity.REQUEST_CODE
                && resultCode == Activity.RESULT_OK
                && data != null) {
            val id = data.getStringExtra(PlayerActivity.RESULT_KEY_ID)
            val bgmId = data.getStringExtra(PlayerActivity.RESULT_KEY_ID_2)
            val duration = data.getLongExtra(PlayerActivity.RESULT_KEY_DURATION, 0)
            val position = data.getLongExtra(PlayerActivity.RESULT_KEY_POSITION, 0)
            ApiClient.getInstance().uploadWatchHistory(
                    HistoryChangeRequest(Collections.singletonList(HistoryChangeItem(bgmId,
                            id,
                            System.currentTimeMillis(),
                            position / 100,
                            position.toFloat() / duration,
                            duration == position))))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            Consumer {
                                loadData(bgmId)
                            },
                            ignoreErrors())
        }
    }

    private fun setData(detail: Bangumi) {
        recyclerView.isNestedScrollingEnabled = false

        iv?.let { Glide.with(this).load(detail.image).into(iv) }
//        Glide.with(this).load(detail.image).into(ivCover)

//        ctitle.text = StringUtil.mainTitle(detail)
        subtitle.text = StringUtil.subTitle(detail)
        info.text = resources.getString(R.string.update_info)
                ?.format(detail.eps, StringUtil.dayOfWeek(detail.air_weekday), detail.air_date)

        btnBgmTv.visibility = if (detail.bgm_id > 0) View.VISIBLE else View.GONE

        btnBgmTv.setOnClickListener {
            if (detail.bgm_id <= 0) {
                return@setOnClickListener
            }

            val url = "https://bgm.tv/subject/" + detail.bgm_id
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        if (TextUtils.isEmpty(detail.summary)) {
            summary.visibility = View.GONE
            summary2.visibility = View.GONE
            more.visibility = View.GONE
        } else {
            fun less() {
                summary.text = detail.summary.replace("\n", "\t")
                summary.maxLines = 1
                summary.post {
                    summary2.text = summary.text.toString().substring(summary.layout.getLineEnd(0))
                }
                more.setText(R.string.more)
                more.tag = 1
            }

            fun more() {
                summary.text = detail.summary
                summary2.text = ""
                summary.maxLines = 20
                more.setText(R.string.less)
                more.tag = 0
            }

            summaryLayout.setOnClickListener {
                if (more.tag == 0) {
                    less()
                } else {
                    more()
                }
            }

            if (more.tag == null) {
                less()
            }
        }

        val adapter = ArrayAdapter.createFromResource(this,
                R.array.array_favorite, R.layout.spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(detail.favorite_status)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (detail.favorite_status == position) {
                    return
                }

                ApiClient.getInstance().uploadFavoriteStatus(detail.id, FavoriteChangeRequest(position))
                        .withLifecycle()
                        .subscribe({
                            detail.favorite_status = position
                        }, {
                            toastErrors()
                            spinner.setSelection(detail.favorite_status)
                        })
            }

        }

        if (detail is BangumiDetail && detail.episodes != null && detail.episodes.isNotEmpty()) {
            episodeAdapter.setEpisodes(detail.episodes)
            detail.episodes
                    .map { it?.watch_progress?.last_watch_time }
                    .withIndex()
                    .filter { it.value != null }
                    .sortedBy { it.value }
                    .lastOrNull()
                    ?.let {
                        recyclerView.post {
                            recyclerView.smoothScrollToPosition(it.index)
                        }
                    }
        }

    }

    inner class EpisodeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var episodes: List<Episode> = java.util.ArrayList()

        fun setEpisodes(ep: List<Episode>) {
            episodes = ep
            notifyDataSetChanged()
        }

        fun getEpisodes(): List<Episode> {
            return episodes
        }

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val view = v
            val tv = v.findViewById(R.id.tv) as TextView
            val image = v.findViewById(R.id.image) as ImageView
            val progress = v.findViewById(R.id.progress) as ProgressCoverView
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, p1: Int) {
            if (holder is VH) {
                val d = episodes[p1]

                val name = if (Locale.getDefault().displayLanguage == Locale.CHINESE.displayLanguage) {
                    if (TextUtils.isEmpty(d.name_cn)) d.name else d.name_cn
                } else {
                    if (TextUtils.isEmpty(d.name)) d.name_cn else d.name
                }

                holder.tv.text = "${d.episode_no}. $name"
                if (d.watch_progress?.percentage != null
                        && d.watch_progress?.percentage!! < 0.15f) {
                    d.watch_progress?.percentage = 0.15f
                }
                holder.progress.setProgress(d.watch_progress?.percentage ?: 0f)

                Glide.with(this@DetailActivity)
                        .load(ApiHelper.fixHttpUrl(d.thumbnail))
                        .into(holder.image)

                holder.tv.alpha = if (d.status != 0) 1f else 0.2f
                holder.view.setOnClickListener {
                    if (d.status != 0) playVideo(d)
                }

                holder.view.setOnLongClickListener {
                    if (d.status != 0) openMenu(d)
                    true
                }

            }
        }

        override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): RecyclerView.ViewHolder {
            return VH(LayoutInflater.from(p0!!.context).inflate(R.layout.rv_item_episode, p0, false))
        }

        override fun getItemCount(): Int {
            return episodes.size
        }

    }
}
