package com.smj.app.ui.pengawas.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.CountDownTimer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smj.app.R
import com.smj.app.helper.DateHelper
import com.smj.app.helper.FormatNumber
import com.smj.app.helper.Helper
import com.smj.app.ui.pengawas.model.FleetHistoryTime
import com.smj.app.ui.pengawas.model.LoadingTime
import com.smj.app.ui.pengawas.model.Ritase
import com.smj.app.ui.task.model.PlanUnit
import com.smj.app.ui.task.model.TaskGroupList
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FleetPlanAdapter(
    private var mContext: Context,
    private var mData: ArrayList<TaskGroupList>,
    adapterCallback: FleetPlanAdapterCallback
) : RecyclerView.Adapter<FleetPlanAdapter.MyViewHolder>()  {

    private lateinit var databaseReference: DatabaseReference
    private val mAdapterCallback: FleetPlanAdapterCallback = adapterCallback
    private var formatNumber: FormatNumber? = null
    private var planUnitList: ArrayList<PlanUnit>? = null
    var count = 0
    var different: Long = 0

    private lateinit var countDownTimer:CountDownTimer
    var different2: Long = 0

    init {
        this.formatNumber = FormatNumber()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val v: View =
            LayoutInflater.from(mContext).inflate(R.layout.layout_item_fleet, parent, false)
        return MyViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvUnitExe.text = HtmlCompat.fromHtml("<strong>"+mData[position].getExaCode()+"</strong> <br/><i><small>Exavator</small></i>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        holder.tvOperator.text = HtmlCompat.fromHtml("<strong>"+mData[position].getOperatorName()+"</strong> <br/><i><small>Operator</small></i>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        holder.tvOperatorLabel.text = HtmlCompat.fromHtml("<i>Operator</i>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        holder.tvPengawas.text = mData[position].getPengawasName()
        holder.tvPengawasLabel.text = HtmlCompat.fromHtml("<i>Pengawas</i>", HtmlCompat.FROM_HTML_MODE_LEGACY)
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
        holder.tvStatus.text = Helper().capitalizeWords(mData[position].getStatus())
        holder.tvStatusLabel.text = HtmlCompat.fromHtml("<i>Status</i>", HtmlCompat.FROM_HTML_MODE_LEGACY)

        if (mData[position].getStatus().equals("todo")) {
            holder.tvDetailMenu.visibility = View.GONE
            holder.tvStartShift.visibility = View.VISIBLE
            holder.tvLoadingStart.visibility = View.GONE
            holder.tvLoadingStart.isEnabled = false
            holder.llStopShift.visibility = View.GONE
        }
        if (mData[position].getStatus().equals("doing")) {
            holder.tvDetailMenu.visibility = View.VISIBLE
            holder.tvStartShift.visibility = View.GONE
            holder.tvLoadingStart.visibility = View.VISIBLE
            holder.tvLoadingStart.isEnabled = false
            holder.tvLoadingStart.background = ContextCompat.getDrawable(mContext, R.drawable.bg_rounded_fill_grey)
            holder.tvLoadingStart.setTextColor(ContextCompat.getColor(mContext, R.color.black))
            holder.tvLoadingStart.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_play_arrow_24, 0)
//            holder.llStopShift.visibility = View.VISIBLE
            holder.tvStopShift.isEnabled = true
//            holder.tvStopShift.visibility = View.VISIBLE
            holder.tvStopPending.visibility = View.GONE
        }
        if (mData[position].getStatus().equals("done")) {
            holder.tvDetailMenu.visibility = View.GONE
            holder.tvStartShift.visibility = View.GONE
            holder.tvLoadingStart.visibility = View.GONE
            holder.tvLoadingStart.isEnabled = false
            holder.tvLoadingStop.visibility = View.GONE
            holder.tvLoadingStop.isEnabled = false
            holder.llStopShift.visibility = View.GONE
            holder.tvShiftOver.visibility = View.VISIBLE
        }

        planUnitList = ArrayList()
        planFleetUnit(
            mData[position].getTaskId(),
            holder.recyclerDetailFleetList,
            planUnitList,
            holder.tvTotalRitase,
            holder.tvTarget,
            mData[position].getPlan(),
            mData[position].getExaId(),
            holder.tvLoadingStop,
            holder.tvLoadingStart,
            holder.ivDown,
            holder.ivUp,
            "",
            mData[position].getStatus(),
            holder.tvStopPending,
            holder.tvStopShift,
            holder.tvBcm,
            holder.tvHistory,
            holder.tvStatusLoading,
            mData[position].getStatus()
        )

        val timeStamp = Helper().dateTimeNow()
        printDifferenceDateForHours(
            timeStamp,
            holder.tvEndShift,
            mData,
            position
        )
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvUnitExe: TextView = itemView.findViewById(R.id.tv_unit_exe)
        var tvOperator: TextView = itemView.findViewById(R.id.tv_operator)
        var tvOperatorLabel: TextView = itemView.findViewById(R.id.tv_operator_label)
        var tvDetail: LinearLayout = itemView.findViewById(R.id.tv_detail)
        var tvLostTime: LinearLayout = itemView.findViewById(R.id.tv_lost_time)
        var tvPengawas: TextView = itemView.findViewById(R.id.tv_pengawas)
        var tvPengawasLabel: TextView = itemView.findViewById(R.id.tv_pengawas_label)
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
        var tvStatus: TextView = itemView.findViewById(R.id.tv_status)
        var tvStatusLabel: TextView = itemView.findViewById(R.id.tv_status_label)
        var recyclerDetailFleetList: RecyclerView = itemView.findViewById(R.id.recycler_detail_fleet_list)
        var ivUp: ImageView = itemView.findViewById(R.id.iv_up)
        var ivDown: ImageView = itemView.findViewById(R.id.iv_down)
        var tvDetailMenu: LinearLayout = itemView.findViewById(R.id.tv_detail_menu)
        var tvTotalRitase: TextView = itemView.findViewById(R.id.tv_total_ritase)
        var llBarFirst: LinearLayout = itemView.findViewById(R.id.ll_bar_first)
        var llBarSecond: LinearLayout = itemView.findViewById(R.id.ll_bar_second)
        var llSosial: LinearLayout = itemView.findViewById(R.id.ll_sosial)
        var tvLoadingStart: TextView = itemView.findViewById(R.id.tv_loading_start)
        var tvStartShift: TextView = itemView.findViewById(R.id.tv_start_shift)
        var tvStopShift: TextView = itemView.findViewById(R.id.tv_stop_shift)
        var tvLoadingStop: TextView = itemView.findViewById(R.id.tv_loading_stop)
        var tvLoadingId: TextView = itemView.findViewById(R.id.tv_loading_id)
        var llStopShift: LinearLayout = itemView.findViewById(R.id.ll_stop_shift)
        var tvShiftOver: TextView = itemView.findViewById(R.id.tv_shift_over)
        var tvStopPending: TextView = itemView.findViewById(R.id.tv_stop_pending)
        var tvBcm: TextView = itemView.findViewById(R.id.tv_bcm)
        var tvHistory: TextView = itemView.findViewById(R.id.tv_history)
        var tvStatusLoading: TextView = itemView.findViewById(R.id.tv_status_loading)
        var tvEndShift: TextView = itemView.findViewById(R.id.tv_end_shift)

        init {
            tvDetail.setOnClickListener {
                mAdapterCallback.onDetail(
                    bindingAdapterPosition,
                    mData,
                    ivDown
                )
            }
            tvLostTime.setOnClickListener {
                mAdapterCallback.onLostTime(
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
                    recyclerDetailFleetList,
                    tvTotalRitase,
                    tvTarget,
                    tvLoadingStop,
                    tvLoadingStart,
                    ivUp,
                    ivDown,
                    tvLoadingId,
                    tvStopShift,
                    tvStopPending,
                    mData,
                    "stop",
                    v,
                    tvBcm,
                    tvHistory,
                    tvStartShift,
                    tvStatusLoading
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
            tvLoadingStart.setOnClickListener {
                tvStopShift.isEnabled = false
                tvStopShift.visibility = View.GONE
//                tvStopPending.visibility = View.VISIBLE
                tvLoadingStart.isEnabled = false
                tvLoadingStart.visibility = View.GONE
                tvLoadingStop.visibility = View.VISIBLE
                tvLoadingStop.isEnabled = true
                tvLoadingStop.background = ContextCompat.getDrawable(mContext, R.drawable.bg_rounded_red_small)
                tvLoadingStop.setTextColor(ContextCompat.getColor(mContext, R.color.black))
                tvLoadingStop.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_pause_24, 0)
                if(isNetworkAvailable(mContext)) {
                    if (different.toInt() == 0) {
                        mAdapterCallback.tvLoadingStart(
                            bindingAdapterPosition,
                            recyclerDetailFleetList,
                            tvTotalRitase,
                            tvTarget,
                            tvLoadingStop,
                            tvLoadingStart,
                            ivUp,
                            ivDown,
                            tvLoadingId,
                            tvStopShift,
                            tvStopPending,
                            mData,
                            "start",
                            tvBcm,
                            tvHistory,
                            tvStartShift,
                            tvStatusLoading,
                            tvEndShift
                        )
//                        val timeStamp = Helper().dateTimeNow()
//                        val status = "start"
//                        val statusFleet = mData[bindingAdapterPosition].getStatus()
//                        val taskId = mData[bindingAdapterPosition].getTaskId()
//                        val unitId = mData[bindingAdapterPosition].getExaId()
//                        val unitCode = mData[bindingAdapterPosition].getExaCode()
//                        val supirId = mData[bindingAdapterPosition].getOperatorId()
//                        val supir = mData[bindingAdapterPosition].getOperatorName()
//                        val uid = FirebaseAuth.getInstance().currentUser?.uid
//                        timeStampUpdate(
//                            timeStamp,
//                            status,
//                            taskId,
//                            unitId,
//                            unitCode,
//                            supirId,
//                            supir,
//                            uid,
//                            bindingAdapterPosition,
//                            recyclerDetailFleetList,
//                            tvTotalRitase,
//                            tvTarget,
//                            tvLoadingStop,
//                            tvLoadingStart,
//                            ivUp,
//                            ivDown,
//                            tvLoadingId,
//                            statusFleet,
//                            tvStopShift,
//                            tvStopPending
//                        )
                    }
                }
                else{
                    Toast.makeText(mContext, mContext.getString(R.string.connection_status), Toast.LENGTH_LONG).show()
                    tvLoadingStart.isEnabled = true
                    tvLoadingStart.visibility = View.VISIBLE
                    tvLoadingStart.background = ContextCompat.getDrawable(mContext, R.drawable.bg_rounded_fill_grey)
                    tvLoadingStart.setTextColor(ContextCompat.getColor(mContext, R.color.black))
                    tvLoadingStart.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_play_arrow_24, 0)
                    tvLoadingStop.visibility = View.GONE
                    tvLoadingStop.isEnabled = false
                    tvStopShift.isEnabled = true
//                    tvStopShift.visibility = View.VISIBLE
                    tvStopPending.visibility = View.GONE
                }
            }
            tvLoadingStop.setOnClickListener { v ->
//                ivUp.visibility = View.VISIBLE
//                ivDown.visibility = View.GONE
//                recyclerDetailFleetList.visibility = View.GONE
                tvLoadingStart.isEnabled = true
                tvLoadingStart.visibility = View.VISIBLE
                tvLoadingStart.background = ContextCompat.getDrawable(mContext, R.drawable.bg_rounded_fill_grey)
                tvLoadingStart.setTextColor(ContextCompat.getColor(mContext, R.color.black))
                tvLoadingStart.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_play_arrow_24, 0)
                tvLoadingStop.visibility = View.GONE
                tvLoadingStop.isEnabled = false
                tvStopShift.isEnabled = true
//                tvStopShift.visibility = View.VISIBLE
                tvStopPending.visibility = View.GONE
                if(isNetworkAvailable(mContext)) {
                    if (different.toInt() == 0) {
//                        val timeStamp = Helper().dateTimeNow()
//                        val status = "canceled"
//                        val statusFleet = mData[bindingAdapterPosition].getStatus()
//                        val taskId = mData[bindingAdapterPosition].getTaskId()
//                        val unitId = mData[bindingAdapterPosition].getExaId()
//                        val unitCode = mData[bindingAdapterPosition].getExaCode()
//                        val supirId = mData[bindingAdapterPosition].getOperatorId()
//                        val supir = mData[bindingAdapterPosition].getOperatorName()
//                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        mAdapterCallback.tvLoadingStop(
                            bindingAdapterPosition,
                            recyclerDetailFleetList,
                            tvTotalRitase,
                            tvTarget,
                            tvLoadingStop,
                            tvLoadingStart,
                            ivUp,
                            ivDown,
                            tvLoadingId,
                            tvStopShift,
                            tvStopPending,
                            mData,
                            "stop",
                            v,
                            tvBcm,
                            tvHistory,
                            tvStartShift,
                            tvStatusLoading
                        )
//                        timeStampUpdate(
//                            timeStamp,
//                            status,
//                            taskId,
//                            unitId,
//                            unitCode,
//                            supirId,
//                            supir,
//                            uid,
//                            bindingAdapterPosition,
//                            recyclerDetailFleetList,
//                            tvTotalRitase,
//                            tvTarget,
//                            tvLoadingStart,
//                            tvLoadingTime,
//                            ivUp,
//                            ivDown,
//                            tvLoadingId,
//                            statusFleet,
//                            tvStopShift,
//                            tvStopPending
//                        )
                    }
                }
                else{
                    Toast.makeText(mContext, mContext.getString(R.string.connection_status), Toast.LENGTH_LONG).show()
                    tvLoadingStart.isEnabled = true
                    tvLoadingStart.visibility = View.VISIBLE
                    tvLoadingStart.background = ContextCompat.getDrawable(mContext, R.drawable.bg_rounded_fill_grey)
                    tvLoadingStart.setTextColor(ContextCompat.getColor(mContext, R.color.black))
                    tvLoadingStart.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_play_arrow_24, 0)
                    tvLoadingStop.visibility = View.GONE
                    tvLoadingStop.isEnabled = false
                    tvStopShift.isEnabled = false
                    tvStopShift.visibility = View.GONE
//                    tvStopPending.visibility = View.VISIBLE
                }
            }
            tvStartShift.setOnClickListener { v ->
                tvStartShift.isEnabled = false
                mAdapterCallback.tvStartShift(
                    bindingAdapterPosition,
                    mData,
                    recyclerDetailFleetList,
                    ivUp,
                    ivDown,
                    tvLoadingStart,
                    tvStartShift,
                    tvStopPending,
                    "start",
                    v,
                    tvBcm,
                    tvTotalRitase,
                    tvTarget,
                    tvLoadingId,
                    tvLoadingStop,
                    tvStopShift
                )
            }
            tvStopShift.setOnClickListener {
                tvStopShift.isEnabled = false
                mAdapterCallback.tvStopShift(
                    bindingAdapterPosition,
                    mData,
                    recyclerDetailFleetList,
                    ivUp,
                    ivDown,
                    tvLoadingStart,
                    tvStartShift,
                    tvStopPending,
                    tvStopShift
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

    interface FleetPlanAdapterCallback : FleetPlanDetailAdapter.FleetPlanAdapterCallback {
        fun onDetail(
            bindingAdapterPosition: Int,
            mData: ArrayList<TaskGroupList>,
            ivDown: ImageView
        )
        fun onLostTime(bindingAdapterPosition: Int, mData: ArrayList<TaskGroupList>)
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
            recyclerDetailFleetList: RecyclerView,
            tvTotalRitase: TextView,
            tvTarget: TextView,
            tvLoadingStop: TextView,
            tvLoadingStart: TextView,
            ivUp: ImageView,
            ivDown: ImageView,
            tvLoadingId: TextView,
            tvStopShift: TextView,
            tvStopPending: TextView,
            mData: ArrayList<TaskGroupList>,
            s: String,
            v: View,
            tvBcm: TextView,
            tvHistory: TextView,
            tvStartShift: TextView,
            tvStatusLoading: TextView
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

        fun tvStartShift(
            bindingAdapterPosition: Int,
            mData: ArrayList<TaskGroupList>,
            recyclerDetailFleetList: RecyclerView,
            ivUp: ImageView,
            ivDown: ImageView,
            tvLoadingStart: TextView,
            tvStartShift: TextView,
            tvStopPending: TextView,
            s: String,
            v: View,
            tvBcm: TextView,
            tvTotalRitase: TextView,
            tvTarget: TextView,
            tvLoadingId: TextView,
            tvLoadingStop: TextView,
            tvStopShift: TextView
        )

        fun tvLoadingStart(
            bindingAdapterPosition: Int,
            recyclerDetailFleetList: RecyclerView,
            tvTotalRitase: TextView,
            tvTarget: TextView,
            tvLoadingStop: TextView,
            tvLoadingStart: TextView,
            ivUp: ImageView,
            ivDown: ImageView,
            tvLoadingId: TextView,
            tvStopShift: TextView,
            tvStopPending: TextView,
            mData: ArrayList<TaskGroupList>,
            status: String,
            tvBcm: TextView,
            tvHistory: TextView,
            tvStartShift: TextView,
            tvStatusLoading: TextView,
            tvShift: TextView
        )

        fun tvLoadingStop(
            bindingAdapterPosition: Int,
            recyclerDetailFleetList: RecyclerView,
            tvTotalRitase: TextView,
            tvTarget: TextView,
            tvLoadingStop: TextView,
            tvLoadingStart: TextView,
            ivUp: ImageView,
            ivDown: ImageView,
            tvLoadingId: TextView,
            tvStopShift: TextView,
            tvStopPending: TextView,
            mData: ArrayList<TaskGroupList>,
            status: String,
            v: View,
            tvBcm: TextView,
            tvHistory: TextView,
            tvStartShift: TextView,
            tvStatusLoading: TextView
        )

        fun tvStopShift(
            bindingAdapterPosition: Int,
            mData: ArrayList<TaskGroupList>,
            recyclerDetailFleetList: RecyclerView,
            ivUp: ImageView,
            ivDown: ImageView,
            tvLoadingStart: TextView,
            tvStartShift: TextView,
            tvStopPending: TextView,
            tvStopShift: TextView
        )

        fun tvHistory(mData: ArrayList<TaskGroupList>, bindingAdapterPosition: Int)

        override fun llMenuUnit(
            bindingAdapterPosition: Int,
            mData: ArrayList<PlanUnit>,
            v: View,
            tvCircleTime: TextView
        )
    }

    private fun planFleetUnit(
        taskId: String?,
        recyclerDetailFleetList: RecyclerView,
        planUnitList: ArrayList<PlanUnit>?,
        tvTotalRitase: TextView,
        tvTarget: TextView,
        plan: String?,
        exaId: String?,
        tvLoadingStop: TextView,
        tvLoadingStart: TextView,
        ivDown: ImageView,
        ivUp: ImageView,
        loadingId: String?,
        status: String?,
        tvStopPending: TextView,
        tvStopShift: TextView,
        tvBcm: TextView,
        tvHistory: TextView,
        tvStatusLoading: TextView,
        statusShift: String?
    ) {
        val refTask = FirebaseDatabase.getInstance().reference
        refTask.child("TasksFleetPlanUnit")
            .orderByChild("taskId")
            .equalTo(taskId)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SuspiciousIndentation")
                override fun onDataChange(snapshot: DataSnapshot) {
                    planUnitList?.clear()
                    if(snapshot.exists()) {
                        if (tvStatusLoading.text.toString() == "status" || tvStatusLoading.text.toString() == "remark") {
                            tvLoadingStart.visibility = View.VISIBLE
                            tvLoadingStart.isEnabled = true
                        }
                        var count = 0
                        for (postSnapshot in snapshot.children) {
                            val planUnitDataList = postSnapshot.getValue(PlanUnit::class.java)
                            planUnitList?.add(planUnitDataList!!)

                            val refRitase = FirebaseDatabase.getInstance().reference
                            refRitase.child("Ritase")
                                .child(taskId.toString())
                                .child(planUnitDataList?.getTaskUnitId().toString())
                                .orderByChild("supirId")
                                .equalTo(planUnitDataList?.getSopirId())
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot2: DataSnapshot) {
                                        if(snapshot2.exists()) {
                                            for (postSnapshot2 in snapshot2.children) {
                                                val ritaseDataList = postSnapshot2.getValue(Ritase::class.java)
                                                if (ritaseDataList?.getStatus() != "canceled" && ritaseDataList?.getTaskId() == taskId) {
                                                    count += 1
                                                    tvTotalRitase.text = count.toString()
                                                    tvTarget.text = HtmlCompat.fromHtml(
                                                        "<i>A/T: </i>$count/$plan",
                                                        HtmlCompat.FROM_HTML_MODE_LEGACY
                                                    )
                                                    tvBcm.text = HtmlCompat.fromHtml("<i>"+FormatNumber().simpleNumber((count*22))+"</i>", HtmlCompat.FROM_HTML_MODE_LEGACY)
                                                }
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }

                                })
                        }
                        planUnitList?.sortByDescending { it.getSopir() }

                        val refLoadingTime = FirebaseDatabase.getInstance().reference
                        refLoadingTime.child("LoadingTime")
                            .child(taskId!!)
                            .child(exaId!!)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot2: DataSnapshot) {
                                    var s = ""
                                    if (snapshot2.exists()) {
                                        ivDown.visibility = View.GONE
                                        ivUp.visibility = View.VISIBLE
                                        for (postSnapshot2 in snapshot2.children) {
                                            val loadingTime = postSnapshot2.getValue(LoadingTime::class.java)
                                            tvStatusLoading.text = Helper().capitalizeWords(loadingTime?.getStatus())
                                            tvStatusLoading.setTextColor(mContext.getColor(R.color.red))
                                            if (loadingTime?.getStatus().equals("start") && status != "done") {
                                                tvLoadingStop.visibility = View.VISIBLE
                                                tvLoadingStop.isEnabled = true
                                                tvLoadingStart.visibility = View.GONE
                                                tvLoadingStart.isEnabled = false
                                                tvStopShift.isEnabled = false
                                                tvStopShift.visibility = View.GONE
//                                                tvStopPending.visibility = View.VISIBLE
                                                s = loadingTime?.getStatus().toString()
                                            }
                                            if (loadingTime?.getStatus().equals("stop") && status != "done") {
                                                tvLoadingStop.visibility = View.GONE
                                                tvLoadingStop.isEnabled = false
                                                tvLoadingStart.visibility = View.VISIBLE
                                                tvLoadingStart.isEnabled = true
                                                tvLoadingStart.background = ContextCompat.getDrawable(mContext, R.drawable.bg_rounded_fill_grey)
                                                tvLoadingStart.setTextColor(ContextCompat.getColor(mContext, R.color.black))
                                                tvLoadingStart.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_play_arrow_24, 0)
                                                tvStopShift.isEnabled = true
//                                                tvStopShift.visibility = View.VISIBLE
                                                tvStopPending.visibility = View.GONE
                                                s = loadingTime?.getStatus().toString()
                                            }

                                        }
                                        recyclerDetailFleetList.visibility = View.VISIBLE
                                        planUnitList?.sortByDescending { it.getSopir() }
                                        val planUnitAdapter = FleetPlanDetailAdapter(
                                            mContext,
                                            planUnitList!!,
                                            tvTotalRitase,
                                            mAdapterCallback,
                                            tvTarget,
                                            plan,
                                            s,
                                            tvLoadingStart,
                                            tvLoadingStop,
                                            taskId,
                                            exaId,
                                            loadingId,
                                            tvStopPending,
                                            tvStopShift,
                                            tvBcm
                                        )
                                        recyclerDetailFleetList.layoutManager = LinearLayoutManager(mContext)
                                        recyclerDetailFleetList.adapter = planUnitAdapter
                                    }
                                    else{
                                        tvLoadingStart.visibility = View.VISIBLE
                                        tvLoadingStart.isEnabled = true
                                        recyclerDetailFleetList.visibility = View.GONE
                                        planUnitList?.sortByDescending { it.getSopir() }
                                        val planUnitAdapter = FleetPlanDetailAdapter(
                                            mContext,
                                            planUnitList!!,
                                            tvTotalRitase,
                                            mAdapterCallback,
                                            tvTarget,
                                            plan,
                                            s,
                                            tvLoadingStart,
                                            tvLoadingStop,
                                            taskId,
                                            exaId,
                                            loadingId,
                                            tvStopPending,
                                            tvStopShift,
                                            tvBcm
                                        )
                                        recyclerDetailFleetList.layoutManager = LinearLayoutManager(mContext)
                                        recyclerDetailFleetList.adapter = planUnitAdapter
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    tvLoadingStart.visibility = View.VISIBLE
                                    tvLoadingStart.isEnabled = true
                                }

                            })

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
                        val refLoadingTime = FirebaseDatabase.getInstance().reference
                        refLoadingTime.child("LoadingTime")
                            .child(taskId!!)
                            .child(exaId!!)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot2: DataSnapshot) {
                                    var s = ""
                                    if (snapshot2.exists()) {
                                        ivDown.visibility = View.GONE
                                        ivUp.visibility = View.VISIBLE
                                        for (postSnapshot2 in snapshot2.children) {
                                            val loadingTime = postSnapshot2.getValue(LoadingTime::class.java)
                                            tvStatusLoading.text = Helper().capitalizeWords(loadingTime?.getStatus())
//                                            tvStatusLoading.setTextColor(mContext.getColor(R.color.red))
                                            if (loadingTime?.getStatus().equals("start") && status != "done") {
                                                tvLoadingStop.visibility = View.VISIBLE
                                                tvLoadingStop.isEnabled = true
                                                tvLoadingStart.visibility = View.GONE
                                                tvLoadingStart.isEnabled = false
                                                tvStopShift.isEnabled = false
                                                tvStopShift.visibility = View.GONE
//                                                tvStopPending.visibility = View.VISIBLE
                                                s = loadingTime?.getStatus().toString()
                                            }
                                            if (loadingTime?.getStatus().equals("stop") && status != "done") {
                                                tvLoadingStop.visibility = View.GONE
                                                tvLoadingStop.isEnabled = false
                                                tvLoadingStart.visibility = View.VISIBLE
                                                tvLoadingStart.isEnabled = true
                                                tvLoadingStart.background = ContextCompat.getDrawable(mContext, R.drawable.bg_rounded_fill_grey)
                                                tvLoadingStart.setTextColor(ContextCompat.getColor(mContext, R.color.black))
                                                tvLoadingStart.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_play_arrow_24, 0)
                                                tvStopShift.isEnabled = true
//                                                tvStopShift.visibility = View.VISIBLE
                                                tvStopPending.visibility = View.GONE
                                                s = loadingTime?.getStatus().toString()
                                            }
                                        }
                                        recyclerDetailFleetList.visibility = View.VISIBLE
                                        planUnitList?.sortByDescending { it.getSopir() }
                                        val planUnitAdapter = FleetPlanDetailAdapter(
                                            mContext,
                                            planUnitList!!,
                                            tvTotalRitase,
                                            mAdapterCallback,
                                            tvTarget,
                                            plan,
                                            s,
                                            tvLoadingStart,
                                            tvLoadingStop,
                                            taskId,
                                            exaId,
                                            loadingId,
                                            tvStopPending,
                                            tvStopShift,
                                            tvBcm
                                        )
                                        recyclerDetailFleetList.layoutManager = LinearLayoutManager(mContext)
                                        recyclerDetailFleetList.adapter = planUnitAdapter
                                    }
                                    else{
                                        recyclerDetailFleetList.visibility = View.GONE
                                        planUnitList?.sortByDescending { it.getSopir() }
                                        val planUnitAdapter = FleetPlanDetailAdapter(
                                            mContext,
                                            planUnitList!!,
                                            tvTotalRitase,
                                            mAdapterCallback,
                                            tvTarget,
                                            plan,
                                            s,
                                            tvLoadingStart,
                                            tvLoadingStop,
                                            taskId,
                                            exaId,
                                            loadingId,
                                            tvStopPending,
                                            tvStopShift,
                                            tvBcm
                                        )
                                        recyclerDetailFleetList.layoutManager = LinearLayoutManager(mContext)
                                        recyclerDetailFleetList.adapter = planUnitAdapter
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })

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

                        if (tvStatusLoading.text.toString().isEmpty() && statusShift != "done") {
                            tvLoadingStart.visibility = View.VISIBLE
                            tvLoadingStart.isEnabled = true
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    recyclerDetailFleetList.visibility = View.GONE
                }

            })
    }

    private fun timeStampUpdate(
        timeStamp: String,
        status: String,
        taskId: String?,
        unitId: String?,
        unitCode: String?,
        supirId: String?,
        supir: String?,
        uid: String?,
        bindingAdapterPosition: Int,
        recyclerDetailFleetList: RecyclerView,
        tvTotalRitase: TextView,
        tvTarget: TextView,
        tvLoadingStop: TextView,
        tvLoadingStart: TextView,
        ivUp: ImageView,
        ivDown: ImageView,
        tvLoadingId: TextView,
        statusFleet: String?,
        tvStopShift: TextView,
        tvStopPending: TextView,
        tvBcm: TextView,
        tvHistory: TextView,
        tvStatusLoading: TextView
    ) {
        val refLoading = FirebaseDatabase.getInstance().reference
        var loadingId = refLoading.push().key
        if (tvLoadingId.text.isNotEmpty()) {
            loadingId = tvLoadingId.text.toString()
        }

        val ritaseHashMap = HashMap<String, Any>()
        ritaseHashMap["loadingId"] = loadingId.toString()
        ritaseHashMap["timeStamp"] = timeStamp
        ritaseHashMap["status"] = status
        ritaseHashMap["taskId"] = taskId.toString()
        ritaseHashMap["unitId"] = unitId.toString()
        ritaseHashMap["unitCode"] = unitCode.toString()
        ritaseHashMap["operatorId"] = supirId.toString()
        ritaseHashMap["operator"] = supir.toString()
        ritaseHashMap["uid"] = uid.toString()

        if(isNetworkAvailable(mContext)) {
            databaseReference = refLoading
                .child("LoadingTime")
                .child(taskId.toString())
                .child(unitId.toString())
                .child(loadingId.toString())
            databaseReference.setValue(ritaseHashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        tvLoadingId.text = loadingId.toString()
                        val refShareContact = FirebaseDatabase.getInstance().reference
                        refShareContact.child("LoadingTime")
                            .child(mData[bindingAdapterPosition].getTaskId().toString())
                            .child(mData[bindingAdapterPosition].getExaId().toString())
                            .child(loadingId.toString())
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot2: DataSnapshot) {
                                    if (snapshot2.exists()) {
                                        val loadingData = snapshot2.getValue(LoadingTime::class.java)
                                        planFleetUnit(
                                            mData[bindingAdapterPosition].getTaskId(),
                                            recyclerDetailFleetList,
                                            planUnitList,
                                            tvTotalRitase,
                                            tvTarget,
                                            mData[bindingAdapterPosition].getPlan(),
                                            mData[bindingAdapterPosition].getExaId(),
                                            tvLoadingStop,
                                            tvLoadingStart,
                                            ivDown,
                                            ivUp,
                                            loadingData?.getLoadingId(),
                                            statusFleet,
                                            tvStopPending,
                                            tvStopShift,
                                            tvBcm,
                                            tvHistory,
                                            tvStatusLoading,
                                            mData[bindingAdapterPosition].getStatus()
                                        )
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })
                    }
                    else {
                        val toast = Toast.makeText(
                            mContext,
                            "Proccessing unsuccessfully",
                            Toast.LENGTH_LONG
                        )
                        toast.setGravity(Gravity.CENTER, 0, 0)
                        toast.show()
                    }
                }
        }
        else{
            Toast.makeText(mContext, mContext.getString(R.string.connection_status), Toast.LENGTH_LONG).show()
        }
    }

    private fun printDifferenceDateForHours(
        timeStamp: String,
        tvEndShift: TextView,
        mData: ArrayList<TaskGroupList>,
        position: Int
    ) {
        val currentTime = Calendar.getInstance().time
        val format1 = SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault())
        val endDate = format1.parse(timeStamp)
        val dateShift = mData[position].getDate()
        val endTime = mData[position].getShiftTime()?.split("-")?.get(1)
        val endShift = format1.parse("$dateShift $endTime:00")
        var secondEnd:Long? = null
        if (mData[position].getShift() == "Shift Siang") {
            secondEnd = endShift?.time?.minus(endDate!!.time)
        }
        if (mData[position].getShift() == "Shift Malam") {
            val tes = endShift?.time?.plus(3600000.times(24))
//            Toast.makeText(mContext, tes.toString()+" - "+endDate!!.time, Toast.LENGTH_LONG).show()
//            tvEndShift.visibility = View.VISIBLE
//            tvEndShift.text = tes.toString()+" - "+endDate!!.time
//            tvEndShift.text = DateHelper().tes(tes!!.toLong())
            secondEnd = tes?.minus(endDate!!.time)
        }
        //milliseconds
        different2 = (endDate?.time?.plus(secondEnd!!))?.minus(currentTime.time)!!
        countDownTimer = object : CountDownTimer(different2, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                var diff = millisUntilFinished
                val secondsInMilli: Long = 1000
                val minutesInMilli = secondsInMilli * 60
                val hoursInMilli = minutesInMilli * 60
                val daysInMilli = hoursInMilli * 24

                val elapsedDays = diff / daysInMilli
                diff %= daysInMilli

                val elapsedHours = diff / hoursInMilli
                diff %= hoursInMilli

                val elapsedMinutes = diff / minutesInMilli
                diff %= minutesInMilli

                val elapsedSeconds = diff / secondsInMilli
                tvEndShift.visibility = View.VISIBLE
                tvEndShift.text = "$elapsedHours:$elapsedMinutes:$elapsedSeconds"
            }

            override fun onFinish() {
                tvEndShift.visibility = View.GONE
            }
        }.start()
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