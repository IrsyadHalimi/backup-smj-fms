package com.smj.app.ui.pengawas.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smj.app.R
import com.smj.app.ui.task.model.PlanUnit

class FleetPlanUnitAdapter(
    mContext: Context,
    mData: ArrayList<PlanUnit>,
    adapterCallback: FleetPlanUnitAdapterCallback
) : RecyclerView.Adapter<FleetPlanUnitAdapter.MyViewHolder>()  {

    private var mContext: Context
    private var mData: ArrayList<PlanUnit>
    private val mAdapterCallback: FleetPlanUnitAdapterCallback

    init {
        this.mData = mData
        this.mContext = mContext
        this.mAdapterCallback = adapterCallback
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val v: View =
            LayoutInflater.from(mContext).inflate(R.layout.layout_plan_unit, parent, false)
        return MyViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = mData[position]
        holder.tvUnit.text = data.getUnitCode()
        holder.tvSopir.text = data.getSopir()
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvUnit: TextView = itemView.findViewById(R.id.tv_unit)
        var tvSopir: TextView = itemView.findViewById(R.id.tv_sopir)
        var ivRemove: ImageView = itemView.findViewById(R.id.iv_remove)

        init {
            ivRemove.setOnClickListener {
                mAdapterCallback.onRemove(
                    bindingAdapterPosition,
                    mData
                )
            }
        }
    }

    interface FleetPlanUnitAdapterCallback {
        fun onRemove(bindingAdapterPosition: Int, mData: ArrayList<PlanUnit>)
    }
}