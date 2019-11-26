package com.sqrtf.megumin.homefragment

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.sqrtf.common.StringUtil
import com.sqrtf.common.api.ApiHelper
import com.sqrtf.common.model.Bangumi
import com.sqrtf.megumin.R

open class HomeHorizontalAdapter(private var datas: HomeData,
                                 private var callback: (Bangumi) -> Unit) : RecyclerView.Adapter<MediumCardHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediumCardHolder {
        return MediumCardHolder(LayoutInflater.from(parent.context).inflate(R.layout.include_bangumi_medium, parent, false))
    }

    override fun onBindViewHolder(holder: MediumCardHolder, position: Int) {
        val bangumi = datas.datas?.get(position) ?: return

        holder.title.text = StringUtil.mainTitle(bangumi)
        holder.subtitle?.text = StringUtil.subTitle(bangumi)
        holder.time?.text = bangumi.air_date
        holder.new?.text = holder.itemView.resources.getString(R.string.unwatched).format(bangumi.unwatched_count)
        holder.eps?.text = holder.itemView.resources.getString(R.string.eps_all).format(bangumi.eps)

        Glide.with(holder.image.context)
                .load(bangumi.cover_image.fixedUrl())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.image)

        holder.itemView.setOnClickListener { callback.invoke(bangumi) }
    }

    override fun getItemCount(): Int {
        datas.datas?.let { return it.size }
        return 0
    }
}
