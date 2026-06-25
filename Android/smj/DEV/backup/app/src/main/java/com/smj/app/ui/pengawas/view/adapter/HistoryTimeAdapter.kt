package com.smj.app.ui.pengawas.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.smj.app.R
import com.smj.app.helper.DateHelper
import com.smj.app.ui.pengawas.model.FleetHistoryTime

class HistoryTimeAdapter(
    private var mContext: Context,
    private var mData: ArrayList<FleetHistoryTime>
) : RecyclerView.Adapter<HistoryTimeAdapter.MyViewHolder>()  {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val v: View =
            LayoutInflater.from(mContext).inflate(R.layout.layout_history_time, parent, false)
        return MyViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = mData[position]
        holder.tvHistoryLabel.text = HtmlCompat.fromHtml("<i>"+data.getRemark().toString()+"</i>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        val time = data.getTimestamp()?.split(" ")?.get(1)
        holder.tvTimeHistory.text = time?.split(":")?.get(0)+":"+time?.split(":")?.get(1)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvHistoryLabel: TextView = itemView.findViewById(R.id.tv_history_label)
        var tvTimeHistory: TextView = itemView.findViewById(R.id.tv_time_history)
    }
}