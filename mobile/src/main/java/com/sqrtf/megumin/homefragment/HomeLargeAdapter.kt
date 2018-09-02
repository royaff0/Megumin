package com.sqrtf.megumin.homefragment

import android.view.LayoutInflater
import android.view.ViewGroup
import com.sqrtf.common.model.Bangumi
import com.sqrtf.megumin.R

class HomeLargeAdapter(
        ndatas: HomeData,
        callback: (Bangumi) -> Unit
) : HomeHorizontalAdapter(ndatas, callback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediumCardHolder? {
        return MediumCardHolder(LayoutInflater.from(parent.context).inflate(R.layout.include_bangumi_large, parent, false))
    }
}