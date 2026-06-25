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
import com.smj.app.ui.settings.model.LocationList

class LocationAdapter(
    mContext: Context,
    mData: ArrayList<LocationList>,
    adapterCallback: LocationAdapterCallback,
    position: String?
) : RecyclerView.Adapter<LocationAdapter.MyViewHolder>()  {

    private var mContext: Context
    private var mData: ArrayList<LocationList>
    private val mAdapterCallback: LocationAdapterCallback
    private var formatNumber: FormatNumber? = null
    private var mPosition: String? = position

    init {
        this.mData = mData
        this.mContext = mContext
        this.mAdapterCallback = adapterCallback
        this.formatNumber = FormatNumber()
        this.mPosition = position
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LocationAdapter.MyViewHolder {
        val v: View =
            LayoutInflater.from(mContext).inflate(R.layout.layout_item_location, parent, false)
        return MyViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LocationAdapter.MyViewHolder, position: Int) {
        holder.locationName.text = mData[position].getLocationName()
        holder.locationId.text = mData[position].getLocationId()
        if (FirebaseAuth.getInstance().currentUser?.uid != mData[position].getUid()){
            holder.detail.visibility = View.GONE
        }
        if (
            mPosition.equals("Manager HRGA")
            || mPosition.equals("MT MCC")
            || mPosition.equals("MCC Field")
            || mPosition.equals("Jr.Foreman MCC")
            || mPosition.equals("Foreman Produksi")
            || mPosition.equals("Sr Foreman Produksi")
            || mPosition.equals("Jr Foreman Produksi")
            || mPosition.equals("SPV Produksi")
            || mPosition.equals("Jr SPV Produksi")
            )
        {
            holder.detail.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var locationId: CheckBox = itemView.findViewById(R.id.location_id)
        var locationName: TextView = itemView.findViewById(R.id.tv_location_name)
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

    interface LocationAdapterCallback {
        fun onDetail(adapterPosition: Int, locationList: ArrayList<LocationList>)

    }
}