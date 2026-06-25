package com.smj.app.ui.fleet.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smj.app.R
import com.smj.app.helper.FormatNumber
import com.smj.app.ui.fleet.model.UnitList

class UnitConfirmAdapter(
    mContext: Context,
    mData: ArrayList<UnitList>,
    adapterCallback: ProductConfirmAdapterCallback
) : RecyclerView.Adapter<UnitConfirmAdapter.MyViewHolder>()  {

    private var mContext: Context
    private var mData: ArrayList<UnitList>
    private val mAdapterCallback: ProductConfirmAdapterCallback
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
    ): UnitConfirmAdapter.MyViewHolder {
        val v: View =
            LayoutInflater.from(mContext).inflate(R.layout.layout_item_product_confirm, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: UnitConfirmAdapter.MyViewHolder, position: Int) {
        holder.unitCode.text = mData[position].getUnitCode()
        holder.unitType.text = mData[position].getUnitType()
        holder.unitId.text = mData[position].getUnitId()
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var unitId: CheckBox = itemView.findViewById(R.id.unit_id)
        var unitCode: TextView = itemView.findViewById(R.id.tv_unit_code)
        var unitType: TextView = itemView.findViewById(R.id.tv_unit_type)

        init {
        }
    }

    interface ProductConfirmAdapterCallback {
    }
}