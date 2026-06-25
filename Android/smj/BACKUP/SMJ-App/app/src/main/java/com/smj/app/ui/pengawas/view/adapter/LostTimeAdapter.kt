package com.smj.app.ui.pengawas.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smj.app.R
import com.smj.app.helper.FormatNumber
import com.smj.app.ui.settings.model.LabelingLostTimeList

class LostTimeAdapter(
    mContext: Context,
    mData: ArrayList<LabelingLostTimeList>,
    adapterCallback: LostTimeAdapterCallback
) : RecyclerView.Adapter<LostTimeAdapter.MyViewHolder>()  {

    private var mContext: Context
    private var mData: ArrayList<LabelingLostTimeList>
    private val mAdapterCallback: LostTimeAdapterCallback
    private var formatNumber: FormatNumber? = null

    init {
        this.mData = mData
        this.mContext = mContext
        this.mAdapterCallback = adapterCallback
        this.formatNumber = FormatNumber()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LostTimeAdapter.MyViewHolder {
        val v: View =
            LayoutInflater.from(mContext).inflate(R.layout.layout_item_lost_time, parent, false)
        return MyViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LostTimeAdapter.MyViewHolder, position: Int) {
        holder.labelingName.text = mData[position].getLabelingLostName()
        holder.labelingId.text = mData[position].getLabelingLostId()
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var labelingId: CheckBox = itemView.findViewById(R.id.labeling_id)
        var labelingName: TextView = itemView.findViewById(R.id.tv_labeling_name)
        var detail: ImageView = itemView.findViewById(R.id.detail)

        init {
            detail.setOnClickListener {
                mAdapterCallback.onDetail(
                    bindingAdapterPosition,
                    mData
                )
            }
        }
    }

    interface LostTimeAdapterCallback {
        fun onDetail(adapterPosition: Int, labelingLostTimeList: ArrayList<LabelingLostTimeList>)

    }
}