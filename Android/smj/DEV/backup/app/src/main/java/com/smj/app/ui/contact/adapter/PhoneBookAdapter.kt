package com.smj.app.ui.contact.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smj.app.R
import com.smj.app.ui.contact.model.PhoneSpot

class PhoneBookAdapter(
    mContext: Context,
    mData: ArrayList<PhoneSpot>,
    adapterCallback: PhoneBookAdapterCallback
) : RecyclerView.Adapter<PhoneBookAdapter.MyViewHolder>()  {

    private var mContext: Context
    private var mData: ArrayList<PhoneSpot>
    private val mAdapterCallback: PhoneBookAdapterCallback

    init {
        this.mData = mData
        this.mContext = mContext
        this.mAdapterCallback = adapterCallback
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PhoneBookAdapter.MyViewHolder {
        val v: View =
            LayoutInflater.from(mContext).inflate(R.layout.layout_item_phone_book, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: PhoneBookAdapter.MyViewHolder, position: Int) {
        holder.tvName.text = mData[position].name
        holder.tvPhone.text = mData[position].number
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvName: TextView = itemView.findViewById(R.id.tv_name)
        var tvPhone: TextView = itemView.findViewById(R.id.tv_phone)
        var read: LinearLayout = itemView.findViewById(R.id.ll_read)

        init {
            read.setOnClickListener {
                mAdapterCallback.onDetail(
                    bindingAdapterPosition,
                    mData
                )
            }

        }
    }

    interface PhoneBookAdapterCallback {
        fun onDetail(bindingAdapterPosition: Int, mData: ArrayList<PhoneSpot>)
    }
}