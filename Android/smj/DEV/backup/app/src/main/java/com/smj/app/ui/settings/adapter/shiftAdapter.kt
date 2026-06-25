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
import com.smj.app.R
import com.smj.app.helper.FormatNumber
import com.smj.app.ui.settings.model.ShiftList

class ShiftAdapter(
    mContext: Context,
    mData: ArrayList<ShiftList>,
    adapterCallback: ShiftAdapterCallback
) : RecyclerView.Adapter<ShiftAdapter.MyViewHolder>()  {

    private var mContext: Context
    private var mData: ArrayList<ShiftList>
    private val mAdapterCallback: ShiftAdapterCallback
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
    ): ShiftAdapter.MyViewHolder {
        val v: View =
            LayoutInflater.from(mContext).inflate(R.layout.layout_item_shift, parent, false)
        return MyViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ShiftAdapter.MyViewHolder, position: Int) {
        holder.shiftName.text = mData[position].getShiftName()
        holder.shiftTime.text = mData[position].getShiftTimeStart()+" s/d "+mData[position].getshiftTimeEnd()
        holder.shiftId.text = mData[position].getShiftId()
//        if (FirebaseAuth.getInstance().currentUser?.uid != mData[position].getUid()){
//            holder.detail.visibility = View.GONE
//        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var shiftId: CheckBox = itemView.findViewById(R.id.shift_id)
        var shiftName: TextView = itemView.findViewById(R.id.tv_shift_name)
        var shiftTime: TextView = itemView.findViewById(R.id.tv_shift_time)
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

    interface ShiftAdapterCallback {
        fun onDetail(adapterPosition: Int, shiftList: ArrayList<ShiftList>)

    }
}