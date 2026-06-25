package com.smj.app.ui.contact.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smj.app.R
import com.smj.app.ui.contact.model.ContactList

class ContactConfirmAdapter(
    mContext: Context,
    mData: ArrayList<ContactList>,
    adapterCallback: ContactConfirmAdapterCallback
) : RecyclerView.Adapter<ContactConfirmAdapter.MyViewHolder>()  {

    private var mContext: Context
    private var mData: ArrayList<ContactList>
    private val mAdapterCallback: ContactConfirmAdapterCallback

    init {
        this.mData = mData
        this.mContext = mContext
        this.mAdapterCallback = adapterCallback
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ContactConfirmAdapter.MyViewHolder {
        val v: View =
            LayoutInflater.from(mContext).inflate(R.layout.layout_item_contact_confirm, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: ContactConfirmAdapter.MyViewHolder, position: Int) {
        holder.tvName.text = mData[position].getFullName()
        holder.tvJob.text = mData[position].getPosition()
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvName: TextView = itemView.findViewById(R.id.tv_name)
        var tvPhone: TextView = itemView.findViewById(R.id.tv_phone)
        var tvJob: TextView = itemView.findViewById(R.id.tv_job)
        var tvSender: TextView = itemView.findViewById(R.id.tv_sender)

        init {
        }
    }

    interface ContactConfirmAdapterCallback {
    }
}