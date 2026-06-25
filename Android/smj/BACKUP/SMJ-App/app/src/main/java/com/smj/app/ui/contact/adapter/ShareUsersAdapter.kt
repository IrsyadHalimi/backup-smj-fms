package com.smj.app.ui.contact.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.smj.app.R
import com.smj.app.ui.auth.model.Users
import com.squareup.picasso.Picasso

class ShareUsersAdapter(
    mContext: Context,
    mData: ArrayList<Users>
) : RecyclerView.Adapter<ShareUsersAdapter.MyViewHolder>()  {

    private var mContext: Context
    private var mData: ArrayList<Users>

    init {
        this.mData = mData
        this.mContext = mContext
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ShareUsersAdapter.MyViewHolder {
        val v: View =
            LayoutInflater.from(mContext).inflate(R.layout.layout_share_user, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: ShareUsersAdapter.MyViewHolder, position: Int) {
        val data = mData[position]
        holder.tvFullName.text = data.getFullName()
        holder.tvEmail.text = data.getEmail()
        if (data.getPhoto()?.isNotEmpty()  == true) {
            Glide.with(mContext)
                .load(data.getPhoto())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(
                    ContextCompat.getDrawable(
                        mContext,
                        R.drawable.icon_user_light
                    )
                )
                .into(holder.ivProfile)
        }
        else{
            Picasso.get().load(R.drawable.icon_user_light).placeholder(R.drawable.icon_user_light)
                .into(holder.ivProfile)
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvFullName: TextView = itemView.findViewById(R.id.tv_fullname)
        var tvEmail: TextView = itemView.findViewById(R.id.tv_email)
        var ivProfile: ImageView = itemView.findViewById(R.id.iv_profil)

        init {
        }
    }

    interface ShareUsersAdapterCallback {
    }
}