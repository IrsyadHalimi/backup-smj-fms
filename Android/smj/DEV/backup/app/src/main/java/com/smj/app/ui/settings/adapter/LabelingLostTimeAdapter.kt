package com.smj.app.ui.settings.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.smj.app.R
import com.smj.app.helper.FormatNumber
import com.smj.app.ui.settings.model.LabelingLostTimeList

class LabelingLostTimeAdapter(
    mContext: Context,
    mData: ArrayList<LabelingLostTimeList>,
    adapterCallback: LabelingLostTimeAdapterCallback,
    userRole: String?
) : RecyclerView.Adapter<LabelingLostTimeAdapter.MyViewHolder>()  {

    private var mContext: Context
    private var mData: ArrayList<LabelingLostTimeList>
    private val mAdapterCallback: LabelingLostTimeAdapterCallback
    private var formatNumber: FormatNumber? = null
    private var userRole: String?

    init {
        this.mData = mData
        this.mContext = mContext
        this.mAdapterCallback = adapterCallback
        this.formatNumber = FormatNumber()
        this.userRole = userRole
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LabelingLostTimeAdapter.MyViewHolder {
        val v: View =
            LayoutInflater.from(mContext).inflate(R.layout.layout_item_label_lost, parent, false)
        return MyViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LabelingLostTimeAdapter.MyViewHolder, position: Int) {
        holder.labelingName.text = mData[position].getLabelingLostName()
        holder.labelingId.text = mData[position].getLabelingLostId()
        if (FirebaseAuth.getInstance().currentUser?.uid == mData[position].getUid() || userRole == "ADMIN" || userRole == "root"){
            holder.detail.visibility = View.VISIBLE
        }
        else{
            holder.detail.visibility = View.GONE
        }
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

    interface LabelingLostTimeAdapterCallback {
        fun onDetail(adapterPosition: Int, labelingLostTimeList: ArrayList<LabelingLostTimeList>)

    }
}