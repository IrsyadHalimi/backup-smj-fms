package com.smj.app.ui.task.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smj.app.R
import com.smj.app.helper.FormatNumber
import com.smj.app.helper.Helper
import com.smj.app.ui.main.view.adapter.TaskPlanDetailAdapter
import com.smj.app.ui.pengawas.model.FleetHistoryTime
import com.smj.app.ui.task.model.PlanUnit
import com.smj.app.ui.task.model.TaskGroupList

class TaskAdapter(
    private var mContext: Context,
    private var mData: ArrayList<TaskGroupList>,
    adapterCallback: TaskAdapterCallback
) : RecyclerView.Adapter<TaskAdapter.MyViewHolder>()  {

    private val mAdapterCallback: TaskAdapterCallback = adapterCallback
    private var planUnitList: ArrayList<PlanUnit>? = null
    var count = 0

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskAdapter.MyViewHolder {
        val v: View =
            LayoutInflater.from(mContext).inflate(R.layout.layout_item_task, parent, false)
        return MyViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TaskAdapter.MyViewHolder, position: Int) {
        holder.tvUnitExe.text = HtmlCompat.fromHtml("<strong>"+mData[position].getExaCode()+"</strong> <br/><i><small>Exavator</small></i>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        holder.tvOperator.text = HtmlCompat.fromHtml("<strong>"+mData[position].getOperatorName()+"</strong> <br/><i><small>Operator</small></i>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        holder.tvOperatorLabel.text = HtmlCompat.fromHtml("<i>Operator</i>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        holder.tvPengawas.text = mData[position].getPengawasName()
        holder.tvPengawasLabel.text = HtmlCompat.fromHtml("<i>Pengawas</i>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        holder.tvStatus.text = Helper().capitalizeWords(mData[position].getStatus())
        holder.tvStatusLabel.text = HtmlCompat.fromHtml("<i>Status</i>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        holder.tvRit.text = mData[position].getTargetRit()
        holder.tvRitLabel.text = HtmlCompat.fromHtml("<i>Ritase/Target</i>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        holder.tvLocation.text = mData[position].getLocationName()+" - "+mData[position].getGalianName()
        holder.tvLocationLabel.text = HtmlCompat.fromHtml("<i>Lokasi Galian</i>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        holder.tvTimbunan.text = mData[position].getTimbunanName()
        holder.tvTimbunanLabel.text = HtmlCompat.fromHtml("<i>Lokasi Timbunan</i>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        holder.tvCalender.text = HtmlCompat.fromHtml("<i>"+mData[position].getDate()+"</i>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        holder.tvJarak.text = HtmlCompat.fromHtml("<i>Jarak: </i>"+mData[position].getJarak()+"km", HtmlCompat.FROM_HTML_MODE_LEGACY)
        holder.tvTarget.text = HtmlCompat.fromHtml("<i>A/T: </i>0/"+mData[position].getPlan(), HtmlCompat.FROM_HTML_MODE_LEGACY)
        holder.tvShift.text = mData[position].getShift()
        holder.tvShiftLabel.text = HtmlCompat.fromHtml("<i>"+mData[position].getShiftTime()+"</i>", HtmlCompat.FROM_HTML_MODE_LEGACY)

        if (mData[position].getStatus().equals("done")) {
            holder.tvDetailMenu.visibility = View.GONE
        }

        planUnitList = ArrayList()
        planFleetUnit(
            mData[position].getTaskId(),
            holder.recyclerDetailFleetList,
            planUnitList,
            holder.tvTotalRitase,
            holder.tvTarget,
            mData[position].getPlan(),
            holder.tvBcm,
            holder.tvHistory
        )
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvUnitExe: TextView = itemView.findViewById(R.id.tv_unit_exe)
        var tvOperator: TextView = itemView.findViewById(R.id.tv_operator)
        var tvOperatorLabel: TextView = itemView.findViewById(R.id.tv_operator_label)
        var whatsappOperator: ImageView = itemView.findViewById(R.id.whatsapp_operator)
        var phoneOperator: ImageView = itemView.findViewById(R.id.phone_operator)
        var tvDetail: TextView = itemView.findViewById(R.id.tv_detail)
        var tvPengawas: TextView = itemView.findViewById(R.id.tv_pengawas)
        var tvPengawasLabel: TextView = itemView.findViewById(R.id.tv_pengawas_label)
        var tvStatus: TextView = itemView.findViewById(R.id.tv_status)
        var tvStatusLabel: TextView = itemView.findViewById(R.id.tv_status_label)
        var tvRit: TextView = itemView.findViewById(R.id.tv_rit)
        var tvRitLabel: TextView = itemView.findViewById(R.id.tv_rit_label)
        var tvLocation: TextView = itemView.findViewById(R.id.tv_location)
        var tvLocationLabel: TextView = itemView.findViewById(R.id.tv_location_label)
        var tvTimbunan: TextView = itemView.findViewById(R.id.tv_timbunan)
        var tvTimbunanLabel: TextView = itemView.findViewById(R.id.tv_timbunan_label)
        var tvCalender: TextView = itemView.findViewById(R.id.tv_calender)
        var tvJarak: TextView = itemView.findViewById(R.id.tv_jarak)
        var tvTarget: TextView = itemView.findViewById(R.id.tv_target)
        var tvShift: TextView = itemView.findViewById(R.id.tv_shift)
        var tvShiftLabel: TextView = itemView.findViewById(R.id.tv_shift_label)
        var recyclerDetailFleetList: RecyclerView = itemView.findViewById(R.id.recycler_detail_fleet_list)
        var ivUp: ImageView = itemView.findViewById(R.id.iv_up)
        var ivDown: ImageView = itemView.findViewById(R.id.iv_down)
        var tvDetailMenu: LinearLayout = itemView.findViewById(R.id.tv_detail_menu)
        var tvTotalRitase: TextView = itemView.findViewById(R.id.tv_total_ritase)
        var llBarFirst: LinearLayout = itemView.findViewById(R.id.ll_bar_first)
        var llBarSecond: LinearLayout = itemView.findViewById(R.id.ll_bar_second)
        var llSosial: LinearLayout = itemView.findViewById(R.id.ll_sosial)
        var tvBcm: TextView = itemView.findViewById(R.id.tv_bcm)
        var tvHistory: TextView = itemView.findViewById(R.id.tv_history)

        init {
            whatsappOperator.setOnClickListener {
                mAdapterCallback.whatsappOperator(
                    bindingAdapterPosition,
                    mData
                )
            }
            phoneOperator.setOnClickListener {
                mAdapterCallback.phoneOperator(
                    bindingAdapterPosition,
                    mData
                )
            }
            tvDetail.setOnClickListener {
                mAdapterCallback.onDetail(
                    bindingAdapterPosition,
                    mData
                )
            }
            ivUp.setOnClickListener {
                mAdapterCallback.ivUp(
                    bindingAdapterPosition,
                    mData,
                    recyclerDetailFleetList,
                    ivUp,
                    ivDown
                )
            }
            ivDown.setOnClickListener {
                mAdapterCallback.ivDown(
                    bindingAdapterPosition,
                    mData,
                    recyclerDetailFleetList,
                    ivUp,
                    ivDown
                )
            }
            tvDetailMenu.setOnClickListener { v ->
                mAdapterCallback.tvDetailMenu(
                    bindingAdapterPosition,
                    mData,
                    v
                )
            }
            tvUnitExe.setOnClickListener { v ->
                mAdapterCallback.tvUnitExe(
                    bindingAdapterPosition,
                    mData,
                    tvOperator,
                    tvUnitExe,
                    llBarFirst,
                    llBarSecond,
                    llSosial
                )
            }
            tvHistory.setOnClickListener {
                if(isNetworkAvailable(mContext)) {
                    mAdapterCallback.tvHistory(mData, bindingAdapterPosition)
                }
                else {
                    Toast.makeText(
                        mContext,
                        mContext.getString(R.string.connection_status),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    interface TaskAdapterCallback : TaskPlanDetailAdapter.TaskAdapterCallback {
        fun onDetail(bindingAdapterPosition: Int, mData: ArrayList<TaskGroupList>)
        fun ivUp(
            bindingAdapterPosition: Int,
            mData: ArrayList<TaskGroupList>,
            recyclerDetailFleetList: RecyclerView,
            ivUp: ImageView,
            ivDown: ImageView
        )
        fun ivDown(
            bindingAdapterPosition: Int,
            mData: ArrayList<TaskGroupList>,
            recyclerDetailFleetList: RecyclerView,
            ivUp: ImageView,
            ivDown: ImageView
        )
        fun tvDetailMenu(
            bindingAdapterPosition: Int,
            mData: ArrayList<TaskGroupList>,
            v: View
        )

        fun tvUnitExe(
            bindingAdapterPosition: Int,
            mData: ArrayList<TaskGroupList>,
            tvOperator: TextView,
            tvUnitExe: TextView,
            llBarFirst: LinearLayout,
            llBarSecond: LinearLayout,
            llSosial: LinearLayout
        )

        fun whatsappOperator(bindingAdapterPosition: Int, mData: java.util.ArrayList<TaskGroupList>)
        fun phoneOperator(bindingAdapterPosition: Int, mData: ArrayList<TaskGroupList>)

        fun tvHistory(mData: ArrayList<TaskGroupList>, bindingAdapterPosition: Int)

        override fun llMenuUnit(bindingAdapterPosition: Int, mData: ArrayList<PlanUnit>, v: View)
    }

    private fun planFleetUnit(
        taskId: String?,
        recyclerDetailFleetList: RecyclerView,
        planUnitList: ArrayList<PlanUnit>?,
        tvTotalRitase: TextView,
        tvTarget: TextView,
        plan: String?,
        tvBcm: TextView,
        tvHistory: TextView
    ) {
        val refTask = FirebaseDatabase.getInstance().reference
        refTask.child("TasksFleetPlanUnit")
            .orderByChild("taskId")
            .equalTo(taskId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    planUnitList?.clear()
                    if(snapshot.exists()) {
                        var count = 0
                        for (postSnapshot in snapshot.children) {
                            val planUnitDataList = postSnapshot.getValue(PlanUnit::class.java)
                            if (planUnitDataList?.getStatus() != "delete") {
                                planUnitList?.add(planUnitDataList!!)
                            }
                            val refRitase = FirebaseDatabase.getInstance().reference
                            refRitase.child("Ritase")
                                .child(planUnitDataList?.getTaskId().toString())
                                .child(planUnitDataList?.getTaskUnitId().toString())
                                .orderByChild("supirId")
                                .equalTo(planUnitDataList?.getSopirId())
                                .addChildEventListener(object : ChildEventListener {
                                    override fun onChildAdded(
                                        snapshot2: DataSnapshot,
                                        previousChildName: String?
                                    ) {
                                        if(snapshot2.exists()) {
//                                            Toast.makeText(mContext, snapshot2.child("taskId").value.toString()+" == "+taskId, Toast.LENGTH_LONG).show()
                                            if (snapshot2.child("status").value != "canceled" && snapshot2.child("taskId").value == taskId) {
                                                count += 1
                                                tvTotalRitase.text = count.toString()
                                                tvTarget.text = HtmlCompat.fromHtml(
                                                    "<i>A/T: </i>$count/$plan",
                                                    HtmlCompat.FROM_HTML_MODE_LEGACY
                                                )
                                                tvBcm.text = HtmlCompat.fromHtml("<i>"+ FormatNumber().simpleNumber((count*22))+"</i>", HtmlCompat.FROM_HTML_MODE_LEGACY)
                                            }
                                        }
                                    }

                                    override fun onChildChanged(
                                        snapshot2: DataSnapshot,
                                        previousChildName: String?
                                    ) {
                                        if(snapshot2.exists()) {
//                                            if (snapshot2.child("status").value != "canceled") {
//                                                count += 1
//                                                tvTotalRitase.text = count.toString()
//                                                tvTarget.text = HtmlCompat.fromHtml(
//                                                    "<i>Actual/Target: </i>$count/$plan",
//                                                    HtmlCompat.FROM_HTML_MODE_LEGACY
//                                                )
//                                            }
                                            if (snapshot2.child("status").value == "canceled" && snapshot2.child("taskId").value == taskId) {
                                                count -= 1
                                                tvTotalRitase.text = count.toString()
                                                tvTarget.text = HtmlCompat.fromHtml(
                                                    "<i>A/T: </i>$count/$plan",
                                                    HtmlCompat.FROM_HTML_MODE_LEGACY
                                                )
                                            }
                                        }
                                    }

                                    override fun onChildRemoved(snapshot2: DataSnapshot) {
                                        if(snapshot2.exists()) {
                                            if (snapshot2.child("status").value == "canceled" && snapshot2.child("taskId").value == taskId) {
                                                count -= 1
                                            }
                                            tvTotalRitase.text = count.toString()
                                            tvTarget.text = HtmlCompat.fromHtml(
                                                "<i>A/T: </i>$count/$plan",
                                                HtmlCompat.FROM_HTML_MODE_LEGACY
                                            )
                                        }
                                    }

                                    override fun onChildMoved(
                                        snapshot2: DataSnapshot,
                                        previousChildName: String?
                                    ) {
                                        if (snapshot2.child("status").value != "canceled" && snapshot2.child("taskId").value == taskId) {
//                                            count += 1
//                                            tvTotalRitase.text = count.toString()
//                                            tvTarget.text = HtmlCompat.fromHtml(
//                                                "<i>Actual/Target: </i>$count/$plan",
//                                                HtmlCompat.FROM_HTML_MODE_LEGACY
//                                            )
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }
                                })
                        }
                        planUnitList?.sortByDescending { it.getSopir() }
                        val planUnitAdapter = TaskPlanDetailAdapter(mContext, planUnitList!!, tvTotalRitase, mAdapterCallback, tvTarget, plan)
                        recyclerDetailFleetList.layoutManager = LinearLayoutManager(mContext)
                        recyclerDetailFleetList.adapter = planUnitAdapter

                        val refTasks = FirebaseDatabase.getInstance().reference
                            .child("FleetHistoryTime")
                            .child(taskId.toString())
                            .limitToLast(1)
                        refTasks.addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    for (postSnapshot in snapshot.children) {
                                        val fleetHistoryTime = postSnapshot.getValue(
                                            FleetHistoryTime::class.java)
                                        tvHistory.text = HtmlCompat.fromHtml("<i><u>"+fleetHistoryTime?.getRemark().toString()+"</u></i>", HtmlCompat.FROM_HTML_MODE_LEGACY)
                                        tvHistory.setTextColor(mContext.getColor(R.color.red))
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                    }
                    else{
                        recyclerDetailFleetList.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    recyclerDetailFleetList.visibility = View.GONE
                }

            })
    }

    @SuppressLint("MissingPermission")
    fun isNetworkAvailable(context: Context) =
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
            getNetworkCapabilities(activeNetwork)?.run {
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            } ?: false
        }
}