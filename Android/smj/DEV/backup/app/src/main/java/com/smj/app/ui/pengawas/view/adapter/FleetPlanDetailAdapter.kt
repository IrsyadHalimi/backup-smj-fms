package com.smj.app.ui.pengawas.view.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.TimePickerDialog
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smj.app.R
import com.smj.app.helper.FormatNumber
import com.smj.app.helper.Helper
import com.smj.app.ui.pengawas.model.LoadingTime
import com.smj.app.ui.pengawas.model.Ritase
import com.smj.app.ui.task.model.PlanUnit
import java.text.SimpleDateFormat
import java.util.Calendar

class FleetPlanDetailAdapter(
    mContext: Context,
    private var mData: ArrayList<PlanUnit>,
    tvTotalRitase: TextView,
    adapterCallback: FleetPlanAdapter.FleetPlanAdapterCallback,
    tvTarget: TextView,
    plan: String?,
    s: String,
    tvLoadingStop: TextView,
    tvLoadingStart: TextView,
    taskId: String,
    exaId: String,
    loadingId: String?,
    tvStopPending: TextView,
    tvStopShift: TextView,
    tvBcm: TextView
) : RecyclerView.Adapter<FleetPlanDetailAdapter.MyViewHolder>()  {

    private var mContext: Context
    private lateinit var databaseReference: DatabaseReference
    private val mAdapterCallback: FleetPlanAdapterCallback
    var ritase: ArrayList<Ritase>? = null
    private lateinit var dialogView: View
    private var tvTotalRitase: TextView
    private var tvTarget: TextView
    private var plan: String
    var count = 0
    private lateinit var countDownTimer:CountDownTimer
    var different: Long = 0
    var s: String
    var tvLoadingStop: TextView
    var tvLoadingStart: TextView
    var exaId: String
    var loadingId: String
    var tvStopShift: TextView
    var tvStopPending: TextView
    var tvBcm: TextView

    private var planUnitList: ArrayList<PlanUnit>? = null

    init {
        this.mContext = mContext
        this.tvTotalRitase = tvTotalRitase
        this.tvTarget = tvTarget
        this.plan = plan!!
        this.mAdapterCallback = adapterCallback
        this.different
        this.s = s
        this.tvLoadingStart = tvLoadingStart
        this.tvLoadingStop = tvLoadingStop
        this.exaId = exaId
        this.loadingId = loadingId!!
        this.tvStopShift = tvStopShift
        this.tvStopPending = tvStopPending
        this.tvBcm = tvBcm
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val v: View =
            LayoutInflater.from(mContext).inflate(R.layout.layout_fleet_unit, parent, false)
        return MyViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = mData[position]
        holder.tvUnit.text = HtmlCompat.fromHtml("<strong>"+data.getUnitCode()+"</strong><br/><i><small> Hauler</small></i>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        holder.tvSopir.text = HtmlCompat.fromHtml("<strong>"+data.getSopir()+"</strong><br/><i><small> Sopir</small></i>", HtmlCompat.FROM_HTML_MODE_LEGACY)

        if ((s == "start" || s == "remark") && mData[position].getStatus() != "delete"){
            holder.tvCircleTime.visibility = View.VISIBLE
            holder.tvLocked.visibility = View.GONE
        }
        else {
//            tvLoadingStart.background = ContextCompat.getDrawable(mContext, R.drawable.bg_rounded_fill_grey)
//            tvLoadingStart.setTextColor(ContextCompat.getColor(mContext, R.color.grey))
//            tvLoadingStart.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_pause_grey, 0)
            holder.tvCircleTime.visibility = View.GONE
            holder.tvLocked.visibility = View.VISIBLE
            holder.tvLocked.text = "Waiting..."
        }
        if (mData[position].getStatus() == "done") {
            holder.llMenuUnit.visibility = View.GONE
            holder.tvLocked.text = "Stopped"
        }
        if (mData[position].getStatus() == "delete") {
            holder.tvLocked.text = "Stopped"
            holder.llRecyclerDetailRitaseList.visibility = View.GONE
            holder.llMenuUnit.visibility = View.GONE
            holder.headerRitase.visibility = View.GONE
            holder.tvHide.visibility = View.GONE
            holder.tvShow.visibility = View.VISIBLE
        }

        if (mData[position].getStatus() == "unitExchange" || mData[position].getStatus() == "operatorExchange") {
            if (mData[position].getStatus() == "unitExchange") {
                holder.tvUnitOver.text = "Unit replaced"
            }
            if (mData[position].getStatus() == "operatorExchange") {
                holder.tvUnitOver.text = "Operator replaced"
            }
            holder.tvUnitOver.visibility = View.VISIBLE
            holder.llRecyclerDetailRitaseList.visibility = View.GONE
            holder.tvCircleTime.visibility = View.GONE
            holder.tvLocked.visibility = View.GONE
            holder.llMenuUnit.visibility = View.GONE
            holder.headerRitase.visibility = View.GONE
            holder.tvHide.visibility = View.GONE
            holder.tvShow.visibility = View.VISIBLE
        }

        ritase = ArrayList()
        ritaseList(
            mData,
            position,
            holder.recyclerDetailRitaseList,
            ritase!!,
            holder.headerRitase,
            holder.tvCount,
            holder.tvShow,
            holder.tvHide,
            holder.headerRitase,
            holder.llRecyclerDetailRitaseList,
            holder.tvCtRite,
            holder.tvCircleTime,
            holder.tvUndo,
            holder.tvLocked,
        )
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvUnit: TextView = itemView.findViewById(R.id.tv_unit)
        var tvSopir: TextView = itemView.findViewById(R.id.tv_sopir)
        var tvStart: TextView = itemView.findViewById(R.id.tv_start)
        var tvCircleTime: TextView = itemView.findViewById(R.id.tv_circle_time)
        var tvStop: TextView = itemView.findViewById(R.id.tv_stop)
        var tvNote: TextView = itemView.findViewById(R.id.tv_note)
        var headerRitase: LinearLayout = itemView.findViewById(R.id.ll_header_ritase)
        var recyclerDetailRitaseList: RecyclerView = itemView.findViewById(R.id.recycler_detail_ritase_list)
        var llRecyclerDetailRitaseList: LinearLayout = itemView.findViewById(R.id.ll_ritase_list)
        var tvCount: TextView = itemView.findViewById(R.id.tv_count)
        var tvCtRite: TextView = itemView.findViewById(R.id.tv_ct_rite)
        var tvHide: TextView = itemView.findViewById(R.id.tv_hide)
        var tvShow: TextView = itemView.findViewById(R.id.tv_show)
        var llMenuUnit: LinearLayout = itemView.findViewById(R.id.ll_menu_unit)
        var tvUndo: TextView = itemView.findViewById(R.id.tv_undo)
        var tvLocked: TextView = itemView.findViewById(R.id.tv_locked)
        var llSosial2: LinearLayout = itemView.findViewById(R.id.ll_sosial_2)
        var tvUnitOver: TextView = itemView.findViewById(R.id.tv_unit_over)

        init {
            tvCircleTime.setOnClickListener {
                tvCircleTime.isEnabled = false
                tvCircleTime.visibility = View.GONE
//                tvLoadingStart.isEnabled = false
//                tvLoadingStart.background = ContextCompat.getDrawable(mContext, R.drawable.bg_rounded_fill_grey)
//                tvLoadingStart.setTextColor(ContextCompat.getColor(mContext, R.color.grey))
//                tvLoadingStart.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_pause_grey, 0)
                if(isNetworkAvailable(mContext)) {
                    if (different.toInt() == 0) {
                        if (s == "start") {
                            val timeStamp = Helper().dateTimeNow()
                            val status = s
                            val taskId = mData[bindingAdapterPosition].getTaskId()
                            val unitId = mData[bindingAdapterPosition].getTaskUnitId()
                            val unitCode = mData[bindingAdapterPosition].getUnitCode()
                            val supirId = mData[bindingAdapterPosition].getSopirId()
                            val supir = mData[bindingAdapterPosition].getSopir()
                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                            if (mData[bindingAdapterPosition].getStatus() == "todo" || mData[bindingAdapterPosition].getStatus() == "CrossLoading") {
                                timeStampUpdate(
                                    timeStamp,
                                    status,
                                    taskId,
                                    unitId,
                                    unitCode,
                                    supirId,
                                    supir,
                                    uid,
                                    tvCount,
                                    bindingAdapterPosition,
                                    recyclerDetailRitaseList,
                                    headerRitase,
                                    tvHide,
                                    tvShow,
                                    llRecyclerDetailRitaseList,
                                    tvCtRite,
                                    tvCircleTime,
                                    tvUndo,
                                    tvLocked,
                                    tvLoadingStop,
                                    tvLoadingStart,
                                    exaId,
                                    loadingId,
                                    tvStopPending,
                                    tvStopShift,
                                    tvBcm
                                )
                            }
                            else {
                                tvCircleTime.isEnabled = false
                                val alertBuilder = AlertDialog.Builder(mContext)
                                alertBuilder.setTitle("Konfirmasi")
                                alertBuilder.setMessage(HtmlCompat.fromHtml("Unit sudah tidak aktif!", HtmlCompat.FROM_HTML_MODE_LEGACY))
                                alertBuilder.setCancelable(false)
                                alertBuilder.setPositiveButton("OK") { _, _ ->
                                    tvCircleTime.visibility = View.GONE
                                    tvCircleTime.isEnabled = false
                                }
                                alertBuilder.show()
                            }
                        }
                        else {
                            tvCircleTime.isEnabled = false
                            val alertBuilder = AlertDialog.Builder(mContext)
                            alertBuilder.setTitle("Konfirmasi")
                            alertBuilder.setMessage(HtmlCompat.fromHtml("Lakukan pencatatan <strong>Start Loading</strong> terlebih dahulu!", HtmlCompat.FROM_HTML_MODE_LEGACY))
                            alertBuilder.setCancelable(false)
                            alertBuilder.setPositiveButton("OK") { _, _ ->
                                tvCircleTime.visibility = View.VISIBLE
                                tvCircleTime.isEnabled = true
                            }
                            alertBuilder.show()
                        }
                    }
                    else {
                        tvCircleTime.isEnabled = false
                        val alertBuilder = AlertDialog.Builder(mContext)
                        alertBuilder.setTitle("Dalam Antrian")
                        alertBuilder.setMessage("Tunggu 1 menit atau batalkan proses yang sedang berjalan!")
                        alertBuilder.setCancelable(false)
                        alertBuilder.setPositiveButton("OK") { _, _ ->
                            tvCircleTime.visibility = View.VISIBLE
                            tvCircleTime.isEnabled = true
                        }
                        alertBuilder.show()
                    }
                }
                else{
                    Toast.makeText(mContext, mContext.getString(R.string.connection_status), Toast.LENGTH_LONG).show()
                    tvCircleTime.isEnabled = true
                    tvCircleTime.visibility = View.VISIBLE
//                    tvLoadingStart.isEnabled = true
//                    tvLoadingStart.background = ContextCompat.getDrawable(mContext, R.drawable.bg_rounded_red_small)
//                    tvLoadingStart.setTextColor(ContextCompat.getColor(mContext, R.color.black))
//                    tvLoadingStart.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_pause_24, 0)
                }
            }
            tvUndo.setOnClickListener {
                tvUndo.isEnabled = false
                tvUndo.visibility = View.GONE
                tvLocked.visibility = View.GONE
//                tvLoadingStart.isEnabled = true
//                tvLoadingStart.background = ContextCompat.getDrawable(mContext, R.drawable.bg_rounded_red_small)
//                tvLoadingStart.setTextColor(ContextCompat.getColor(mContext, R.color.black))
//                tvLoadingStart.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_pause_24, 0)
                if(isNetworkAvailable(mContext)) {
                    val refShareContact = FirebaseDatabase.getInstance().reference
                    refShareContact.child("Ritase")
                        .child(mData[bindingAdapterPosition].getTaskId().toString())
                        .child(mData[bindingAdapterPosition].getTaskUnitId().toString())
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    var ritaseDataList: Ritase? = null
                                    for (postSnapshot in snapshot.children) {
                                        ritaseDataList = postSnapshot.getValue(Ritase::class.java)
                                    }
                                    setUndo(
                                        ritaseDataList!!,
                                        mData[bindingAdapterPosition].getTaskId(),
                                        tvCircleTime,
                                        tvUndo,
                                        tvLocked,
                                        ritaseDataList.getRitaseId().toString(),
                                        ritaseDataList.getTimeStamp(),
                                        bindingAdapterPosition,
                                        recyclerDetailRitaseList,
                                        headerRitase,
                                        tvCount,
                                        tvCtRite
                                    )
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                }
                else{
                    Toast.makeText(mContext, mContext.getString(R.string.connection_status), Toast.LENGTH_LONG).show()
                    tvUndo.isEnabled = true
                    tvUndo.visibility = View.VISIBLE
                    tvLocked.visibility = View.GONE
//                    tvLoadingStart.isEnabled = false
//                    tvLoadingStart.background = ContextCompat.getDrawable(mContext, R.drawable.bg_rounded_fill_grey)
//                    tvLoadingStart.setTextColor(ContextCompat.getColor(mContext, R.color.grey))
//                    tvLoadingStart.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_pause_grey, 0)
                }
            }
            tvStart.setOnClickListener {
                if(isNetworkAvailable(mContext)) {
                    val alertBuilder = AlertDialog.Builder(mContext)
                    alertBuilder.setTitle("Start")
                    alertBuilder.setMessage("Start timestamp ritase ?")
                    alertBuilder.setCancelable(false)
                    alertBuilder.setPositiveButton("Start") { _, _ ->
                        val timeStamp = Helper().dateTimeNow()
                        val status = "start"
                        val taskId = mData[bindingAdapterPosition].getTaskId()
                        val unitId = mData[bindingAdapterPosition].getTaskUnitId()
                        val unitCode = mData[bindingAdapterPosition].getUnitCode()
                        val supirId = mData[bindingAdapterPosition].getSopirId()
                        val supir = mData[bindingAdapterPosition].getSopir()
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        tvStart.visibility = View.GONE
                        tvStop.visibility = View.VISIBLE
                        timeStampUpdate(
                            timeStamp,
                            status,
                            taskId,
                            unitId,
                            unitCode,
                            supirId,
                            supir,
                            uid,
                            tvCount,
                            bindingAdapterPosition,
                            recyclerDetailRitaseList,
                            headerRitase,
                            tvHide,
                            tvShow,
                            llRecyclerDetailRitaseList,
                            tvCtRite,
                            tvCircleTime,
                            tvUndo,
                            tvLocked,
                            tvLoadingStop,
                            tvLoadingStart,
                            exaId,
                            loadingId,
                            tvStopPending,
                            tvStopShift,
 tvBcm
                        )
                    }
                    alertBuilder.setNeutralButton("Cancel") { _, _ ->
                    }
                    alertBuilder.show()
                }
                else{
                    Toast.makeText(mContext, mContext.getString(R.string.connection_status), Toast.LENGTH_LONG).show()
                }
            }
            tvStop.setOnClickListener {
                val alertBuilder = AlertDialog.Builder(mContext)
                alertBuilder.setTitle("Stop")
                alertBuilder.setMessage("Stop timestamp ritase ?")
                alertBuilder.setCancelable(false)
                alertBuilder.setPositiveButton("Stop"){_,_ ->
                    val timeStamp = Helper().dateTimeNow()
                    val status = "end"
                    val taskId = mData[bindingAdapterPosition].getTaskId()
                    val unitId = mData[bindingAdapterPosition].getTaskUnitId()
                    val unitCode = mData[bindingAdapterPosition].getUnitCode()
                    val supirId = mData[bindingAdapterPosition].getSopirId()
                    val supir = mData[bindingAdapterPosition].getSopir()
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    tvStart.visibility = View.VISIBLE
                    tvStop.visibility = View.GONE
                    timeStampUpdate(
                        timeStamp,
                        status,
                        taskId,
                        unitId,
                        unitCode,
                        supirId,
                        supir,
                        uid,
                        tvCount,
                        bindingAdapterPosition,
                        recyclerDetailRitaseList,
                        headerRitase,
                        tvHide,
                        tvShow,
                        llRecyclerDetailRitaseList,
                        tvCtRite,
                        tvCircleTime,
                        tvUndo,
                        tvLocked,
                        tvLoadingStop,
                        tvLoadingStart,
                        exaId,
                        loadingId,
                        tvStopPending,
                        tvStopShift,
 tvBcm
                    )
                }
                alertBuilder.setNeutralButton("Cancel"){_,_ ->
                }
                alertBuilder.show()
            }
            tvNote.setOnClickListener {
                showNoteDialog()
            }
            llMenuUnit.setOnClickListener { v ->
                mAdapterCallback.llMenuUnit(
                    bindingAdapterPosition,
                    mData,
                    v,
                    tvCircleTime
                )
            }
            tvUnit.setOnClickListener {
                mAdapterCallback.tvUnit(tvSopir, tvUnit, llSosial2)
            }
        }
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
        tvCount: TextView,
        position: Int,
        recyclerDetailRitasList: RecyclerView,
        headerRitase: LinearLayout,
        tvHide: TextView,
        tvShow: TextView,
        llRecyclerDetailRitaseList: LinearLayout,
        tvCtRite: TextView,
        tvCircleTime: TextView,
        tvUndo: TextView,
        tvLocked: TextView,
        tvLoadingStop: TextView,
        tvLoadingStart: TextView,
        exaId: String,
        loadingId: String,
        tvStopPending: TextView,
        tvStopShift: TextView,
        tvBcm: TextView
    ) {
        val refRitase = FirebaseDatabase.getInstance().reference
        val ritaseId = refRitase.push().key

        val ritaseHashMap = HashMap<String, Any>()
        ritaseHashMap["ritaseId"] = ritaseId.toString()
        ritaseHashMap["timeStamp"] = timeStamp
        ritaseHashMap["status"] = status
        ritaseHashMap["taskId"] = taskId.toString()
        ritaseHashMap["unitId"] = unitId.toString()
        ritaseHashMap["unitCode"] = unitCode.toString()
        ritaseHashMap["supirId"] = supirId.toString()
        ritaseHashMap["supir"] = supir.toString()
        ritaseHashMap["uid"] = uid.toString()

        if(isNetworkAvailable(mContext)) {
            databaseReference = refRitase
                .child("Ritase")
                .child(taskId.toString())
                .child(unitId.toString())
                .child(ritaseId.toString())
            databaseReference.updateChildren(ritaseHashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        tvCircleTime.isEnabled = false
                        tvCircleTime.visibility = View.GONE
                        tvUndo.visibility = View.VISIBLE
                        llRecyclerDetailRitaseList.visibility = View.VISIBLE
                        recyclerDetailRitasList.visibility = View.VISIBLE
                        tvHide.visibility = View.VISIBLE
                        tvShow.visibility = View.GONE

                        val refShareContact = FirebaseDatabase.getInstance().reference
                        refShareContact.child("Ritase")
                            .child(mData[position].getTaskId().toString())
                            .child(mData[position].getTaskUnitId().toString())
                            .orderByChild("supirId")
                            .equalTo(supirId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot2: DataSnapshot) {
                                    ritase?.clear()
                                    if (snapshot2.exists()) {
                                        headerRitase.visibility = View.VISIBLE
                                        count += 1
                                        tvTotalRitase.text = count.toString()
                                        tvTarget.text = HtmlCompat.fromHtml(
                                            "<i>A/T: </i>$count/$plan",
                                            HtmlCompat.FROM_HTML_MODE_LEGACY
                                        )
                                        tvBcm.text = HtmlCompat.fromHtml("<i>"+FormatNumber().simpleNumber((count*22))+"</i>", HtmlCompat.FROM_HTML_MODE_LEGACY)
                                        this@FleetPlanDetailAdapter.tvBcm.text = FormatNumber().simpleNumber((count*22))
                                        var c = 0
                                        for (postSnapshot in snapshot2.children) {
                                            val ritaseDataList =
                                                postSnapshot.getValue(Ritase::class.java)
                                            if (ritaseDataList?.getStatus() != "canceled" && ritaseDataList?.getSupirId() == supirId && ritaseDataList?.getUnitId() == unitId) {
                                                c += 1
                                                tvCount.text = c.toString()
                                                ritase?.add(ritaseDataList!!)
                                            }

                                            if (ritaseDataList?.getStatus().equals("start")) {
                                                tvCircleTime.visibility = View.GONE
                                                printDifferenceDateForHours(
                                                    tvUndo,
                                                    timeStamp,
                                                    tvCircleTime,
                                                    tvLocked,
                                                    status,
                                                    ritaseDataList,
                                                    taskId,
                                                    tvCount,
                                                    position,
                                                    recyclerDetailRitasList,
                                                    headerRitase,
                                                    tvHide,
                                                    tvShow,
                                                    llRecyclerDetailRitaseList,
                                                    tvCtRite,
                                                    ritaseId,
                                                    tvLoadingStop,
                                                    tvLoadingStart,
                                                    exaId,
                                                    loadingId,
                                                    tvStopPending,
                                                    tvStopShift
                                                )
                                            } else {
                                                tvCircleTime.visibility = View.VISIBLE
                                            }
                                        }
                                        val planUnitAdapter = RitaseAdapter(
                                            mContext,
                                            ritase!!,
                                            tvCtRite,
                                            mData[position].getTaskId().toString(),
                                            mAdapterCallback,
                                            mData[position].getStatus()
                                        )
                                        ritase!!.sortByDescending { it.getUnitId() }
                                        recyclerDetailRitasList.layoutManager = GridLayoutManager(
                                            mContext,
                                            1,
                                            GridLayoutManager.VERTICAL,
                                            false
                                        )
                                        recyclerDetailRitasList.adapter = planUnitAdapter
                                    } else {
                                        headerRitase.visibility = View.GONE
                                        recyclerDetailRitasList.visibility = View.GONE
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    recyclerDetailRitasList.visibility = View.GONE
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

    private fun ritaseList(
        mData: ArrayList<PlanUnit>,
        position: Int,
        recyclerDetailRitaseList: RecyclerView,
        ritase: ArrayList<Ritase>,
        headerRitase: LinearLayout,
        tvCount: TextView,
        tvShow: TextView,
        tvHide: TextView,
        headerRitase1: LinearLayout,
        llRecyclerDetailRitaseList: LinearLayout,
        tvCtRite: TextView,
        tvCircleTime: TextView,
        tvUndo: TextView,
        tvLocked: TextView
    ) {
        val refShareContact = FirebaseDatabase.getInstance().reference

        refShareContact.child("Ritase")
            .child(mData[position].getTaskId().toString())
            .child(mData[position].getTaskUnitId().toString())
            .orderByChild("supirId")
            .equalTo(mData[position].getSopirId())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SimpleDateFormat")
                override fun onDataChange(snapshot: DataSnapshot) {
                    ritase.clear()
                    tvHide.setOnClickListener {
                        llRecyclerDetailRitaseList.visibility = View.GONE
                        headerRitase1.visibility = View.GONE
                        tvHide.visibility = View.GONE
                        tvShow.visibility = View.VISIBLE
                    }
                    tvShow.setOnClickListener {
                        llRecyclerDetailRitaseList.visibility = View.VISIBLE
                        headerRitase1.visibility = View.VISIBLE
                        tvHide.visibility = View.VISIBLE
                        tvShow.visibility = View.GONE
                    }
                    if(snapshot.exists()) {
                        headerRitase.visibility = View.VISIBLE
                        recyclerDetailRitaseList.visibility = View.VISIBLE
                        var c = 0
                        for (postSnapshot in snapshot.children) {
                            val ritaseDataList = postSnapshot.getValue(Ritase::class.java)
                            if (ritaseDataList?.getStatus() != "canceled" && ritaseDataList?.getSupirId() == mData[position].getSopirId() && ritaseDataList?.getUnitId() == mData[position].getTaskUnitId()) {
                                c += 1
                                tvCount.text = c.toString()
                                ritase.add(ritaseDataList!!)
                                tvLoadingStart.isEnabled = true
                                tvLoadingStart.background = ContextCompat.getDrawable(mContext, R.drawable.bg_rounded_red_small)
                                tvLoadingStart.setTextColor(ContextCompat.getColor(mContext, R.color.black))
                                tvLoadingStart.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_pause_24, 0)
                            }
                            if (ritaseDataList?.getStatus().equals("start")) {
//                                tvLoadingStart.isEnabled = false
//                                tvLoadingStart.background = ContextCompat.getDrawable(mContext, R.drawable.bg_rounded_fill_grey)
//                                tvLoadingStart.setTextColor(ContextCompat.getColor(mContext, R.color.grey))
//                                tvLoadingStart.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_pause_24, 0)
                                tvCircleTime.visibility = View.GONE
                                if (ritaseDataList?.getSupirId() == mData[position].getSopirId()) {
                                    printDifferenceDateForHours(
                                        tvUndo,
                                        ritaseDataList?.getTimeStamp(),
                                        tvCircleTime,
                                        tvLocked,
                                        ritaseDataList?.getStatus(),
                                        ritaseDataList,
                                        mData[position].getTaskId(),
                                        tvCount,
                                        position,
                                        recyclerDetailRitaseList,
                                        headerRitase,
                                        tvHide,
                                        tvShow,
                                        llRecyclerDetailRitaseList,
                                        tvCtRite,
                                        ritaseDataList?.getRitaseId().toString(),
                                        tvLoadingStop,
                                        tvLoadingStart,
                                        exaId,
                                        loadingId,
                                        tvStopPending,
                                        tvStopShift
                                    )
                                }
                            }
                            if (ritaseDataList?.getStatus().equals("locked") && ritaseDataList?.getSupirId() == mData[position].getSopirId()) {
                                tvCircleTime.visibility = View.GONE
                                tvLocked.visibility = View.VISIBLE
                                unLocked(ritaseDataList, mData[position].getTaskId(), tvCount, position, recyclerDetailRitaseList, headerRitase, tvHide, tvShow, llRecyclerDetailRitaseList, tvCtRite, tvCircleTime, tvUndo, tvLocked, ritaseDataList?.getRitaseId(), ritaseDataList?.getTimeStamp(), ritaseDataList?.getStatus())
                            }
                            if (ritaseDataList?.getStatus().equals("unLocked")) {
                                tvCircleTime.visibility = View.VISIBLE
                                tvLocked.visibility = View.GONE
                            }
                        }
                        val planUnitAdapter = RitaseAdapter(
                            mContext,
                            ritase,
                            tvCtRite,
                            mData[position].getTaskId().toString(),
                            mAdapterCallback,
                            mData[position].getStatus()
                        )
                        ritase.sortByDescending { it.getUnitId() }
                        recyclerDetailRitaseList.layoutManager = GridLayoutManager(mContext, 1, GridLayoutManager.VERTICAL, false)
                        recyclerDetailRitaseList.adapter = planUnitAdapter
                    }
                    else{
                        headerRitase.visibility = View.GONE
                        recyclerDetailRitaseList.visibility = View.GONE
                    }

                    count += (tvCount.text as String).toInt()
                    tvTotalRitase.text = count.toString()

                }

                override fun onCancelled(error: DatabaseError) {
                    recyclerDetailRitaseList.visibility = View.GONE
                }

            })

    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility", "SimpleDateFormat")
    private fun showNoteDialog() {
        val dialogBuilder = MaterialAlertDialogBuilder(mContext, R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
        val inflater = LayoutInflater.from(mContext)
        dialogView = inflater.inflate(R.layout.layout_note_unit, null)

        val close = dialogView.findViewById<LinearLayout>(R.id.icon_close)
        val etNote = dialogView.findViewById<TextView>(R.id.et_note_keterangan)
        etNote.setOnTouchListener { v, _ ->
            v.isFocusable = false
            v.isFocusableInTouchMode = true
            false
        }
        val btnTimeStart = dialogView.findViewById<ImageView>(R.id.iv_time_start)
        val timeStart = dialogView.findViewById<TextView>(R.id.et_time_start)
        val btnTimeEnd = dialogView.findViewById<ImageView>(R.id.iv_time_end)
        val timeEnd = dialogView.findViewById<TextView>(R.id.et_time_end)

        btnTimeStart.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                timeStart.text = SimpleDateFormat("HH:mm").format(cal.time)
            }
            TimePickerDialog(mContext, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        btnTimeEnd.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                timeEnd.text = SimpleDateFormat("HH:mm").format(cal.time)
            }
            TimePickerDialog(mContext, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        close.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    interface FleetPlanAdapterCallback {
        fun llMenuUnit(
            bindingAdapterPosition: Int,
            mData: ArrayList<PlanUnit>,
            v: View,
            tvCircleTime: TextView
        )
        fun tvUnit(tvSopir: TextView, tvUnit: TextView, llSosial2: LinearLayout)
        fun showFormRemark(
            bindingAdapterPosition: Int,
            mData: java.util.ArrayList<Ritase>,
            v: View?,
            tvRemark: TextView,
            status: String?
        )
    }

    fun printDifferenceDateForHours(
        tvUndo: TextView,
        timeStamp: String?,
        tvCircleTime: TextView,
        tvLocked: TextView,
        status: String?,
        ritaseDataList: Ritase?,
        taskId: String?,
        tvCount: TextView,
        position: Int,
        recyclerDetailRitasList: RecyclerView,
        headerRitase: LinearLayout,
        tvHide: TextView,
        tvShow: TextView,
        llRecyclerDetailRitaseList: LinearLayout,
        tvCtRite: TextView,
        ritaseId: String?,
        tvLoadingStop: TextView,
        tvLoadingStart: TextView,
        exaId: String,
        loadingId: String,
        tvStopPending: TextView,
        tvStopShift: TextView
    ) {
        val currentTime = Calendar.getInstance().time
        val format1 = SimpleDateFormat("dd/MM/yyyy hh:mm:ss", java.util.Locale.getDefault())
        val endDateDay = timeStamp.toString()
        val endDate = format1.parse(endDateDay)
        //milliseconds
        different = (endDate?.time?.plus(60000))?.minus(currentTime.time)!!
        countDownTimer = object : CountDownTimer(different, 1000) {

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

//                tvUndo.text = "$elapsedDays days $elapsedHours hs $elapsedMinutes min $elapsedSeconds sec"
                tvUndo.visibility = View.VISIBLE
                tvUndo.isEnabled = true
                tvCircleTime.visibility = View.GONE
                tvUndo.text = "Batalkan $elapsedSeconds..."
            }

            override fun onFinish() {
                tvUndo.visibility = View.GONE
                if (status.equals("start")) {
//                    tvLoadingStart.background = ContextCompat.getDrawable(mContext, R.drawable.bg_rounded_fill_grey)
//                    tvLoadingStart.setTextColor(ContextCompat.getColor(mContext, R.color.grey))
//                    tvLoadingStart.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_pause_grey, 0)
                    tvLocked.visibility = View.VISIBLE
                    tvCircleTime.visibility = View.GONE
                    tvCircleTime.isEnabled = false
                    locked(ritaseDataList, taskId, tvCount, position, recyclerDetailRitasList, headerRitase, tvHide, tvShow, llRecyclerDetailRitaseList, tvCtRite, tvCircleTime, tvUndo, tvLocked, ritaseId, timeStamp, tvLoadingStop, tvLoadingStart, exaId, loadingId, tvStopPending, tvStopShift)
                }
                else{
//                    tvLocked.visibility = View.GONE
                    tvCircleTime.visibility = View.VISIBLE
                    tvCircleTime.isEnabled = true
                    unLocked(
                        ritaseDataList,
                        taskId,
                        tvCount,
                        position,
                        recyclerDetailRitasList,
                        headerRitase,
                        tvHide,
                        tvShow,
                        llRecyclerDetailRitaseList,
                        tvCtRite,
                        tvCircleTime,
                        tvUndo,
                        tvLocked,
                        ritaseId,
                        timeStamp,
                        status
                    )
                }
            }
        }.start()
    }

    fun locked(
        ritaseDataList: Ritase?,
        taskId: String?,
        tvCount: TextView,
        position: Int,
        recyclerDetailRitasList: RecyclerView,
        headerRitase: LinearLayout,
        tvHide: TextView,
        tvShow: TextView,
        llRecyclerDetailRitaseList: LinearLayout,
        tvCtRite: TextView,
        tvCircleTime: TextView,
        tvUndo: TextView,
        tvLocked: TextView,
        ritaseId: String?,
        timeStamp: String?,
        tvLoadingStop: TextView,
        tvLoadingStart: TextView,
        exaId: String,
        loadingId: String,
        tvStopPending: TextView,
        tvStopShift: TextView
    ) {
        val timeStamp = timeStamp
        val status = "locked"
        val taskId = taskId
        val unitId = ritaseDataList?.getUnitId()
        val unitCode = ritaseDataList?.getUnitCode()
        val supirId = ritaseDataList?.getSupirId()
        val supir = ritaseDataList?.getSupir()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val refRitase = FirebaseDatabase.getInstance().reference
        val ritaseId = ritaseId

        val ritaseHashMap = HashMap<String, Any>()
        ritaseHashMap["ritaseId"] = ritaseId.toString()
        ritaseHashMap["timeStamp"] = timeStamp.toString()
        ritaseHashMap["status"] = status
        ritaseHashMap["taskId"] = taskId.toString()
        ritaseHashMap["unitId"] = unitId.toString()
        ritaseHashMap["unitCode"] = unitCode.toString()
        ritaseHashMap["supirId"] = supirId.toString()
        ritaseHashMap["supir"] = supir.toString()
        ritaseHashMap["uid"] = uid.toString()

        databaseReference = refRitase.child("Ritase")
            .child(taskId.toString())
            .child(unitId.toString())
            .child(ritaseId.toString())
        databaseReference.updateChildren(ritaseHashMap)
            .addOnCompleteListener {

//                loadingStop(taskId, exaId, loadingId, tvLoadingStop, tvLoadingStart, tvCircleTime, tvLocked, tvStopShift, tvStopPending)

                if (ritaseDataList?.getSupirId() == supirId) {
                    different = 0
                    unLocked(
                        ritaseDataList,
                        taskId,
                        tvCount,
                        position,
                        recyclerDetailRitasList,
                        headerRitase,
                        tvHide,
                        tvShow,
                        llRecyclerDetailRitaseList,
                        tvCtRite,
                        tvCircleTime,
                        tvUndo,
                        tvLocked,
                        ritaseId,
                        timeStamp,
                        status,
                    )
                }
            }
    }

    private fun loadingStop(
        taskId: String?,
        exaId: String,
        loadingId: String,
        tvLoadingStop: TextView,
        tvLoadingStart: TextView,
        tvCircleTime: TextView,
        tvLocked: TextView,
        tvStopShift: TextView,
        tvStopPending: TextView
    ) {
        val refShareContact = FirebaseDatabase.getInstance().reference
        refShareContact.child("LoadingTime")
            .child(taskId!!)
            .child(exaId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapLoading: DataSnapshot) {
                    if (snapLoading.exists()) {
                        for (snapDataLoading in snapLoading.children) {
                            val loadingData = snapDataLoading.getValue(LoadingTime::class.java)
                            if (loadingData?.getStatus().equals("start") || loadingData?.getStatus().equals("process")) {
                                val refLoading = FirebaseDatabase.getInstance().reference

                                val loadingHashMap = HashMap<String, Any>()
                                loadingHashMap["loadingId"] = loadingData?.getLoadingId().toString()
                                loadingHashMap["timeStamp"] = loadingData?.getTimeStamp().toString()
                                loadingHashMap["status"] = "stop"
                                loadingHashMap["taskId"] = loadingData?.getTaskId().toString()
                                loadingHashMap["unitId"] = loadingData?.getUnitId().toString()
                                loadingHashMap["unitCode"] = loadingData?.getUnitCode().toString()
                                loadingHashMap["operatorId"] = loadingData?.getOperatorId().toString()
                                loadingHashMap["operator"] = loadingData?.getOperator().toString()
                                loadingHashMap["uid"] = loadingData?.getUid().toString()
                                databaseReference = refLoading
                                    .child("LoadingTime")
                                    .child(taskId.toString())
                                    .child(exaId)
                                    .child(loadingData?.getLoadingId().toString())
                                databaseReference.updateChildren(loadingHashMap)
                                    .addOnCompleteListener {
                                        s = "stop"
                                        tvLoadingStart.visibility = View.VISIBLE
                                        tvLoadingStart.isEnabled = true
                                        tvLoadingStop.visibility = View.GONE
                                        tvCircleTime.visibility = View.GONE
                                        tvCircleTime.isEnabled = false
                                        tvLocked.visibility = View.VISIBLE
                                        tvStopShift.isEnabled = true
//                                        tvStopShift.visibility = View.VISIBLE
                                        tvStopPending.visibility = View.GONE

                                        planUnitList = ArrayList()
                                        val refTask = FirebaseDatabase.getInstance().reference
                                        refTask.child("TasksFleetPlanUnit")
                                            .orderByChild("taskId")
                                            .equalTo(loadingData?.getTaskId().toString())
                                            .addValueEventListener(object : ValueEventListener {
                                                override fun onDataChange(snapPlan: DataSnapshot) {
                                                    if(snapPlan.exists()) {
//                                                        var count = 0
                                                        for (postSnapshot in snapPlan.children) {
                                                            val planUnitDataList = postSnapshot.getValue(PlanUnit::class.java)
                                                            if (planUnitDataList?.getStatus() != "delete") {
                                                                planUnitList?.add(planUnitDataList!!)
                                                                tvCircleTime.isEnabled = false
                                                                tvCircleTime.visibility = View.GONE
                                                                tvLocked.visibility = View.VISIBLE
                                                            }

//                                                            val refRitase = FirebaseDatabase.getInstance().reference
//                                                            refRitase.child("Ritase")
//                                                                .child(taskId.toString())
//                                                                .child(planUnitDataList?.getTaskUnitId().toString())
//                                                                .addValueEventListener(object : ValueEventListener {
//                                                                    override fun onDataChange(snapshot2: DataSnapshot) {
//                                                                        if(snapshot2.exists()) {
//                                                                            for (postSnapshot2 in snapshot2.children) {
//                                                                                val ritaseDataList = postSnapshot2.getValue(Ritase::class.java)
//                                                                                if (ritaseDataList?.getStatus() != "canceled" && ritaseDataList?.getTaskId() == taskId) {
//                                                                                    count += 1
//                                                                                    tvTotalRitase.text = count.toString()
//                                                                                    tvTarget.text = HtmlCompat.fromHtml(
//                                                                                        "<i>Actual/Target: </i>$count/$plan",
//                                                                                        HtmlCompat.FROM_HTML_MODE_LEGACY
//                                                                                    )
//                                                                                }
//                                                                            }
//                                                                        }
//                                                                    }
//
//                                                                    override fun onCancelled(error: DatabaseError) {
//                                                                    }
//
//                                                                })
                                                        }
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {

                                                }

                                            })
                                    }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    fun unLocked(
        ritaseDataList: Ritase?,
        taskId: String?,
        tvCount: TextView,
        position: Int,
        recyclerDetailRitasList: RecyclerView,
        headerRitase: LinearLayout,
        tvHide: TextView,
        tvShow: TextView,
        llRecyclerDetailRitaseList: LinearLayout,
        tvCtRite: TextView,
        tvCircleTime: TextView,
        tvUndo: TextView,
        tvLocked: TextView,
        ritaseId: String?,
        timeStamp: String?,
        status: String?
    ) {
        val currentTime = Calendar.getInstance().time
        val format1 = SimpleDateFormat("dd/MM/yyyy hh:mm:ss", java.util.Locale.getDefault())
        val endDateDay = timeStamp.toString()
        val endDate = format1.parse(endDateDay)

        //milliseconds
        val different = (endDate?.time?.plus(300000))?.minus(currentTime.time)
        countDownTimer = object : CountDownTimer(different!!, 1000) {

            @SuppressLint("SetTextI18n")
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

//                tvUndo.text = "$elapsedDays days $elapsedHours hs $elapsedMinutes min $elapsedSeconds sec"
                tvUndo.visibility = View.GONE
                tvCircleTime.visibility = View.GONE
                tvLocked.text = "Locked $elapsedMinutes:$elapsedSeconds"
            }

            override fun onFinish() {
                tvUndo.visibility = View.GONE
                if (status.equals("locked")) {
//                    tvLocked.visibility = View.VISIBLE
                    tvCircleTime.visibility = View.GONE
                    tvCircleTime.isEnabled = false
                    setUnlocked(ritaseDataList!!, taskId, tvCount, position, recyclerDetailRitasList, headerRitase, tvHide, tvShow, llRecyclerDetailRitaseList, tvCtRite, tvCircleTime, tvUndo, tvLocked, ritaseId!!, timeStamp)
                }
                else{
                    tvUndo.visibility = View.GONE
//                    tvLocked.visibility = View.GONE
                    tvCircleTime.visibility = View.VISIBLE
                    tvCircleTime.isEnabled = true
                }
            }
        }.start()
    }

    fun setUnlocked(
        ritaseDataList: Ritase,
        taskId: String?,
        tvCount: TextView,
        position: Int,
        recyclerDetailRitasList: RecyclerView,
        headerRitase: LinearLayout,
        tvHide: TextView,
        tvShow: TextView,
        llRecyclerDetailRitaseList: LinearLayout,
        tvCtRite: TextView,
        tvCircleTime: TextView,
        tvUndo: TextView,
        tvLocked: TextView,
        ritaseId: String,
        timeStamp: String?
    ) {
        val timeStamp = timeStamp
        val status = "UnLocked"
        val taskId = taskId
        val unitId = ritaseDataList.getUnitId()
        val unitCode = ritaseDataList.getUnitCode()
        val supirId = ritaseDataList.getSupirId()
        val supir = ritaseDataList.getSupir()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val refRitase = FirebaseDatabase.getInstance().reference
        val ritaseId = ritaseId

        val ritaseHashMap = HashMap<String, Any>()
        ritaseHashMap["ritaseId"] = ritaseId
        ritaseHashMap["timeStamp"] = timeStamp.toString()
        ritaseHashMap["status"] = status
        ritaseHashMap["taskId"] = taskId.toString()
        ritaseHashMap["unitId"] = unitId.toString()
        ritaseHashMap["unitCode"] = unitCode.toString()
        ritaseHashMap["supirId"] = supirId.toString()
        ritaseHashMap["supir"] = supir.toString()
        ritaseHashMap["uid"] = uid.toString()

        databaseReference = refRitase.child("Ritase").child(taskId.toString()).child(unitId.toString()).child(ritaseId)
        databaseReference.updateChildren(ritaseHashMap)
            .addOnCompleteListener {
                val refTask = FirebaseDatabase.getInstance().reference
                refTask.child("TasksFleetPlanUnit")
                    .orderByChild("taskId")
                    .equalTo(taskId.toString())
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                tvUndo.visibility = View.GONE
                                tvLocked.visibility = View.GONE
                                tvCircleTime.visibility = View.VISIBLE
                                tvCircleTime.isEnabled = true
                            }
                            else{
                                tvUndo.visibility = View.GONE
                                tvLocked.visibility = View.GONE
                                tvCircleTime.visibility = View.GONE
                                tvCircleTime.isEnabled = false
                            }
//                            for (postSnapshot in snapshot.children) {
//                                val planUnitDataList = postSnapshot.getValue(PlanUnit::class.java)
//                                if (planUnitDataList?.getTaskUnitId() == unitId.toString()) {
                                    tvUndo.visibility = View.GONE
                                    tvLocked.visibility = View.GONE
                                    tvCircleTime.visibility = View.VISIBLE
                                    tvCircleTime.isEnabled = true
//                                }
//                                else{
//                                    tvUndo.visibility = View.GONE
//                                    tvLocked.visibility = View.GONE
//                                    tvCircleTime.visibility = View.GONE
//                                    tvCircleTime.isEnabled = false
//                                }
//                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            tvUndo.visibility = View.GONE
                            tvLocked.visibility = View.GONE
                            tvCircleTime.visibility = View.GONE
                            tvCircleTime.isEnabled = false
                        }

                    })
            }
    }

    fun setUndo(
        ritaseDataList: Ritase,
        taskId: String?,
        tvCircleTime: TextView,
        tvUndo: TextView,
        tvLocked: TextView,
        ritaseId: String,
        timeStamp: String?,
        position: Int,
        recyclerDetailRitaseList: RecyclerView,
        headerRitase: LinearLayout,
        tvCount: TextView,
        tvCtRite: TextView
    ) {
        tvUndo.isEnabled = false
        tvUndo.visibility = View.GONE
        different = 0
        countDownTimer.cancel()
        count -= 1
        tvTotalRitase.text = count.toString()
        tvTarget.text = HtmlCompat.fromHtml("<i>A/T: </i>$count/$plan", HtmlCompat.FROM_HTML_MODE_LEGACY)
        tvBcm.text = HtmlCompat.fromHtml("<i>"+FormatNumber().simpleNumber((count*22))+"</i>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        val timeStamp = timeStamp
        val status = "canceled"
        val taskId = taskId
        val unitId = ritaseDataList.getUnitId()
        val unitCode = ritaseDataList.getUnitCode()
        val supirId = ritaseDataList.getSupirId()
        val supir = ritaseDataList.getSupir()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val refRitase = FirebaseDatabase.getInstance().reference
        val ritaseId = ritaseId

        val ritaseHashMap = HashMap<String, Any>()
        ritaseHashMap["ritaseId"] = ritaseId
        ritaseHashMap["timeStamp"] = timeStamp.toString()
        ritaseHashMap["status"] = status
        ritaseHashMap["taskId"] = taskId.toString()
        ritaseHashMap["unitId"] = unitId.toString()
        ritaseHashMap["unitCode"] = unitCode.toString()
        ritaseHashMap["supirId"] = supirId.toString()
        ritaseHashMap["supir"] = supir.toString()
        ritaseHashMap["uid"] = uid.toString()

        databaseReference = refRitase
            .child("Ritase")
            .child(taskId.toString())
            .child(unitId.toString())
            .child(ritaseId)
        databaseReference.updateChildren(ritaseHashMap)
            .addOnCompleteListener {
                val refShareContact = FirebaseDatabase.getInstance().reference
                ritase = ArrayList()
                refShareContact.child("Ritase")
                    .child(mData[position].getTaskId().toString())
                    .child(mData[position].getTaskUnitId().toString())
//                    .child(ritaseId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot3: DataSnapshot) {
                            ritase?.clear()
                            if(snapshot3.exists()) {
                                tvLocked.visibility = View.VISIBLE
                                headerRitase.visibility = View.VISIBLE
                                recyclerDetailRitaseList.visibility = View.VISIBLE
                                var c = 0
                                for (postSnapshot3 in snapshot3.children) {
                                    val ritaseDataList3 = postSnapshot3.getValue(Ritase::class.java)
                                    if (ritaseDataList3?.getStatus() != "canceled" && ritaseDataList3?.getSupirId() == supirId && ritaseDataList3?.getUnitId() == unitId) {
                                        c += 1
                                        tvCount.text = c.toString()
                                        ritase!!.add(ritaseDataList3!!)
                                    }
                                }
                                val planUnitAdapter = RitaseAdapter(
                                    mContext,
                                    ritase!!,
                                    tvCtRite,
                                    mData[position].getTaskId().toString(),
                                    mAdapterCallback,
                                    mData[position].getStatus()
                                )
                                ritase!!.sortByDescending { it.getUnitId() }
                                recyclerDetailRitaseList.layoutManager = GridLayoutManager(mContext, 1, GridLayoutManager.VERTICAL, false)
                                recyclerDetailRitaseList.adapter = planUnitAdapter
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })

                val currentTime = Calendar.getInstance().time
                val format1 = SimpleDateFormat("dd/MM/yyyy hh:mm:ss", java.util.Locale.getDefault())
                val endDateDay = timeStamp.toString()
                val endDate = format1.parse(endDateDay)

                //milliseconds
                val different = (endDate?.time?.plus(20000))?.minus(currentTime.time)!!
                countDownTimer = object : CountDownTimer(different, 1000) {

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
                        tvLocked.text = "Pending $elapsedSeconds..."
                    }

                    override fun onFinish() {
                        tvUndo.visibility = View.GONE
                        tvLocked.visibility = View.GONE
                        tvCircleTime.visibility = View.VISIBLE
                        tvCircleTime.isEnabled = true
                    }
                }.start()
            }
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