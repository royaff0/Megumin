package com.sqrtf.megumin.homefragment

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.sqrtf.common.StringUtil
import com.sqrtf.common.model.Bangumi
import com.sqrtf.megumin.R

class HomeLineAdapter(ndatas: HomeData, callback: OnClickListener?) : RecyclerView.Adapter<MediumCardHolder>() {

    interface OnClickListener {
        fun onClick(b: Bangumi)
    }

    private var datas: HomeData = ndatas
    private var callback: OnClickListener? = callback

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediumCardHolder? {
        return MediumCardHolder(LayoutInflater.from(parent.context).inflate(R.layout.include_bangumi_medium, parent, false))
    }

    override fun onBindViewHolder(holder: MediumCardHolder, position: Int) {
        val bangumi = datas.datas?.get(position) ?: return

        holder.title.text = StringUtil.mainTitle(bangumi)

        holder.subtitle.text =
                if (bangumi.status == 1)
                    holder.subtitle.resources.getString(R.string.unwatched).format(bangumi.unwatched_count)
                else bangumi.air_date

        Glide.with(holder.image.context)
                .load(bangumi.image)
                .into(holder.image)

        holder.itemView.setOnClickListener { callback?.onClick(bangumi) }
    }

    override fun getItemCount(): Int {
        datas.datas?.let { return it.size }
        return 0
    }
}
