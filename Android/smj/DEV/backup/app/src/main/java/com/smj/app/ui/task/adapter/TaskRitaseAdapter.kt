package com.smj.app.ui.task.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smj.app.R
import com.smj.app.helper.DateHelper
import com.smj.app.ui.main.view.adapter.TaskPlanDetailAdapter
import com.smj.app.ui.pengawas.model.Ritase
import com.smj.app.ui.pengawas.view.adapter.FleetPlanDetailAdapter

class TaskRitaseAdapter(
    private var mContext: Context,
    private var mData: ArrayList<Ritase>,
    tvCtRite: TextView,
    var taskId: String,
    adapterCallback: TaskPlanDetailAdapter.TaskAdapterCallback
) : RecyclerView.Adapter<TaskRitaseAdapter.MyViewHolder>()  {

    var ritase: ArrayList<Ritase>? = null
    private lateinit var time2: String
    private var circleTime: Long = 0
    private var tvCtRite: TextView = tvCtRite
    private val mAdapterCallback: TaskPlanDetailAdapter.TaskAdapterCallback = adapterCallback
    private var t:String? = null
    private var n:Int = 0
    var s:String? = null
    var s2:String? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val v: View =
            LayoutInflater.from(mContext).inflate(R.layout.layout_ritase, parent, false)
        return MyViewHolder(v)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = mData[position]

        val time = DateHelper().timeStimeToTimeStringId(mData[position].getTimeStamp().toString())
        holder.tvTime.text = time.split(":")[0] +":"+ time.split(":")[1]

        if(position>0){
            time2 = data.getTimeStamp()?.split(" ")?.get(1).toString()
            holder.tvCt.visibility = View.VISIBLE

            s = DateHelper().timeStimeToDate(mData[position-1].getTimeStamp().toString(), mData[position].getTimeStamp().toString())
            s2 = DateHelper().dateToTimeStime(mData[position-1].getTimeStamp().toString(), mData[position].getTimeStamp().toString())
            circleTime += s2!!.toLong()
            holder.tvCt.text = Html.fromHtml("<i> CT: $s</i>", Html.FROM_HTML_MODE_LEGACY)
            tvCtRite.text = "Rata2 CT: "+DateHelper().timeStimeToDate2((circleTime.toInt()/position).toLong())
        }
        else{
            t = time.split(":")[0]
            holder.tvHeadingTime.visibility = View.VISIBLE
        }

        if(t != time.split(":")[0]) {
            t = time.split(":")[0]
            n = 0
            holder.tvHeadingTime.visibility = View.VISIBLE
        }
        holder.tvHeadingTime.text = time.split(":")[0] + ":00"

        if(t == time.split(":")[0]) {
            n += 1
            if((position+1) != mData.size){
                holder.tvCountHour.text = n.toString()
                if (time.split(":")[0] != mData[position+1].getTimeStamp()!!.split(" ")[1].split(":")[0]) {
                    holder.tvCountHour.visibility = View.VISIBLE
//                    holder.llCountHour.visibility = View.VISIBLE
                }
            }
            else{
                holder.tvCountHour.text = n.toString()
                holder.tvCountHour.visibility = View.VISIBLE
//                holder.llCountHour.visibility = View.VISIBLE
            }
        }

        listRemark(mData, position, holder.tvRemark)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvCt: TextView = itemView.findViewById(R.id.tv_ct)
        var tvTime: TextView = itemView.findViewById(R.id.tv_time)
        var tvRemark: TextView = itemView.findViewById(R.id.tv_remark)
        var tvHeadingTime: TextView = itemView.findViewById(R.id.tv_heading_time)
        var tvCountHour: TextView = itemView.findViewById(R.id.tv_count_hour)
        var llCountHour: LinearLayout = itemView.findViewById(R.id.ll_count_hour)

        init {
            tvRemark.setOnClickListener { v ->
                mAdapterCallback.showFormRemark(
                    bindingAdapterPosition,
                    mData,
                    v,
                    tvRemark
                )
            }
        }
    }

    private fun listRemark(
        mData: ArrayList<Ritase>,
        position: Int,
        tvRemark: TextView,
    ) {
        val refShareContact = FirebaseDatabase.getInstance().reference
        refShareContact.child("Remark")
            .child(mData[position].getRitaseId().toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        tvRemark.text = HtmlCompat.fromHtml("<small><i>"+ snapshot.child("remark").value.toString() +"</i></small>...", HtmlCompat.FROM_HTML_MODE_LEGACY)
                        tvRemark.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_edit_24, 0)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}