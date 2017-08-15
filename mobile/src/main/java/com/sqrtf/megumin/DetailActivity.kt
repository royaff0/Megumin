package com.sqrtf.megumin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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

        val json = intent.getStringExtra(INTENT_KEY_BANGMUMI)
        checkNotNull(json)
        val bgm = JsonUtil.fromJson(json, Bangumi::class.java)
        checkNotNull(bgm)

        setData(bgm!!)
        loadData(bgm.id)
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
        val iv = findViewById(R.id.image) as ImageView?
        val ivCover = findViewById(R.id.image_cover) as ImageView
        val ctitle = findViewById(R.id.title) as TextView
        val subtitle = findViewById(R.id.subtitle) as TextView
        val info = findViewById(R.id.info) as TextView
        val summary = findViewById(R.id.summary) as TextView
        val summary2 = findViewById(R.id.summary2) as TextView
        val more = findViewById(R.id.button_more)
        val spinner = findViewById(R.id.spinner) as Spinner
        val recyclerView = findViewById(R.id.recycler_view) as RecyclerView
        val summaryLayout = findViewById(R.id.summary_layout)
        val btnBgmTv = findViewById(R.id.button_bgm_tv)

        recyclerView.isNestedScrollingEnabled = false

        iv?.let { Glide.with(this).load(detail.image).into(iv) }
        Glide.with(this).load(detail.image).into(ivCover)

        ctitle.text = detail.name_cn
        subtitle.text = detail.name
        info.text = resources.getString(R.string.update_info)
                ?.format(detail.air_date, detail.eps, StringUtil.dayOfWeek(detail.air_weekday))

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

        if (!TextUtils.isEmpty(detail.summary)) {
            summary.text = detail.summary
            summary2.post {
                summary2.text = summary.text.toString().substring(summary.layout.getLineEnd(2))
            }
            summaryLayout.setOnClickListener {
                summary2.setSingleLine(false)
                more.visibility = View.GONE
            }
        } else {
            summary.visibility = View.GONE
            summary2.visibility = View.GONE
            more.visibility = View.GONE
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
            recyclerView.layoutManager = ScrollStartLayoutManager(this, ScrollStartLayoutManager.HORIZONTAL, false)
            recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                inner class VH(v: View) : RecyclerView.ViewHolder(v) {
                    val view = v
                    val tv = v.findViewById(R.id.tv) as TextView
                    val image = v.findViewById(R.id.image) as ImageView
                    val progress = v.findViewById(R.id.progress) as ProgressCoverView
                }

                override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, p1: Int) {
                    if (holder is VH) {
                        val d = detail.episodes[p1]

                        holder.tv.text = (p1 + 1).toString() + ". " + d.name_cn
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

                    }
                }

                override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): RecyclerView.ViewHolder {
                    return VH(LayoutInflater.from(p0!!.context).inflate(R.layout.rv_item_episode, p0, false))
                }

                override fun getItemCount(): Int {
                    return detail.episodes.size
                }

            }

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
}
