package com.sqrtf.megumin.homefragment

import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.sqrtf.megumin.R

class MediumCardHolder(view: View) : ViewHolder(view) {
    val image = view.findViewById<ImageView>(R.id.imageView)
    val title = view.findViewById<TextView>(R.id.title)
    val subtitle = view.findViewById<TextView?>(R.id.subtitle)
    val time = view.findViewById<TextView?>(R.id.time)
    val new = view.findViewById<TextView?>(R.id.new_count)
    val eps = view.findViewById<TextView?>(R.id.eps)
}
