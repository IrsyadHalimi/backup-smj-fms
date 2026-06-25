package com.smj.app.ui.contact.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smj.app.R
import com.smj.app.ui.contact.model.ContactList

class UserAdapter(
    mContext: Context,
    mData: ArrayList<ContactList>,
    adapterCallback: UserAdapterCallback
) : RecyclerView.Adapter<UserAdapter.MyViewHolder>()  {

    private var mContext: Context
    private var mData: ArrayList<ContactList>
    private val mAdapterCallback: UserAdapterCallback

    init {
        this.mData = mData
        this.mContext = mContext
        this.mAdapterCallback = adapterCallback
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserAdapter.MyViewHolder {
        val v: View =
            LayoutInflater.from(mContext).inflate(R.layout.layout_item_contact, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: UserAdapter.MyViewHolder, position: Int) {
        holder.tvName.text = mData[position].getFullName()
        holder.tvJob.text = mData[position].getPosition()
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvName: TextView = itemView.findViewById(R.id.tv_name)
        var tvJob: TextView = itemView.findViewById(R.id.tv_job)
        var llContact: LinearLayout = itemView.findViewById(R.id.ll_contact)
        var llCall: LinearLayout = itemView.findViewById(R.id.ll_call)
        var llWa: LinearLayout = itemView.findViewById(R.id.ll_wa)
        var llEmail: LinearLayout = itemView.findViewById(R.id.ll_email)

        init {
            if(mAdapterCallback.javaClass.simpleName.equals("ContactListActivity")){
                llWa.visibility = View.GONE
                llCall.visibility = View.GONE
                llEmail.visibility = View.GONE
            }
            llContact.setOnClickListener {
                mAdapterCallback.OnDetail(
                    bindingAdapterPosition,
                    mData
                )
            }
            llWa.setOnClickListener {
                mAdapterCallback.WhatsApp(
                    bindingAdapterPosition,
                    mData
                )
            }
            llCall.setOnClickListener {
                mAdapterCallback.Call(
                    bindingAdapterPosition,
                    mData
                )
            }
            llEmail.setOnClickListener {
                mAdapterCallback.Email(
                    bindingAdapterPosition,
                    mData
                )
            }

        }
    }

    interface UserAdapterCallback {
        fun OnDetail(bindingAdapterPosition: Int, mData: ArrayList<ContactList>)
        fun WhatsApp(bindingAdapterPosition: Int, mData: ArrayList<ContactList>)
        fun Call(bindingAdapterPosition: Int, mData: java.util.ArrayList<ContactList>)
        fun Email(bindingAdapterPosition: Int, mData: java.util.ArrayList<ContactList>)
    }
}