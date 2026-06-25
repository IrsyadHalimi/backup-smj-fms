package com.smj.app.ui.task.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smj.app.R
import com.smj.app.ui.task.model.TaskGroupList

class TaskSearchAdapter(
    mContext: Context,
    mData: ArrayList<TaskGroupList>
) : RecyclerView.Adapter<TaskSearchAdapter.MyViewHolder>()  {

    private var mContext: Context
    private var mData: ArrayList<TaskGroupList>

    init {
        this.mData = mData
        this.mContext = mContext
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskSearchAdapter.MyViewHolder {
        val v: View =
            LayoutInflater.from(mContext).inflate(R.layout.layout_item_task, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: TaskSearchAdapter.MyViewHolder, position: Int) {
//        holder.tvTitle.text = mData[position].getCategory()
//        holder.tvSubTitle.text = mData[position].getContact()
//        holder.tvDueDate.text = mData[position].getDueDate()
//        holder.tvNote.text = mData[position].getNote()
//        if (mData[position].getPriority() == 1) {
//            holder.tvPriority.background =
//                ContextCompat.getDrawable(mContext, R.drawable.bg_circle_green)
//        }
//        if (mData[position].getPriority() == 2) {
//            holder.tvPriority.background =
//                ContextCompat.getDrawable(mContext, R.drawable.bg_circle_yellow)
//        }
//        if (mData[position].getPriority() == 3) {
//            holder.tvPriority.background =
//                ContextCompat.getDrawable(mContext, R.drawable.bg_circle_red)
//        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvUnitExe: TextView = itemView.findViewById(R.id.tv_unit_exe)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tv_operator)
        var tvDetail: TextView = itemView.findViewById(R.id.tv_detail)
        var tvNote: TextView = itemView.findViewById(R.id.tv_pengawas)
        var llDetail: LinearLayout = itemView.findViewById(R.id.ll_detail)

    }
}