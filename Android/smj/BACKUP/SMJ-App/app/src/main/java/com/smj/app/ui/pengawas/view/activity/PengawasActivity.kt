package com.smj.app.ui.pengawas.view.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daasuu.bl.ArrowDirection
import com.daasuu.bl.BubbleLayout
import com.daasuu.bl.BubblePopupHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.smj.app.R
import com.smj.app.databinding.ActivityPengawasBinding
import com.smj.app.helper.DateHelper
import com.smj.app.helper.FormatNumber
import com.smj.app.helper.Helper
import com.smj.app.helper.NavigationHelper
import com.smj.app.ui.auth.model.Users
import com.smj.app.ui.auth.view.LoginActivity
import com.smj.app.ui.fleet.model.UnitList
import com.smj.app.ui.main.view.activity.MainActivity
import com.smj.app.ui.main.view.activity.PendingActivity
import com.smj.app.ui.main.view.activity.SuspendActivity
import com.smj.app.ui.main.viewModel.TaskPengawasViewModel
import com.smj.app.ui.pengawas.model.FleetHistoryTime
import com.smj.app.ui.pengawas.model.LoadingTime
import com.smj.app.ui.pengawas.model.Ritase
import com.smj.app.ui.pengawas.view.adapter.FleetPlanAdapter
import com.smj.app.ui.pengawas.view.adapter.FleetPlanDetailAdapter
import com.smj.app.ui.pengawas.view.adapter.FleetPlanUnitAdapter
import com.smj.app.ui.pengawas.view.adapter.HistoryTimeAdapter
import com.smj.app.ui.profile.activity.ProfileActivity
import com.smj.app.ui.task.model.PlanUnit
import com.smj.app.ui.task.model.TaskGroupList
import com.smj.app.ui.task.view.AddTaskActivity
import com.smj.app.ui.task.view.ContactListActivity
import com.smj.app.ui.task.view.EditTaskActivity
import com.smj.app.ui.task.view.ProductListActivity
import com.smj.app.utils.response.BaseResponseFirebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PengawasActivity : AppCompatActivity(), FleetPlanAdapter.FleetPlanAdapterCallback, FleetPlanUnitAdapter.FleetPlanUnitAdapterCallback{
    private lateinit var binding: ActivityPengawasBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var firebaseUser: FirebaseUser? = null
    private var usersRefrence: DatabaseReference? = null
    private lateinit var databaseReference: DatabaseReference
    private val taskPengawasViewModel by viewModels<TaskPengawasViewModel>()
    private lateinit var recyclePlanUnitList: RecyclerView
    private lateinit var recycleHistoryList: RecyclerView
    private var planUnitList: ArrayList<PlanUnit>? = null
    private var fleetHistoryTime: ArrayList<FleetHistoryTime>? = null

    private var context: Context? = null

    private lateinit var recyclerViewFleetList: RecyclerView
    private var taskGroupList: ArrayList<TaskGroupList>? = null

    private val myCalendar: Calendar = Calendar.getInstance()
    lateinit var dialogView: View

    var tvSopir: TextView? = null
    var tvSopirId: TextView? = null
    var tvSopirPosition: TextView? = null

    var tvUnit: TextView? = null
    var tvUnitId: TextView? = null
    var tvUnitCode: TextView? = null

    var tvUnitCodeExchange: TextView? = null
    var tvUnitIdExchange: TextView? = null

    var tvOperatorNameExchange: TextView? = null
    var tvOperatorIdExchange: TextView? = null

    var llEmpty: LinearLayout? = null

    var user: Users? = null

    var dataRemark = "...."
    var addExchange: LinearLayout? = null
    var history: String? = null

    //start operator
    private val sopirResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK){
                tvSopir?.text = result.data?.getStringExtra("userName")
                tvSopirId?.text = result.data?.getStringExtra("userId")
                tvSopirPosition?.text = result.data?.getStringExtra("userPosition")
            }
        }
    private val operatorExchange =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK){
                tvOperatorNameExchange?.text = result.data?.getStringExtra("userName")
                tvOperatorIdExchange?.text = result.data?.getStringExtra("userId")
//                tvSopirPosition?.text = result.data?.getStringExtra("userPosition")
            }
        }

    //start unit
    private val unitResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK){
                tvUnit?.text = result.data?.getStringExtra("unitType")
                tvUnitId?.text = result.data?.getStringExtra("unitId")
                tvUnitCode?.text = result.data?.getStringExtra("unitCode")
            }
        }

    private val unitExchange =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK){
                tvUnitIdExchange?.text = result.data?.getStringExtra("unitId")
                tvUnitCodeExchange?.text = result.data?.getStringExtra("unitCode")
                addExchange?.isEnabled = true
            }
        }

    private val lostTimeResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK){
                dialogView.findViewById<TextView>(R.id.et_lost_time).text = result.data?.getStringExtra("lostTimeName")
                dialogView.findViewById<TextView>(R.id.tv_lost_time_id).text = result.data?.getStringExtra("lostTimeId")
            }
        }

    private val addTaksResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if (result.resultCode == RESULT_OK){
            binding.llProgressBar.preload.visibility = View.GONE
        }
        else{
            binding.llProgressBar.preload.visibility = View.GONE
        }
    }

    @SuppressLint("SourceLockedOrientationActivity", "PrivateResource", "NotifyDataSetChanged",
        "SimpleDateFormat", "ClickableViewAccessibility"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPengawasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseDatabase.getInstance().reference.child("ep1").keepSynced(true)

        context = this.applicationContext
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        checkUserCurrent()

        recyclerViewFleetList = binding.recyclerViewFleetList
        recyclerViewFleetList.setHasFixedSize(true)
        recyclerViewFleetList.layoutManager = LinearLayoutManager(context)
        recyclerViewFleetList.clearOnChildAttachStateChangeListeners()

        taskGroupList = ArrayList()

        planUnitList = ArrayList()
        fleetHistoryTime = ArrayList()

        val connectedRef = Firebase.database.getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    Log.d(TAG, "connected")
                    binding.tvOnline.visibility = View.VISIBLE
                    binding.tvOffline.visibility = View.GONE
                } else {
                    Log.d(TAG, "not connected")
                    binding.tvOnline.visibility = View.GONE
                    binding.tvOffline.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Listener was cancelled")
            }
        })

        isNetworkAvailable(this@PengawasActivity).run {
            binding.tvOnline.visibility = View.VISIBLE
            binding.tvOffline.visibility = View.GONE
            showFleetPlan()
        }

        if(!isNetworkAvailable(this@PengawasActivity)){
            Helper().showToast(context!!.getString(R.string.connection_status), this)
        }

        this.let {
            ContextCompat.getColor(
                it,
                R.color.green_light
            )
        }.let {
            Helper().changeStatusBarColor(
                it, true,
                this
            )
        }

        this.let {
            ContextCompat.getColor(
                it,
                R.color.green_light
            )
        }.let {
            Helper().changeStatusNavColor(
                it, true,
                this
            )
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val currentDate = sdf.format(Date())
        binding.tvDate.text = currentDate

        val date = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, day)
            updateLabel()
        }
        binding.ivDate.setOnClickListener {
            DatePickerDialog(
                this,
                date,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.tvDate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,before: Int, count: Int) {
                binding.recyclerViewFleetList.visibility = View.GONE
                binding.shimmerViewContainer.visibility = View.VISIBLE
                showFleetPlan()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.tvSettings.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            NavigationHelper().navigateToActivityCallback(this, intent)
        }

        FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val user: Users? = p0.getValue(Users::class.java)

                        if (
                            user?.getPosition() == "Foreman Produksi"
                            || user?.getPosition() == "Sr Foreman Produksi"
                            || user?.getPosition() == "Jr Foreman Produksi"
                            || user?.getPosition() == "SPV Produksi"
                            || user?.getPosition() == "Jr SPV Produksi"
                        ) {
                            showFleetPlan()
                        }
                        else{
                            val intent = Intent(this@PengawasActivity, MainActivity::class.java)
                            NavigationHelper().navigateToActivity(this@PengawasActivity, intent)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        taskPengawasViewModel.fDbTaskResult?.observe(this){
            when (it) {
                is BaseResponseFirebase.TaskShowSuccess -> {
                    (taskGroupList as ArrayList).clear()
                    if (it.value?.isNotEmpty() == true) {
                        binding.shimmerViewContainer.visibility = View.GONE
                        recyclerViewFleetList.visibility = View.VISIBLE
                        binding.llEmpty.visibility = View.GONE
                        binding.llProgressBar.preload.visibility = View.GONE
                        for (data in it.value) {
                            (taskGroupList as ArrayList).add(data)
                        }
                        val fleetPlanAdapter = FleetPlanAdapter(
                            this,
                            taskGroupList!!,
                            this@PengawasActivity
                        )
                        recyclerViewFleetList.clearOnChildAttachStateChangeListeners()
                        recyclerViewFleetList.layoutManager = LinearLayoutManager(this@PengawasActivity)
                        recyclerViewFleetList.adapter = fleetPlanAdapter
                        recyclerViewFleetList.adapter?.notifyDataSetChanged()
                        fleetPlanAdapter.notifyDataSetChanged()
                    }
                    else {
                        binding.llProgressBar.preload.visibility = View.GONE
                        binding.shimmerViewContainer.visibility = View.GONE
                        recyclerViewFleetList.visibility = View.GONE
                        binding.llEmpty.visibility = View.VISIBLE
                    }
                }
                is BaseResponseFirebase.Failed -> {
                    binding.llProgressBar.preload.visibility = View.GONE
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewFleetList.visibility = View.GONE
                    binding.llEmpty.visibility = View.VISIBLE
                }
                else -> {
                    binding.llProgressBar.preload.visibility = View.GONE
                    binding.shimmerViewContainer.visibility = View.GONE
                }
            }
        }

        binding.addTaks.setOnClickListener {
            binding.llProgressBar.preload.visibility = View.VISIBLE
            val intent = Intent(this@PengawasActivity, AddTaskActivity::class.java)
            addTaksResult.launch(intent)
        }
    }

    private fun updateLabel() {
        val myFormat = "dd/MM/yyyy"
        val dateFormat = SimpleDateFormat(myFormat, Locale.US)
        binding.tvDate.text = dateFormat.format(myCalendar.time)
    }

    override fun onDetail(
        bindingAdapterPosition: Int,
        mData: ArrayList<TaskGroupList>,
        ivDown: ImageView
    ) {
        val ivDown = ivDown
        binding.llProgressBar.preload.visibility = View.VISIBLE
        showFormDialog(bindingAdapterPosition, mData, ivDown)
    }

    override fun onLostTime(bindingAdapterPosition: Int, mData: ArrayList<TaskGroupList>) {
        binding.llProgressBar.preload.visibility = View.VISIBLE
//        showLostTimeDialog(
//            mData,
//            bindingAdapterPosition,
//            recyclerDetailFleetList,
//            tvTotalRitase,
//            tvTarget,
//            tvLoadingStop,
//            tvLoadingStart,
//            ivUp,
//            ivDown,
//            tvLoadingId,
//            tvStopShift,
//            tvStopPending,
//            status
//        )
    }

    override fun ivUp(
        bindingAdapterPosition: Int,
        mData: ArrayList<TaskGroupList>,
        recyclerDetailFleetList: RecyclerView,
        ivUp: ImageView,
        ivDown: ImageView
    ) {
        recyclerDetailFleetList.visibility = View.GONE
        ivUp.visibility = View.GONE
        ivDown.visibility = View.VISIBLE
    }

    override fun ivDown(
        bindingAdapterPosition: Int,
        mData: ArrayList<TaskGroupList>,
        recyclerDetailFleetList: RecyclerView,
        ivUp: ImageView,
        ivDown: ImageView
    ) {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        ivDown.isEnabled = false
        val refTasks = FirebaseDatabase.getInstance().reference
            .child("TasksFleetPlan")
            .child(mData[bindingAdapterPosition].getTaskId().toString())
        refTasks.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val task = snapshot.getValue(TaskGroupList::class.java)

                    if (task?.getStatus().equals("todo")) {
                        showFormDialog(bindingAdapterPosition, mData, ivDown)
                    }
                    else{
                        binding.llProgressBar.preload.visibility = View.GONE
                        ivDown.isEnabled = true
                        recyclerDetailFleetList.visibility = View.VISIBLE
                        ivUp.visibility = View.VISIBLE
                        ivDown.visibility = View.GONE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun tvDetailMenu(
        bindingAdapterPosition: Int,
        mData: ArrayList<TaskGroupList>,
        v: View,
        ivDown: ImageView
    ) {
        @SuppressLint("InflateParams")
        val bubbleLayout = LayoutInflater.from(this).inflate(R.layout.bubble_menu, null) as BubbleLayout
        val location = IntArray(2)
        v.getLocationInWindow(location)
        bubbleLayout.arrowDirection = ArrowDirection.TOP
        val popupWindow = BubblePopupHelper.create(this, bubbleLayout)
        bubbleLayout.arrowDirection = ArrowDirection.TOP
        popupWindow.showAtLocation(
            v,
            Gravity.TOP,
            location[0],
            v.height + location[1]
        )

        bubbleLayout.findViewById<TextView>(R.id.bubble_add_unit).setOnClickListener {
            binding.llProgressBar.preload.visibility = View.VISIBLE
            showFormDialog(bindingAdapterPosition, mData, ivDown)
            popupWindow.dismiss()
        }

//        bubbleLayout.findViewById<TextView>(R.id.bubble_change_exa).setOnClickListener {
//            binding.llProgressBar.preload.visibility = View.VISIBLE
//            popupWindow.dismiss()
//            formExaChange(mData, bindingAdapterPosition,  v, popupWindow)
//        }

//        bubbleLayout.findViewById<TextView>(R.id.bubble_lost_time).setOnClickListener {
//            binding.llProgressBar.preload.visibility = View.VISIBLE
//            showLostTimeDialog(
//                mData,
//                bindingAdapterPosition,
//                recyclerDetailFleetList,
//                tvTotalRitase,
//                tvTarget,
//                tvLoadingStop,
//                tvLoadingStart,
//                ivUp,
//                ivDown,
//                tvLoadingId,
//                tvStopShift,
//                tvStopPending,
//                status
//            )
//            popupWindow.dismiss()
//        }
    }

    override fun tvUnitExe(
        bindingAdapterPosition: Int,
        mData: ArrayList<TaskGroupList>,
        tvOperator: TextView,
        tvUnitExe: TextView,
        llBarFirst: LinearLayout,
        llBarSecond: LinearLayout,
        llSosial: LinearLayout
    ) {
        if (!tvOperator.isEnabled) {
            tvOperator.visibility = View.VISIBLE
            tvOperator.isEnabled = true
            llBarFirst.visibility = View.VISIBLE
            llBarSecond.visibility = View.VISIBLE
            llSosial.visibility = View.VISIBLE
            tvUnitExe.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_excavator, 0, R.drawable.icon_up_small, 0)
        }
        else{
            tvOperator.isEnabled = false
            tvOperator.visibility = View.GONE
            llBarFirst.visibility = View.GONE
            llBarSecond.visibility = View.GONE
            llSosial.visibility = View.GONE
            tvUnitExe.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_excavator, 0, R.drawable.arrow_down_24, 0)
        }
    }

    override fun tvStartShift(
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
    ) {
        val refTasks = FirebaseDatabase.getInstance().reference
            .child("TasksFleetPlan")
            .child(mData[bindingAdapterPosition].getTaskId().toString())
        refTasks.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val task = snapshot.getValue(TaskGroupList::class.java)

                    if (task?.getStatus().equals("todo")) {
                        val refTasksUnit = FirebaseDatabase.getInstance().reference
                            .child("TasksFleetPlanUnit")
                            .orderByChild("taskId")
                            .equalTo(mData[bindingAdapterPosition].getTaskId().toString())
                        refTasksUnit.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshotUnit: DataSnapshot) {
                                if(snapshotUnit.exists()){
                                    dialogLoading(
                                        mData,
                                        bindingAdapterPosition,
                                        recyclerDetailFleetList,
                                        ivUp,
                                        ivDown,
                                        tvLoadingStart,
                                        tvStartShift,
                                        tvStopPending,
                                        s,
                                        v,
                                        tvBcm,
                                        tvTotalRitase
                                    )
                                }
                                else{
                                    dialogStart(
                                        mData,
                                        bindingAdapterPosition,
                                        recyclerDetailFleetList,
                                        ivUp,
                                        ivDown,
                                        tvLoadingStart,
                                        tvStartShift,
                                        tvStopPending,
                                        s,
                                        v,
                                        tvBcm,
                                        tvTotalRitase
                                    )
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun tvLoadingStart(
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
        tvHistory: TextView
    ) {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        showLostTimeDialog(
            mData,
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
            status,
            tvBcm,
            tvHistory
        )
//        timeStampUpdate(
//            mData,
//            bindingAdapterPosition,
//            recyclerDetailFleetList,
//            tvTotalRitase,
//            tvTarget,
//            tvLoadingStop,
//            tvLoadingStart,
//            ivUp,
//            ivDown,
//            tvLoadingId,
//            tvStopShift,
//            tvStopPending,
//            status
//        )
    }

    override fun tvLoadingStop(
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
        tvHistory: TextView
    ) {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        showLostTimeDialog(
            mData,
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
            status,
            tvBcm,
            tvHistory
        )

//        val bottomSheet = BottomSheetUnit(bindingAdapterPosition, "Unit Status")
//        bottomSheet.show(FragmentManager.findFragmentManager(v), bottomSheet.tag)

//        val alertBuilder = android.app.AlertDialog.Builder(this)
//        alertBuilder.setTitle("Loading Stop")
//        alertBuilder.setMessage(HtmlCompat.fromHtml("Proses ini akan mencatat <i><b>'Operasional berhenti'!</b></i><br/><br/> Apakah anda yakin?", HtmlCompat.FROM_HTML_MODE_LEGACY))
//        alertBuilder.setCancelable(false)
//        alertBuilder.setPositiveButton("Lanjutkan"){_,_ ->
//            timeStampUpdate(
//                mData,
//                bindingAdapterPosition,
//                recyclerDetailFleetList,
//                tvTotalRitase,
//                tvTarget,
//                tvLoadingStop,
//                tvLoadingStart,
//                ivUp,
//                ivDown,
//                tvLoadingId,
//                tvStopShift,
//                tvStopPending,
//                status
//            )
//        }
//        alertBuilder.setNegativeButton("Batal"){dialog,_ ->
//            dialog.cancel()
//            dialog.dismiss()
//            tvLoadingStop.visibility = View.VISIBLE
//            tvLoadingStop.isEnabled = true
//            tvLoadingStart.visibility = View.GONE
//            tvStopShift.isEnabled = false
//            tvStopShift.visibility = View.GONE
//            tvStopPending.visibility = View.VISIBLE
//        }
//        alertBuilder.show()
    }

    private fun timeStampUpdate(
        mData: ArrayList<TaskGroupList>,
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
        status: String,
        tvBcm: TextView,
    ) {
        val timeStamp = Helper().dateTimeNow()
        val statusFleet = mData[bindingAdapterPosition].getStatus()
        val taskId = mData[bindingAdapterPosition].getTaskId()
        val unitId = mData[bindingAdapterPosition].getExaId()
        val unitCode = mData[bindingAdapterPosition].getExaCode()
        val supirId = mData[bindingAdapterPosition].getOperatorId()
        val supir = mData[bindingAdapterPosition].getOperatorName()
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        val refLoading = FirebaseDatabase.getInstance().reference
        val loadingId = refLoading.push().key
//        if (tvLoadingId.text.isNotEmpty()) {
//            loadingId = tvLoadingId.text.toString()
//        }

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

        if(isNetworkAvailable(this)) {
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
                                            tvBcm
                                        )
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })
                    }
                    else {
                        val toast = Toast.makeText(
                            this,
                            "Proccessing unsuccessfully",
                            Toast.LENGTH_LONG
                        )
                        toast.setGravity(Gravity.CENTER, 0, 0)
                        toast.show()
                    }
                }
        }
        else{
            Toast.makeText(this, getString(R.string.connection_status), Toast.LENGTH_LONG).show()
        }
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
        tvBcm: TextView
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
                        binding.llProgressBar.preload.visibility = View.GONE
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
                                            binding.llProgressBar.preload.visibility = View.GONE
                                            for (postSnapshot2 in snapshot2.children) {
                                                val ritaseDataList = postSnapshot2.getValue(Ritase::class.java)
                                                if (ritaseDataList?.getStatus() != "canceled" && ritaseDataList?.getTaskId() == taskId) {
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
                                            if (loadingTime?.getStatus().equals("start") && status != "done") {
                                                tvLoadingStop.visibility = View.VISIBLE
                                                tvLoadingStart.visibility = View.GONE
                                                tvStopShift.isEnabled = false
                                                tvStopShift.visibility = View.GONE
                                                tvStopPending.visibility = View.VISIBLE
                                                s = loadingTime?.getStatus().toString()
                                            }
                                            if (loadingTime?.getStatus().equals("stop") && status != "done") {
                                                tvLoadingStop.visibility = View.GONE
                                                tvLoadingStart.visibility = View.VISIBLE
                                                tvLoadingStart.background = ContextCompat.getDrawable(this@PengawasActivity, R.drawable.bg_rounded_fill_grey)
                                                tvLoadingStart.setTextColor(ContextCompat.getColor(this@PengawasActivity, R.color.black))
                                                tvLoadingStart.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_play_arrow_24, 0)
                                                tvStopShift.isEnabled = true
                                                tvStopShift.visibility = View.VISIBLE
                                                tvStopPending.visibility = View.GONE
                                                s = loadingTime?.getStatus().toString()
                                            }
                                        }
                                        recyclerDetailFleetList.visibility = View.VISIBLE
                                        planUnitList?.sortByDescending { it.getSopir() }
                                        val planUnitAdapter = FleetPlanDetailAdapter(
                                            this@PengawasActivity,
                                            planUnitList!!,
                                            tvTotalRitase,
                                            this@PengawasActivity,
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
                                        recyclerDetailFleetList.layoutManager = LinearLayoutManager(this@PengawasActivity)
                                        recyclerDetailFleetList.adapter = planUnitAdapter
                                    }
                                    else{
                                        recyclerDetailFleetList.visibility = View.GONE
                                        planUnitList?.sortByDescending { it.getSopir() }
                                        val planUnitAdapter = FleetPlanDetailAdapter(
                                            this@PengawasActivity,
                                            planUnitList!!,
                                            tvTotalRitase,
                                            this@PengawasActivity,
                                            tvTarget,
                                            plan,
                                            s,
                                            tvLoadingStop,
                                            tvLoadingStart,
                                            taskId,
                                            exaId,
                                            loadingId,
                                            tvStopPending,
                                            tvStopShift,
                                            tvBcm
                                        )
                                        recyclerDetailFleetList.layoutManager = LinearLayoutManager(this@PengawasActivity)
                                        recyclerDetailFleetList.adapter = planUnitAdapter
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

    override fun tvStopShift(
        bindingAdapterPosition: Int,
        mData: ArrayList<TaskGroupList>,
        recyclerDetailFleetList: RecyclerView,
        ivUp: ImageView,
        ivDown: ImageView,
        tvLoadingStart: TextView,
        tvStartShift: TextView,
        tvStopPending: TextView,
        tvStopShift: TextView
    ) {
        if(isNetworkAvailable(this)) {
            val refTasks = FirebaseDatabase.getInstance().reference
                .child("TasksFleetPlan")
                .child(mData[bindingAdapterPosition].getTaskId().toString())
            refTasks.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val task = snapshot.getValue(TaskGroupList::class.java)

                        if (task?.getStatus().equals("doing")) {
                            val refTasksUnit = FirebaseDatabase.getInstance().reference
                                .child("TasksFleetPlanUnit")
                                .orderByChild("taskId")
                                .equalTo(mData[bindingAdapterPosition].getTaskId().toString())
                            refTasksUnit.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshotUnit: DataSnapshot) {
                                    if(snapshotUnit.exists()){
                                        dialogStopWithUnit(
                                            mData,
                                            bindingAdapterPosition,
                                            recyclerDetailFleetList,
                                            ivUp,
                                            ivDown,
                                            tvLoadingStart,
                                            tvStartShift,
                                            tvStopPending,
                                            tvStopShift
                                        )
                                    }
                                    else{
                                        dialogStop(
                                            mData,
                                            bindingAdapterPosition,
                                            recyclerDetailFleetList,
                                            ivUp,
                                            ivDown,
                                            tvLoadingStart,
                                            tvStartShift,
                                            tvStopShift
                                        )
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            })
                        }
                        else{
                            Helper().showToast("Status sudah selesai shift", this@PengawasActivity)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
        else{
            Toast.makeText(this, this.getString(R.string.connection_status), Toast.LENGTH_LONG).show()
        }
    }

    override fun tvHistory(mData: ArrayList<TaskGroupList>, bindingAdapterPosition: Int) {
        val dialogBuilder = MaterialAlertDialogBuilder(this, R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
        val inflater = this.layoutInflater
        dialogView = inflater.inflate(R.layout.layout_popup_history, null)
        binding.llProgressBar.preload.visibility = View.VISIBLE
        val close = dialogView.findViewById<LinearLayout>(R.id.icon_close)
        val llEmpty = dialogView.findViewById<LinearLayout>(R.id.ll_empty)

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        close.setOnClickListener {
            binding.llProgressBar.preload.visibility = View.GONE
            alertDialog.dismiss()
        }

        recycleHistoryList = dialogView.findViewById(R.id.recycler_history_list)
        recycleHistoryList.setHasFixedSize(true)
        recycleHistoryList.layoutManager = LinearLayoutManager(context)
        historyList(mData[bindingAdapterPosition].getTaskId(), llEmpty)
    }

    override fun llMenuUnit(bindingAdapterPosition: Int, mData: ArrayList<PlanUnit>, v: View) {
        @SuppressLint("InflateParams")
        val bubbleLayout = LayoutInflater.from(this).inflate(R.layout.bubble_menu_unit, null) as BubbleLayout
        val location = IntArray(2)
        v.getLocationInWindow(location)
        bubbleLayout.arrowDirection = ArrowDirection.TOP
        val popupWindow = BubblePopupHelper.create(this, bubbleLayout)

        val bubbleChangeUnit = bubbleLayout.findViewById<TextView>(R.id.bubble_change_unit)
        val bubbleChangeDriver = bubbleLayout.findViewById<TextView>(R.id.bubble_change_driver)

        bubbleChangeUnit.setOnClickListener {
//            val bottomSheet = BottomSheetUnit(bindingAdapterPosition, "Unit Status")
//            bottomSheet.show(FragmentManager.findFragmentManager(v), bottomSheet.tag)
            popupWindow.dismiss()
            formHaulerChange(mData, bindingAdapterPosition,  v, popupWindow)
        }

        bubbleChangeDriver.setOnClickListener {
            popupWindow.dismiss()
            formOperatorChange(mData, bindingAdapterPosition,  v, popupWindow)
        }

        bubbleLayout.arrowDirection = ArrowDirection.TOP
        popupWindow.showAtLocation(
            v,
            Gravity.TOP,
            location[0],
            v.height + location[1]
        )
    }

    fun formExaChange(
        mData: ArrayList<TaskGroupList>,
        bindingAdapterPosition: Int,
        v: View,
        popupWindow: PopupWindow
    ) {
        val dialogBuilder = MaterialAlertDialogBuilder(this, R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
        val inflater = this.layoutInflater
        dialogView = inflater.inflate(R.layout.layout_popup_unit, null)

        val close = dialogView.findViewById<LinearLayout>(R.id.icon_close)
        val hauler = dialogView.findViewById<TextView>(R.id.tv_hauler)
        val haulerExist = dialogView.findViewById<TextView>(R.id.tv_hauler_exist)
        addExchange = dialogView.findViewById(R.id.add_exchange)

        tvUnitCodeExchange = dialogView.findViewById(R.id.tv_unit_code_exchange)
        tvUnitCodeExchange?.text = tvUnitCodeExchange?.text.toString()

        tvUnitIdExchange = dialogView.findViewById(R.id.tv_unit_id_exchange)
        tvUnitIdExchange?.text = tvUnitIdExchange?.text.toString()

        hauler.text = HtmlCompat.fromHtml("<strong>Exchange/Pertukaran</strong>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        haulerExist.text = HtmlCompat.fromHtml("<b>"+mData[bindingAdapterPosition].getExaCode()+"</b><br/><small><i>EXCAVATOR</i></small>", HtmlCompat.FROM_HTML_MODE_LEGACY)

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)

        tvUnitCodeExchange?.setOnClickListener {
            binding.llProgressBar.preload.visibility = View.VISIBLE
            val intent = Intent(this, ProductListActivity::class.java)
            intent.putExtra("unitType", "EXCAVATOR")
            intent.putExtra("forUse", "pengawas")
            unitExchange.launch(intent)
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        addExchange?.setOnClickListener {
            addExchange?.isEnabled = false
            val tasksId = mData[bindingAdapterPosition].getTaskId()
            val operator = mData[bindingAdapterPosition].getOperatorName().toString()
            val operatorId = mData[bindingAdapterPosition].getOperatorId().toString()
            val exaCode = tvUnitCodeExchange?.text.toString()
            tvUnitCode?.text = ""
            val exaId = tvUnitIdExchange?.text.toString()
            tvUnitId?.text = ""
            tvUnit?.text = ""
            tvSopirPosition?.text = ""
            removeExaExchange(
                tasksId,
                operator,
                operatorId,
                exaCode,
                alertDialog,
                bindingAdapterPosition,
                mData,
                exaId
            )
        }

        close.setOnClickListener {
            binding.llProgressBar.preload.visibility = View.GONE
            alertDialog.dismiss()
            popupWindow.dismiss()
        }
    }

    fun formHaulerChange(
        mData: ArrayList<PlanUnit>,
        bindingAdapterPosition: Int,
        v: View,
        popupWindow: PopupWindow
    ) {
        val dialogBuilder = MaterialAlertDialogBuilder(this, R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
        val inflater = this.layoutInflater
        dialogView = inflater.inflate(R.layout.layout_popup_unit, null)

        val close = dialogView.findViewById<LinearLayout>(R.id.icon_close)
        val hauler = dialogView.findViewById<TextView>(R.id.tv_hauler)
        val haulerExist = dialogView.findViewById<TextView>(R.id.tv_hauler_exist)
        addExchange = dialogView.findViewById(R.id.add_exchange)

        tvUnitCodeExchange = dialogView.findViewById(R.id.tv_unit_code_exchange)
        tvUnitCodeExchange?.text = tvUnitCodeExchange?.text.toString()

        tvUnitIdExchange = dialogView.findViewById(R.id.tv_unit_id_exchange)
        tvUnitIdExchange?.text = tvUnitIdExchange?.text.toString()

        hauler.text = HtmlCompat.fromHtml("<strong>Exchange/Pertukaran</strong>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        haulerExist.text = HtmlCompat.fromHtml("<b>"+mData[bindingAdapterPosition].getUnitCode()+"</b><br/><small><i>Hauler</i></small>", HtmlCompat.FROM_HTML_MODE_LEGACY)

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)

        tvUnitCodeExchange?.setOnClickListener {
            binding.llProgressBar.preload.visibility = View.VISIBLE
            val intent = Intent(this, ProductListActivity::class.java)
            intent.putExtra("unitType", "DUMP TRUCK")
            intent.putExtra("forUse", "pengawas")
            unitExchange.launch(intent)
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        addExchange?.setOnClickListener {
            addExchange?.isEnabled = false
            val tasksId = mData[bindingAdapterPosition].getTaskId()
            val sopir = mData[bindingAdapterPosition].getSopir().toString()
            val sopirId = mData[bindingAdapterPosition].getSopirId().toString()
            val unitCode = tvUnitCodeExchange?.text.toString()
            tvUnitCode?.text = ""
            val unitId = tvUnitIdExchange?.text.toString()
            tvUnitId?.text = ""
            tvUnit?.text = ""
            tvSopirPosition?.text = ""
            removePlanUnitExchange(
                tasksId,
                sopir,
                sopirId,
                unitCode,
                alertDialog,
                bindingAdapterPosition,
                mData,
                unitId
            )
        }

        close.setOnClickListener {
            binding.llProgressBar.preload.visibility = View.GONE
            alertDialog.dismiss()
            popupWindow.dismiss()
        }
    }

    fun formOperatorChange(
        mData: ArrayList<PlanUnit>,
        bindingAdapterPosition: Int,
        v: View,
        popupWindow: PopupWindow
    ) {
        val dialogBuilder = MaterialAlertDialogBuilder(this, R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
        val inflater = this.layoutInflater
        dialogView = inflater.inflate(R.layout.layout_popup_operator, null)

        val close = dialogView.findViewById<LinearLayout>(R.id.icon_close)
        val operator = dialogView.findViewById<TextView>(R.id.tv_operator)
        val operatorExist = dialogView.findViewById<TextView>(R.id.tv_operator_exist)
        addExchange = dialogView.findViewById(R.id.add_exchange)

        tvOperatorNameExchange = dialogView.findViewById(R.id.tv_operator_name_exchange)
        tvOperatorNameExchange?.text = tvOperatorNameExchange?.text.toString()

        tvOperatorIdExchange = dialogView.findViewById(R.id.tv_operator_id_exchange)
        tvOperatorIdExchange?.text = tvOperatorIdExchange?.text.toString()

        operator.text = HtmlCompat.fromHtml("<strong>Exchange/Pertukaran</strong>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        operatorExist.text = HtmlCompat.fromHtml("<b>"+mData[bindingAdapterPosition].getSopir()+"</b>", HtmlCompat.FROM_HTML_MODE_LEGACY)

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)

        tvOperatorNameExchange?.setOnClickListener {
            binding.llProgressBar.preload.visibility = View.VISIBLE
            val intent = Intent(this, ContactListActivity::class.java)
            intent.putExtra("position", "Sopir")
            intent.putExtra("forUse", "pengawas")
            operatorExchange.launch(intent)
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        addExchange?.setOnClickListener {
            addExchange?.isEnabled = false
            val tasksId = mData[bindingAdapterPosition].getTaskId()
            val sopir = tvOperatorNameExchange?.text.toString()
            val sopirId = tvOperatorIdExchange?.text.toString()
            val unitCode = mData[bindingAdapterPosition].getUnitCode().toString()
            tvUnitCode?.text = ""
            val unitId = mData[bindingAdapterPosition].getTaskUnitId().toString()
            tvUnitId?.text = ""
            tvUnit?.text = ""
            tvSopirPosition?.text = ""
            removeOperatorExchange(
                tasksId,
                sopir,
                sopirId,
                unitCode,
                alertDialog,
                bindingAdapterPosition,
                mData,
                unitId
            )
        }

        close.setOnClickListener {
            binding.llProgressBar.preload.visibility = View.GONE
            alertDialog.dismiss()
            popupWindow.dismiss()
        }
    }

    fun addingTaskUnitExchange(
        tasksId: String?,
        operator: String,
        operatorId: String,
        exaCode: String,
        alertDialog: AlertDialog,
        exaId: String,
        newTaskId: String
    ) {
        if(tasksId!!.isNotEmpty()
            && operator.isNotEmpty()
            && operatorId.isNotEmpty()
            && exaCode.isNotEmpty()
        ) {
            val refGetUnit = FirebaseDatabase.getInstance().reference
            refGetUnit.child("TasksFleetPlanUnit")
                .orderByChild("taskId")
                .equalTo(tasksId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (postSnapshot in snapshot.children) {
                                val refTaskUnit2 = FirebaseDatabase.getInstance().reference
                                val planUnitDataList = postSnapshot.getValue(PlanUnit::class.java)
                                var id = planUnitDataList?.getUid().toString()
                                val taskHashMap2 = HashMap<String, Any>()
                                taskHashMap2["uid"] = id
                                taskHashMap2["taskUnitId"] = planUnitDataList?.getTaskUnitId().toString()
                                taskHashMap2["taskId"] = tasksId
                                taskHashMap2["sopir"] = planUnitDataList?.getSopir().toString()
                                taskHashMap2["sopirId"] = planUnitDataList?.getSopirId().toString()
                                taskHashMap2["unitCode"] = planUnitDataList?.getUnitCode().toString()
                                taskHashMap2["status"] = "todo"

                                databaseReference =
                                    refTaskUnit2.child("TasksFleetPlanUnit").child(id)
                                databaseReference.updateChildren(taskHashMap2)
                                    .addOnCompleteListener {}
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }
        else{
            binding.llProgressBar.preload.visibility = View.GONE
            Helper().showToast("All field is required!", this)
        }
    }

    fun addingUnitExchange(
        tasksId: String?,
        sopir: String,
        sopirId: String,
        unitCode: String,
        alertDialog: AlertDialog,
        unitId: String
    ) {
        if(tasksId!!.isNotEmpty()
            && sopir.isNotEmpty()
            && sopirId.isNotEmpty()
            && unitCode.isNotEmpty()
        ) {
            val refGetUnit = FirebaseDatabase.getInstance().reference
            refGetUnit.child("TasksFleetPlanUnit")
                .orderByChild("taskUnitId")
                .equalTo(unitId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val refTaskUnit2 = FirebaseDatabase.getInstance().reference
                            var id = refTaskUnit2.push().key.toString()
                            for (postSnapshot in snapshot.children) {
                                val planUnitDataList = postSnapshot.getValue(PlanUnit::class.java)
                                if (sopirId == planUnitDataList?.getSopirId()
                                    && tasksId == planUnitDataList.getTaskId()
                                ) {
                                    id = planUnitDataList.getUid().toString()
                                }
                            }
                            val taskHashMap2 = HashMap<String, Any>()
                            taskHashMap2["uid"] = id
                            taskHashMap2["taskUnitId"] = unitId
                            taskHashMap2["taskId"] = tasksId
                            taskHashMap2["sopir"] = sopir
                            taskHashMap2["sopirId"] = sopirId
                            taskHashMap2["unitCode"] = unitCode
                            taskHashMap2["status"] = "todo"

                            databaseReference =
                                refTaskUnit2.child("TasksFleetPlanUnit").child(id)
                            databaseReference.updateChildren(taskHashMap2)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val productRefrence =
                                            FirebaseDatabase.getInstance().reference
                                                .child("Units")
                                                .child(unitId)

                                        productRefrence.addListenerForSingleValueEvent(
                                            object : ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    if (snapshot.exists()) {
                                                        val unit: UnitList? =
                                                            snapshot.getValue(UnitList::class.java)

                                                        val refUnit =
                                                            FirebaseDatabase.getInstance().reference
                                                        val unitHashMap =
                                                            HashMap<String, Any>()
                                                        unitHashMap["uid"] =
                                                            unit?.getUid().toString()
                                                        unitHashMap["unitId"] = unitId
                                                        unitHashMap["unitCode"] =
                                                            unit?.getUnitCode().toString()
                                                        unitHashMap["unitType"] =
                                                            unit?.getUnitType().toString()
                                                        unitHashMap["merk"] =
                                                            unit?.getMerk().toString()
                                                        unitHashMap["yom"] =
                                                            unit?.getYom().toString()
                                                        unitHashMap["status"] = "USED"
                                                        val databaseReference =
                                                            refUnit.child("Units")
                                                                .child(unitId)
                                                        databaseReference.updateChildren(
                                                            unitHashMap
                                                        )
                                                            .addOnCompleteListener { task ->
                                                                if (task.isSuccessful) {
                                                                    binding.llProgressBar.preload.visibility =
                                                                        View.GONE
                                                                    alertDialog.dismiss()
                                                                } else {
                                                                    binding.llProgressBar.preload.visibility =
                                                                        View.GONE
                                                                    Helper().showToast(
                                                                        "Data Product is Something Wrong!!!",
                                                                        this@PengawasActivity
                                                                    )
                                                                }
                                                            }
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {

                                                }
                                            })
                                    } else {
                                        binding.llProgressBar.preload.visibility = View.GONE
                                        Helper().showToast(
                                            "Data Company is Something Wrong!!!",
                                            this@PengawasActivity
                                        )
                                    }
                                }
                        }
                        else{
                            val refTaskUnit = FirebaseDatabase.getInstance().reference
                            val taskUnitId = refTaskUnit.push().key.toString()

                            val taskHashMap = HashMap<String, Any>()
                            taskHashMap["uid"] = taskUnitId
                            taskHashMap["taskUnitId"] = unitId
                            taskHashMap["taskId"] = tasksId
                            taskHashMap["sopir"] = sopir
                            taskHashMap["sopirId"] = sopirId
                            taskHashMap["unitCode"] = unitCode
                            taskHashMap["status"] = "todo"

                            databaseReference = refTaskUnit.child("TasksFleetPlanUnit").child(taskUnitId)
                            databaseReference.updateChildren(taskHashMap)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val productRefrence = FirebaseDatabase.getInstance().reference
                                            .child("Units")
                                            .child(unitId)

                                        productRefrence.addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (snapshot.exists())
                                                {
                                                    val unit: UnitList? = snapshot.getValue(UnitList::class.java)

                                                    val refUnit = FirebaseDatabase.getInstance().reference
                                                    val unitHashMap = HashMap<String, Any>()
                                                    unitHashMap["uid"] = unit?.getUid().toString()
                                                    unitHashMap["unitId"] = unitId
                                                    unitHashMap["unitCode"] = unit?.getUnitCode().toString()
                                                    unitHashMap["unitType"] = unit?.getUnitType().toString()
                                                    unitHashMap["merk"] = unit?.getMerk().toString()
                                                    unitHashMap["yom"] =  unit?.getYom().toString()
                                                    unitHashMap["status"] =  "USED"
                                                    val databaseReference = refUnit.child("Units").child(unitId)
                                                    databaseReference.updateChildren(unitHashMap)
                                                        .addOnCompleteListener { task ->
                                                            if (task.isSuccessful) {
                                                                binding.llProgressBar.preload.visibility = View.GONE
                                                                alertDialog.dismiss()
                                                            }
                                                            else{
                                                                binding.llProgressBar.preload.visibility = View.GONE
                                                                Helper().showToast("Data Product is Something Wrong!!!", this@PengawasActivity)
                                                            }
                                                        }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {

                                            }
                                        })
                                    }
                                    else{
                                        binding.llProgressBar.preload.visibility = View.GONE
                                        Helper().showToast("Data Company is Something Wrong!!!", this@PengawasActivity)
                                    }
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }
        else{
            binding.llProgressBar.preload.visibility = View.GONE
            Helper().showToast("All field is required!", this)
        }
    }

    fun addingOperatorHaulerExchange(
        tasksId: String?,
        sopir: String,
        sopirId: String,
        unitCode: String,
        alertDialog: AlertDialog,
        unitId: String
    ) {
        if(tasksId!!.isNotEmpty()
            && sopir.isNotEmpty()
            && sopirId.isNotEmpty()
            && unitCode.isNotEmpty()
        ) {
            val refGetUnit = FirebaseDatabase.getInstance().reference
            refGetUnit.child("TasksFleetPlanUnit")
                .orderByChild("sopirId")
                .equalTo(sopirId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val refTaskUnit2 = FirebaseDatabase.getInstance().reference
                            var id = refTaskUnit2.push().key.toString()
                            for (postSnapshot in snapshot.children) {
                                val planUnitDataList = postSnapshot.getValue(PlanUnit::class.java)
                                if (unitId == planUnitDataList?.getTaskUnitId()
                                    && tasksId == planUnitDataList.getTaskId()
                                    ) {
                                    id = planUnitDataList.getUid().toString()
                                }
                            }
                            val taskHashMap2 = HashMap<String, Any>()
                            taskHashMap2["uid"] = id
                            taskHashMap2["taskUnitId"] = unitId
                            taskHashMap2["taskId"] = tasksId
                            taskHashMap2["sopir"] = sopir
                            taskHashMap2["sopirId"] = sopirId
                            taskHashMap2["unitCode"] = unitCode
                            taskHashMap2["status"] = "todo"

                            databaseReference =
                                refTaskUnit2.child("TasksFleetPlanUnit").child(id)
                            databaseReference.updateChildren(taskHashMap2)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val productRefrence =
                                            FirebaseDatabase.getInstance().reference
                                                .child("Units")
                                                .child(unitId)

                                        productRefrence.addListenerForSingleValueEvent(
                                            object : ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    if (snapshot.exists()) {
                                                        val unit: UnitList? =
                                                            snapshot.getValue(UnitList::class.java)

                                                        val refUnit =
                                                            FirebaseDatabase.getInstance().reference
                                                        val unitHashMap =
                                                            HashMap<String, Any>()
                                                        unitHashMap["uid"] =
                                                            unit?.getUid().toString()
                                                        unitHashMap["unitId"] = unitId
                                                        unitHashMap["unitCode"] =
                                                            unit?.getUnitCode().toString()
                                                        unitHashMap["unitType"] =
                                                            unit?.getUnitType().toString()
                                                        unitHashMap["merk"] =
                                                            unit?.getMerk().toString()
                                                        unitHashMap["yom"] =
                                                            unit?.getYom().toString()
                                                        unitHashMap["status"] = "USED"
                                                        val databaseReference =
                                                            refUnit.child("Units")
                                                                .child(unitId)
                                                        databaseReference.updateChildren(
                                                            unitHashMap
                                                        )
                                                            .addOnCompleteListener { task ->
                                                                if (task.isSuccessful) {
                                                                    binding.llProgressBar.preload.visibility =
                                                                        View.GONE
                                                                    alertDialog.dismiss()
                                                                } else {
                                                                    binding.llProgressBar.preload.visibility =
                                                                        View.GONE
                                                                    Helper().showToast(
                                                                        "Data Product is Something Wrong!!!",
                                                                        this@PengawasActivity
                                                                    )
                                                                }
                                                            }
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {

                                                }
                                            })
                                    } else {
                                        binding.llProgressBar.preload.visibility = View.GONE
                                        Helper().showToast(
                                            "Data Company is Something Wrong!!!",
                                            this@PengawasActivity
                                        )
                                    }
                                }
                        }
                        else{
                            val refTaskUnit2 = FirebaseDatabase.getInstance().reference
                            val taskUnitId2 = refTaskUnit2.push().key.toString()

                            val taskHashMap2 = HashMap<String, Any>()
                            taskHashMap2["uid"] = taskUnitId2
                            taskHashMap2["taskUnitId"] = unitId
                            taskHashMap2["taskId"] = tasksId
                            taskHashMap2["sopir"] = sopir
                            taskHashMap2["sopirId"] = sopirId
                            taskHashMap2["unitCode"] = unitCode
                            taskHashMap2["status"] = "todo"

                            databaseReference = refTaskUnit2.child("TasksFleetPlanUnit").child(taskUnitId2)
                            databaseReference.updateChildren(taskHashMap2)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val productRefrence = FirebaseDatabase.getInstance().reference
                                            .child("Units")
                                            .child(unitId)

                                        productRefrence.addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (snapshot.exists())
                                                {
                                                    val unit: UnitList? = snapshot.getValue(UnitList::class.java)

                                                    val refUnit = FirebaseDatabase.getInstance().reference
                                                    val unitHashMap = HashMap<String, Any>()
                                                    unitHashMap["uid"] = unit?.getUid().toString()
                                                    unitHashMap["unitId"] = unitId
                                                    unitHashMap["unitCode"] = unit?.getUnitCode().toString()
                                                    unitHashMap["unitType"] = unit?.getUnitType().toString()
                                                    unitHashMap["merk"] = unit?.getMerk().toString()
                                                    unitHashMap["yom"] =  unit?.getYom().toString()
                                                    unitHashMap["status"] =  "USED"
                                                    val databaseReference = refUnit.child("Units").child(unitId)
                                                    databaseReference.updateChildren(unitHashMap)
                                                        .addOnCompleteListener { task ->
                                                            if (task.isSuccessful) {
                                                                binding.llProgressBar.preload.visibility = View.GONE
                                                                alertDialog.dismiss()
                                                            }
                                                            else{
                                                                binding.llProgressBar.preload.visibility = View.GONE
                                                                Helper().showToast("Data Product is Something Wrong!!!", this@PengawasActivity)
                                                            }
                                                        }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {

                                            }
                                        })
                                    }
                                    else{
                                        binding.llProgressBar.preload.visibility = View.GONE
                                        Helper().showToast("Data Company is Something Wrong!!!", this@PengawasActivity)
                                    }
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }
        else{
            binding.llProgressBar.preload.visibility = View.GONE
            Helper().showToast("All field is required!", this)
        }
    }

    override fun tvUnit(tvSopir: TextView, tvUnit: TextView, llSosial2: LinearLayout) {
        if (!tvSopir.isEnabled) {
            tvSopir.visibility = View.VISIBLE
            tvSopir.isEnabled = true
            llSosial2.visibility = View.VISIBLE
            tvUnit.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_dump_truck, 0, R.drawable.icon_up_small, 0)
        }
        else {
            tvSopir.visibility = View.GONE
            tvSopir.isEnabled = false
            llSosial2.visibility = View.GONE
            tvUnit.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_dump_truck, 0, R.drawable.icon_down_small, 0)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun showFormRemark(
        bindingAdapterPosition: Int,
        mData: java.util.ArrayList<Ritase>,
        v: View?,
        tvRemark: TextView,
        status: String?
    ) {
        binding.llProgressBar.preload.visibility = View.VISIBLE

        val refShareContact = FirebaseDatabase.getInstance().reference
        refShareContact.child("Remark")
            .child(mData[bindingAdapterPosition].getRitaseId().toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        dataRemark = snapshot.child("remark").value.toString()
                        formRemark(mData, bindingAdapterPosition, v, tvRemark, "Update Remark", status)
                    }
                    else {
                        formRemark(mData, bindingAdapterPosition, v, tvRemark, "Save Remark", status)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    fun formRemark(
        mData: java.util.ArrayList<Ritase>,
        bindingAdapterPosition: Int,
        v: View?,
        tvRemark: TextView,
        s: String,
        status: String?
    ) {
        val dialogBuilder = MaterialAlertDialogBuilder(this@PengawasActivity, R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
        val inflater = LayoutInflater.from(this@PengawasActivity)
        dialogView = inflater.inflate(R.layout.layout_form_remark, null)

        val close = dialogView.findViewById<LinearLayout>(R.id.icon_close)
        val hauler = dialogView.findViewById<TextView>(R.id.tv_hauler)
        val time = dialogView.findViewById<TextView>(R.id.tv_time)
        val remark = dialogView.findViewById<EditText>(R.id.et_remark)
        if (status == "done") {
            remark.isEnabled = false
        }
        val addRemark = dialogView.findViewById<LinearLayout>(R.id.add_remark)
        if (status == "done") {
            addRemark.visibility = View.GONE
        }
        val txtFacebook = dialogView.findViewById<TextView>(R.id.txt_facebook)
        txtFacebook.text = s

        hauler.text = HtmlCompat.fromHtml("<b>"+mData[bindingAdapterPosition].getUnitCode()+"</b><br/><small><i>Hauler</i></small>", HtmlCompat.FROM_HTML_MODE_LEGACY)

        val s = mData[bindingAdapterPosition].getTimeStamp()?.split(" ")?.get(1)
        time.text = s?.split(":")?.get(0)+":"+s?.split(":")?.get(1)

        if (tvRemark.text.toString() != "....") {
            remark.setText(dataRemark)
        }
        else{
            remark.setHint("Tambahkan remark / keterangan disini...")
        }

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        addRemark.setOnClickListener {
            if(remark.text.isNotEmpty()) {
                firebaseAuth = FirebaseAuth.getInstance()
                val refRemark = FirebaseDatabase.getInstance().reference
                val remarkId = mData[bindingAdapterPosition].getRitaseId()

                val remarkHashMap = HashMap<String, Any>()
                remarkHashMap["uid"] = mData[bindingAdapterPosition].getUid().toString()
                remarkHashMap["remarkId"] = remarkId.toString()
                remarkHashMap["remark"] = remark.text.toString()
                remarkHashMap["createBy"] = firebaseAuth.currentUser!!.uid
                remarkHashMap["updateBy"] = ""
                remarkHashMap["createDate"] = DateHelper().todayTime()
                remarkHashMap["updateDate"] = DateHelper().todayTime()
                databaseReference = refRemark.child("Remark").child(remarkId.toString())
                databaseReference.setValue(remarkHashMap)
                    .addOnCompleteListener {
                        tvRemark.text = HtmlCompat.fromHtml("<small><i>"+ remark.text
                            .split(" ")[0] +"</i></small>...", HtmlCompat.FROM_HTML_MODE_LEGACY)
                        tvRemark.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_edit_24, 0)
                        alertDialog.dismiss()
                        binding.llProgressBar.preload.visibility = View.GONE
                    }
            }
        }

        close.setOnClickListener {
            alertDialog.dismiss()
            binding.llProgressBar.preload.visibility = View.GONE
        }
    }

    private fun showFleetPlan() {
        val refTasks = FirebaseDatabase.getInstance().reference
            .child("TasksFleetPlan")
            .orderByChild("date")
            .equalTo(binding.tvDate.text.toString())
        taskPengawasViewModel.show(refTasks, firebaseUser!!.uid)
    }

    @SuppressLint("ClickableViewAccessibility", "SimpleDateFormat", "InflateParams", "SetTextI18n")
    private fun showFormDialog(
        bindingAdapterPosition: Int,
        mData: ArrayList<TaskGroupList>,
        ivDown: ImageView
    ) {
        val dialogBuilder = MaterialAlertDialogBuilder(this, R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
        val inflater = this.layoutInflater
        dialogView = inflater.inflate(R.layout.layout_form_unit, null)

        val tvExa = dialogView.findViewById<TextView>(R.id.tv_exa)
        tvExa.text = mData[bindingAdapterPosition].getExaCode()

        tvSopirPosition = dialogView.findViewById(R.id.tv_sopir_position)
        tvSopir = dialogView.findViewById(R.id.tv_sopir)
        tvSopirId = dialogView.findViewById(R.id.tv_sopir_id)
        tvUnitCode = dialogView.findViewById(R.id.tv_unit_code)
        tvUnit = dialogView.findViewById(R.id.tv_unit)
        tvUnitId = dialogView.findViewById(R.id.tv_unit_id)
        val close = dialogView.findViewById<LinearLayout>(R.id.icon_close)
        val addUnit = dialogView.findViewById<LinearLayout>(R.id.add_unit)
        llEmpty = dialogView.findViewById(R.id.ll_empty)

        val tvPengawas = dialogView.findViewById<TextView>(R.id.tv_pengawas)
        tvPengawas.text = mData[bindingAdapterPosition].getPengawasName()
        val tvPengawasLabel = dialogView.findViewById<TextView>(R.id.tv_pengawas_label)
        tvPengawasLabel.text = HtmlCompat.fromHtml("<small><i>Pengawas</i></small>", HtmlCompat.FROM_HTML_MODE_LEGACY)

        val tvOperator = dialogView.findViewById<TextView>(R.id.tv_operator)
        tvOperator.text = mData[bindingAdapterPosition].getOperatorName()
        val tvOperatorLabel = dialogView.findViewById<TextView>(R.id.tv_operator_label)
        tvOperatorLabel.text = HtmlCompat.fromHtml("<small><i>Operator</i></small>", HtmlCompat.FROM_HTML_MODE_LEGACY)

        val tvLocation = dialogView.findViewById<TextView>(R.id.tv_location)
        tvLocation.text = mData[bindingAdapterPosition].getLocationName()+" - "+mData[bindingAdapterPosition].getGalianName()
        val tvLocationLabel = dialogView.findViewById<TextView>(R.id.tv_location_label)
        tvLocationLabel.text = HtmlCompat.fromHtml("<small><i>Lokasi Galian</i></small>", HtmlCompat.FROM_HTML_MODE_LEGACY)

        val tvTimbunan = dialogView.findViewById<TextView>(R.id.tv_timbunan)
        tvTimbunan.text = mData[bindingAdapterPosition].getTimbunanName()
        val tvTimbunanLabel = dialogView.findViewById<TextView>(R.id.tv_timbunan_label)
        tvTimbunanLabel.text = HtmlCompat.fromHtml("<small><i>Lokasi Timbunan</i></small>", HtmlCompat.FROM_HTML_MODE_LEGACY)

        val tvCalender = dialogView.findViewById<TextView>(R.id.tv_calender)
        tvCalender.text = mData[bindingAdapterPosition].getDate()

        val tvJarak = dialogView.findViewById<TextView>(R.id.tv_jarak)
        tvJarak.text = HtmlCompat.fromHtml("<i>Jarak: </i>"+mData[bindingAdapterPosition].getJarak()+"km", HtmlCompat.FROM_HTML_MODE_LEGACY)

        val tvTarget = dialogView.findViewById<TextView>(R.id.tv_target)
        tvTarget.text = HtmlCompat.fromHtml("<i>Target/Plan: </i>"+mData[bindingAdapterPosition].getPlan(), HtmlCompat.FROM_HTML_MODE_LEGACY)

        val tvOpenForm = dialogView.findViewById<TextView>(R.id.tv_open_form)
        val tvCloseForm = dialogView.findViewById<TextView>(R.id.tv_close_form)
        tvOpenForm.setOnClickListener {
            tvOpenForm.visibility = View.GONE
            tvCloseForm.visibility = View.VISIBLE
            dialogView.findViewById<LinearLayout>(R.id.detail_fleet_plan).visibility = View.GONE
            dialogView.findViewById<LinearLayout>(R.id.form_sopir).visibility = View.VISIBLE
        }
        tvCloseForm.setOnClickListener {
            tvOpenForm.visibility = View.VISIBLE
            tvCloseForm.visibility = View.GONE
            dialogView.findViewById<LinearLayout>(R.id.detail_fleet_plan).visibility = View.VISIBLE
            dialogView.findViewById<LinearLayout>(R.id.form_sopir).visibility = View.GONE
        }

        tvSopir?.setOnClickListener {
            val intent = Intent(this, ContactListActivity::class.java)
            intent.putExtra("position", "Sopir")
            intent.putExtra("forUse", "pengawas")
            sopirResult.launch(intent)
        }

        tvUnit?.setOnClickListener {
            binding.llProgressBar.preload.visibility = View.VISIBLE
            val intent = Intent(this, ProductListActivity::class.java)
            intent.putExtra("unitType", "DUMP TRUCK")
            intent.putExtra("forUse", "pengawas")
            unitResult.launch(intent)
        }

        tvExa.setOnClickListener {
            val intent = Intent(this, EditTaskActivity::class.java)
            intent.putExtra("uid", mData[bindingAdapterPosition].getUid())
            intent.putExtra("taskId", mData[bindingAdapterPosition].getTaskId())
            NavigationHelper().navigateToActivityCallback(this, intent)
        }

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        close.setOnClickListener {
            binding.llProgressBar.preload.visibility = View.GONE
            alertDialog.dismiss()
            ivDown.isEnabled = true
        }
        binding.llProgressBar.preload.visibility = View.GONE
        addUnit.setOnClickListener {
            val tasksId = mData[bindingAdapterPosition].getTaskId()
            val sopir = tvSopir?.text.toString()
            tvSopir?.text = ""
            val sopirId = tvSopirId?.text.toString()
            tvSopirId?.text = ""
            val unitCode = tvUnitCode?.text.toString()
            tvUnitCode?.text = ""
            val unitId = tvUnitId?.text.toString()
            tvUnitId?.text = ""
            tvUnit?.text = ""
            tvSopirPosition?.text = ""
            addingUnit(
                tasksId,
                sopir,
                sopirId,
                unitCode,
                dialogView,
                bindingAdapterPosition,
                mData,
                llEmpty!!,
                unitId,
                ivDown
            )
        }

        recyclePlanUnitList = dialogView.findViewById(R.id.recycler_plan_unit_list)
        recyclePlanUnitList.setHasFixedSize(true)
        recyclePlanUnitList.layoutManager = LinearLayoutManager(context)
        planUnit(mData[bindingAdapterPosition].getTaskId(), llEmpty!!)
    }

    @SuppressLint("SimpleDateFormat")
    private fun showLostTimeDialog(
        mData: ArrayList<TaskGroupList>,
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
        status: String,
        tvBcm: TextView,
        tvHistory: TextView
    ) {
        val dialogBuilder = MaterialAlertDialogBuilder(this, R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
        val inflater = this.layoutInflater
        dialogView = inflater.inflate(R.layout.layout_form_lost_time, null)

        val close = dialogView.findViewById<LinearLayout>(R.id.icon_close)
        val etLostTime = dialogView.findViewById<EditText>(R.id.et_lost_time)
        val btnTimeStart = dialogView.findViewById<ImageView>(R.id.iv_time_start)
        val timeStart = dialogView.findViewById<TextView>(R.id.et_time_start)
        val btnTimeEnd = dialogView.findViewById<ImageView>(R.id.iv_time_end)
        val timeEnd = dialogView.findViewById<TextView>(R.id.et_time_end)
        val addLostTime = dialogView.findViewById<LinearLayout>(R.id.add_lost_time)
        val tvLabelTime = dialogView.findViewById<TextView>(R.id.tv_label_time)

        etLostTime.setOnClickListener {
            getItemLostTimeActivity()
        }

        btnTimeStart.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                timeStart.text = SimpleDateFormat("HH:mm").format(cal.time)
            }
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        btnTimeEnd.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                timeEnd.text = SimpleDateFormat("HH:mm").format(cal.time)
            }
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)

        val alertDialog = dialogBuilder.create()

        addLostTime.setOnClickListener {
            if (
                etLostTime.text.isNotEmpty()
                && timeStart.text.isNotEmpty()
//                && timeEnd.text.isNotEmpty()
            ) {
                addingLostTime(
                    mData,
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
                    status,
                    etLostTime,
                    timeStart,
                    timeEnd,
                    alertDialog,
                    tvBcm,
                    tvHistory
                )
            }
            else{
                Toast.makeText(this, "Semua field wajib diisi!", Toast.LENGTH_LONG).show()
            }
        }
        alertDialog.show()

        close.setOnClickListener {
            alertDialog.dismiss()
            if (status == "stop") {
                binding.llProgressBar.preload.visibility = View.GONE
                tvLoadingStop.visibility = View.VISIBLE
                tvLoadingStop.isEnabled = true
                tvLoadingStart.visibility = View.GONE
                tvStopShift.isEnabled = false
                tvStopShift.visibility = View.GONE
                tvStopPending.visibility = View.VISIBLE
            }
            if (status == "start") {
                binding.llProgressBar.preload.visibility = View.GONE
                tvLoadingStart.visibility = View.VISIBLE
                tvLoadingStart.isEnabled = true
                tvLoadingStop.visibility = View.GONE
                tvStopShift.isEnabled = true
                tvStopShift.visibility = View.VISIBLE
                tvStopPending.visibility = View.GONE
            }
        }
    }

    private fun addingLostTime(
        mData: ArrayList<TaskGroupList>,
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
        status: String,
        etLostTime: EditText,
        timeStart: TextView,
        timeEnd: TextView,
        alertDialog: AlertDialog,
        tvBcm: TextView,
        tvHistory: TextView
    ) {
        val refTime = FirebaseDatabase.getInstance().reference
        val lostTimeId = refTime.push().key
        val lostimeHashMap = HashMap<String, Any>()
        lostimeHashMap["taskId"] = mData[bindingAdapterPosition].getTaskId().toString()
        lostimeHashMap["timestamp"] = Helper().dateTimeNow().split(" ")[0] +" "+timeStart.text.toString()+":00"
        lostimeHashMap["remark"] = etLostTime.text.toString()
        lostimeHashMap["createBy"] = firebaseAuth.currentUser!!.uid
        lostimeHashMap["updateBy"] = ""
        lostimeHashMap["createDate"] =  DateHelper().todayTime()
        lostimeHashMap["updateDate"] =  ""
        refTime.child("FleetHistoryTime")
            .child(mData[bindingAdapterPosition].getTaskId().toString())
            .child(lostTimeId!!)
            .setValue(lostimeHashMap).addOnCompleteListener { task2 ->
                if (task2.isSuccessful) {
                    tvHistory.text = HtmlCompat.fromHtml("<i><u>"+etLostTime.text.toString()+"</u></i>", HtmlCompat.FROM_HTML_MODE_LEGACY)
                    alertDialog.dismiss()
                    timeStampUpdate(
                        mData,
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
                        status,
                        tvBcm
                    )
                }
            }
    }

    private fun getItemLostTimeActivity() {
        val intent = Intent(this, LostTimeListActivity::class.java)
        intent.putExtra("position", "Label History Time")
        lostTimeResult.launch(intent)
    }

    private fun addingUnit(
        tasksId: String?,
        sopir: String,
        sopirId: String,
        unitCode: String,
        dialogView: View,
        bindingAdapterPosition: Int,
        mData: ArrayList<TaskGroupList>,
        llEmpty: LinearLayout,
        unitId: String,
        ivDown: ImageView
    ) {
        if(tasksId!!.isNotEmpty()
            && sopir.isNotEmpty()
            && sopirId.isNotEmpty()
            && unitCode.isNotEmpty()
        ) {
            val refGetUnit = FirebaseDatabase.getInstance().reference
            refGetUnit.child("TasksFleetPlanUnit")
                .orderByChild("taskUnitId")
                .equalTo(unitId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val refTaskUnit = FirebaseDatabase.getInstance().reference
                            var taskUnitId = refTaskUnit.push().key.toString()
                            for (postSnapshot in snapshot.children) {
                                val planUnitDataList = postSnapshot.getValue(PlanUnit::class.java)
                                if (sopirId == planUnitDataList?.getSopirId()
                                    && tasksId == planUnitDataList.getTaskId()
                                ) {
                                    taskUnitId = planUnitDataList.getUid().toString()
                                }
                            }
                            val taskHashMap = HashMap<String, Any>()
                            taskHashMap["uid"] = taskUnitId
                            taskHashMap["taskUnitId"] = unitId
                            taskHashMap["taskId"] = tasksId
                            taskHashMap["sopir"] = sopir
                            taskHashMap["sopirId"] = sopirId
                            taskHashMap["unitCode"] = unitCode
                            taskHashMap["status"] = "todo"

                            databaseReference = refTaskUnit.child("TasksFleetPlanUnit").child(taskUnitId)
                            databaseReference.updateChildren(taskHashMap)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val productRefrence = FirebaseDatabase.getInstance().reference
                                            .child("Units")
                                            .child(unitId)

                                        productRefrence.addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (snapshot.exists())
                                                {
                                                    val unit: UnitList? = snapshot.getValue(UnitList::class.java)

                                                    val refUnit = FirebaseDatabase.getInstance().reference
                                                    val unitHashMap = HashMap<String, Any>()
                                                    unitHashMap["uid"] = unit?.getUid().toString()
                                                    unitHashMap["unitId"] = unitId
                                                    unitHashMap["unitCode"] = unit?.getUnitCode().toString()
                                                    unitHashMap["unitType"] = unit?.getUnitType().toString()
                                                    unitHashMap["merk"] = unit?.getMerk().toString()
                                                    unitHashMap["yom"] =  unit?.getYom().toString()
                                                    unitHashMap["status"] =  "USED"
                                                    val databaseReference = refUnit.child("Units").child(unitId)
                                                    databaseReference.updateChildren(unitHashMap)
                                                        .addOnCompleteListener { task ->
                                                            if (task.isSuccessful) {
                                                                Helper().showToast("Adding data is Successful!", this@PengawasActivity)
                                                                ivDown.isEnabled = true
                                                                recyclePlanUnitList = dialogView.findViewById(R.id.recycler_plan_unit_list)
                                                                recyclePlanUnitList.setHasFixedSize(true)
                                                                recyclePlanUnitList.layoutManager = LinearLayoutManager(context)
                                                                dialogView.findViewById<TextView>(R.id.tv_open_form).visibility = View.VISIBLE
                                                                dialogView.findViewById<TextView>(R.id.tv_close_form).visibility = View.GONE
                                                                dialogView.findViewById<LinearLayout>(R.id.detail_fleet_plan).visibility = View.VISIBLE
                                                                dialogView.findViewById<LinearLayout>(R.id.form_sopir).visibility = View.GONE
                                                                planUnit(mData[bindingAdapterPosition].getTaskId(), llEmpty)
                                                            }
                                                            else{
                                                                binding.llProgressBar.preload.visibility = View.GONE
                                                                Helper().showToast("Data Product is Something Wrong!!!", this@PengawasActivity)
                                                            }
                                                        }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {

                                            }
                                        })
                                    }
                                    else{
                                        binding.llProgressBar.preload.visibility = View.GONE
                                        Helper().showToast("Data Company is Something Wrong!!!", this@PengawasActivity)
                                    }
                                }
                        }
                        else{
                            val refTaskUnit = FirebaseDatabase.getInstance().reference
                            val taskUnitId = refTaskUnit.push().key

                            val taskHashMap = HashMap<String, Any>()
                            taskHashMap["uid"] = taskUnitId!!
                            taskHashMap["taskUnitId"] = unitId
                            taskHashMap["taskId"] = tasksId
                            taskHashMap["sopir"] = sopir
                            taskHashMap["sopirId"] = sopirId
                            taskHashMap["unitCode"] = unitCode
                            taskHashMap["status"] = "todo"

                            databaseReference = refTaskUnit.child("TasksFleetPlanUnit").child(taskUnitId)
                            databaseReference.updateChildren(taskHashMap)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val productRefrence = FirebaseDatabase.getInstance().reference
                                            .child("Units")
                                            .child(unitId)

                                        productRefrence.addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (snapshot.exists())
                                                {
                                                    val unit: UnitList? = snapshot.getValue(UnitList::class.java)

                                                    val refUnit = FirebaseDatabase.getInstance().reference
                                                    val unitHashMap = HashMap<String, Any>()
                                                    unitHashMap["uid"] = unit?.getUid().toString()
                                                    unitHashMap["unitId"] = unitId
                                                    unitHashMap["unitCode"] = unit?.getUnitCode().toString()
                                                    unitHashMap["unitType"] = unit?.getUnitType().toString()
                                                    unitHashMap["merk"] = unit?.getMerk().toString()
                                                    unitHashMap["yom"] =  unit?.getYom().toString()
                                                    unitHashMap["status"] =  "USED"
                                                    val databaseReference = refUnit.child("Units").child(unitId)
                                                    databaseReference.updateChildren(unitHashMap)
                                                        .addOnCompleteListener { task ->
                                                            if (task.isSuccessful) {
                                                                Helper().showToast("Adding data is Successful!", this@PengawasActivity)
                                                                ivDown.isEnabled = true
                                                                recyclePlanUnitList = dialogView.findViewById(R.id.recycler_plan_unit_list)
                                                                recyclePlanUnitList.setHasFixedSize(true)
                                                                recyclePlanUnitList.layoutManager = LinearLayoutManager(context)
                                                                dialogView.findViewById<TextView>(R.id.tv_open_form).visibility = View.VISIBLE
                                                                dialogView.findViewById<TextView>(R.id.tv_close_form).visibility = View.GONE
                                                                dialogView.findViewById<LinearLayout>(R.id.detail_fleet_plan).visibility = View.VISIBLE
                                                                dialogView.findViewById<LinearLayout>(R.id.form_sopir).visibility = View.GONE
                                                                planUnit(mData[bindingAdapterPosition].getTaskId(), llEmpty)
                                                            }
                                                            else{
                                                                binding.llProgressBar.preload.visibility = View.GONE
                                                                Helper().showToast("Data Product is Something Wrong!!!", this@PengawasActivity)
                                                            }
                                                        }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {

                                            }
                                        })
                                    }
                                    else{
                                        binding.llProgressBar.preload.visibility = View.GONE
                                        Helper().showToast("Data Company is Something Wrong!!!", this@PengawasActivity)
                                    }
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }
        else{
            binding.llProgressBar.preload.visibility = View.GONE
            Helper().showToast("All field is required!", this)
        }
    }

    private fun historyList(taskId: String?, llEmpty: LinearLayout) {
        val refShareContact = FirebaseDatabase.getInstance().reference
        refShareContact.child("FleetHistoryTime")
            .child(taskId.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.llProgressBar.preload.visibility = View.GONE
                    dialogView.findViewById<RecyclerView>(R.id.recycler_history_list)?.visibility = View.VISIBLE
                    fleetHistoryTime?.clear()
                    if(snapshot.exists()) {
                        llEmpty.visibility = View.GONE
                        for (postSnapshot in snapshot.children) {
                            val history = postSnapshot.getValue(FleetHistoryTime::class.java)
                            fleetHistoryTime?.add(history!!)
                        }
                        fleetHistoryTime?.sortByDescending { it.getCreateDate() }
                        val historyTimeAdapter = HistoryTimeAdapter(this@PengawasActivity, fleetHistoryTime!!)
                        recycleHistoryList.layoutManager = LinearLayoutManager(this@PengawasActivity)
                        recycleHistoryList.adapter = historyTimeAdapter
                    }
                    else{
                        binding.llProgressBar.preload.visibility = View.GONE
                        llEmpty.visibility = View.VISIBLE
                        dialogView.findViewById<RecyclerView>(R.id.recycler_history_list)?.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    dialogView.findViewById<RecyclerView>(R.id.recycler_history_list)?.visibility = View.GONE
                    llEmpty.visibility = View.VISIBLE
                    binding.llProgressBar.preload.visibility = View.GONE
                }

            })
    }

    private fun planUnit(taskId: String?, llEmpty: LinearLayout) {
        val refShareContact = FirebaseDatabase.getInstance().reference
        refShareContact.child("TasksFleetPlanUnit")
            .orderByChild("taskId")
            .equalTo(taskId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.llProgressBar.preload.visibility = View.GONE
                    dialogView.findViewById<RecyclerView>(R.id.recycler_plan_unit_list)?.visibility = View.VISIBLE
                    planUnitList?.clear()
                    if(snapshot.exists()) {
                        llEmpty.visibility = View.GONE
                        for (postSnapshot in snapshot.children) {
                            val planUnitDataList = postSnapshot.getValue(PlanUnit::class.java)
                            if (planUnitDataList?.getStatus() != "delete") {
                                planUnitList?.add(planUnitDataList!!)
                            }
                        }
                        planUnitList?.sortByDescending { it.getSopir() }
                        val planUnitAdapter = FleetPlanUnitAdapter(this@PengawasActivity, planUnitList!!, this@PengawasActivity)
                        recyclePlanUnitList.layoutManager = LinearLayoutManager(this@PengawasActivity)
                        recyclePlanUnitList.adapter = planUnitAdapter
                    }
                    else{
                        binding.llProgressBar.preload.visibility = View.GONE
                        llEmpty.visibility = View.VISIBLE
                        dialogView.findViewById<RecyclerView>(R.id.recycler_plan_unit_list)?.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    dialogView.findViewById<RecyclerView>(R.id.recycler_plan_unit_list)?.visibility = View.GONE
                    llEmpty.visibility = View.VISIBLE
                    binding.llProgressBar.preload.visibility = View.GONE
                    val notification = dialogView.findViewById<TextView>(R.id.tv_notification)
                    notification?.visibility = View.VISIBLE
                    notification?.text = error.message
                    Helper().showToast(error.message, this@PengawasActivity)
                }

            })
    }

    private fun removePlanUnit(mData: ArrayList<PlanUnit>, bindingAdapterPosition: Int) {
        if (mData.size > 0) {
            val refTaskUnit = FirebaseDatabase.getInstance().reference
            val uid =  mData[bindingAdapterPosition].getUid().toString()

            val taskHashMap = HashMap<String, Any>()
            taskHashMap["uid"] = uid
            taskHashMap["taskUnitId"] = mData[bindingAdapterPosition].getTaskUnitId().toString()
            taskHashMap["taskId"] = mData[bindingAdapterPosition].getTaskId().toString()
            taskHashMap["sopir"] = mData[bindingAdapterPosition].getSopir().toString()
            taskHashMap["sopirId"] = mData[bindingAdapterPosition].getSopirId().toString()
            taskHashMap["unitCode"] = mData[bindingAdapterPosition].getUnitCode().toString()
            taskHashMap["status"] = "delete"

            databaseReference = refTaskUnit.child("TasksFleetPlanUnit").child(uid)
            databaseReference.updateChildren(taskHashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val productRefrence = FirebaseDatabase.getInstance().reference
                            .child("Units")
                            .child(mData[bindingAdapterPosition].getTaskUnitId().toString())

                        productRefrence.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists())
                                {
                                    val unit: UnitList? = snapshot.getValue(UnitList::class.java)

                                    val refUnit = FirebaseDatabase.getInstance().reference
                                    val unitHashMap = HashMap<String, Any>()
                                    unitHashMap["uid"] = unit?.getUid().toString()
                                    unitHashMap["unitId"] = unit?.getUnitId().toString()
                                    unitHashMap["unitCode"] = unit?.getUnitCode().toString()
                                    unitHashMap["unitType"] = unit?.getUnitType().toString()
                                    unitHashMap["merk"] = unit?.getMerk().toString()
                                    unitHashMap["yom"] =  unit?.getYom().toString()
                                    unitHashMap["status"] =  "RFU"
                                    val databaseReference = refUnit.child("Units").child(unit?.getUnitId().toString())
                                    databaseReference.updateChildren(unitHashMap)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                recyclePlanUnitList = dialogView.findViewById(R.id.recycler_plan_unit_list)
                                                recyclePlanUnitList.setHasFixedSize(true)
                                                recyclePlanUnitList.layoutManager = LinearLayoutManager(context)
                                                planUnit(mData[bindingAdapterPosition].getTaskId(), llEmpty!!)
                                            }
                                            else{
                                                binding.llProgressBar.preload.visibility = View.GONE
                                                Helper().showToast("Data Product is Something Wrong!!!", this@PengawasActivity)
                                            }
                                        }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })

                    }
                    else{
                        binding.llProgressBar.preload.visibility = View.GONE
                        Helper().showToast("Data Company is Something Wrong!!!", this)
                    }
                }
        }
    }

    private fun removeExaExchange(
        tasksId: String?,
        operator: String,
        operatorId: String,
        exaCode: String,
        alertDialog: AlertDialog,
        bindingAdapterPosition: Int,
        mData: ArrayList<TaskGroupList>,
        exaId: String
    ) {
        if (mData.size > 0) {
            val refTask = FirebaseDatabase.getInstance().reference
            val newTaskId = refTask.push().key

            val taskHashMap = HashMap<String, Any>()
            taskHashMap["uid"] = firebaseAuth.currentUser!!.uid
            taskHashMap["taskId"] = newTaskId!!
            taskHashMap["status"] = mData[bindingAdapterPosition].getStatus().toString()
            taskHashMap["date"] = mData[bindingAdapterPosition].getDate().toString()
            taskHashMap["shift"] = mData[bindingAdapterPosition].getShift().toString()
            taskHashMap["shiftId"] = mData[bindingAdapterPosition].getShiftId().toString()
            taskHashMap["shiftTime"] = mData[bindingAdapterPosition].getShiftTime().toString()
            taskHashMap["pengawasName"] = mData[bindingAdapterPosition].getPengawasName().toString()
            taskHashMap["pengawasId"] = mData[bindingAdapterPosition].getPengawasId().toString()
            taskHashMap["operatorName"] = mData[bindingAdapterPosition].getOperatorName().toString()
            taskHashMap["operatorId"] = mData[bindingAdapterPosition].getOperatorId().toString()
            taskHashMap["exaCode"] = exaCode
            taskHashMap["exaId"] = exaId
            taskHashMap["locationCode"] = mData[bindingAdapterPosition].getLocationCode().toString()
            taskHashMap["locationName"] = mData[bindingAdapterPosition].getLocationName().toString()
            taskHashMap["galianCode"] = mData[bindingAdapterPosition].getGalianCode().toString()
            taskHashMap["galianName"] = mData[bindingAdapterPosition].getGalianName().toString()
            taskHashMap["timbunanCode"] = mData[bindingAdapterPosition].getTimbunanCode().toString()
            taskHashMap["timbunanName"] = mData[bindingAdapterPosition].getTimbunanName().toString()
            taskHashMap["plan"] = mData[bindingAdapterPosition].getPlan().toString()
            taskHashMap["jarak"] = mData[bindingAdapterPosition].getJarak().toString()
            taskHashMap["targetRit"] = mData[bindingAdapterPosition].getTargetRit().toString()
            taskHashMap["createBy"] = firebaseAuth.currentUser!!.uid
            taskHashMap["updateBy"] = ""
            taskHashMap["createDate"] =  DateHelper().todayTime()
            taskHashMap["updateDate"] =  ""

            databaseReference = refTask.child("TasksFleetPlan").child(newTaskId)
            databaseReference.setValue(taskHashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val productRefrence = FirebaseDatabase.getInstance().reference
                            .child("Units")
                            .child(exaId)

                        productRefrence.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists())
                                {
                                    val unit: UnitList? = snapshot.getValue(UnitList::class.java)

                                    val refUnit = FirebaseDatabase.getInstance().reference
                                    val unitHashMap = HashMap<String, Any>()
                                    unitHashMap["uid"] = unit?.getUid().toString()
                                    unitHashMap["unitId"] = unit?.getUnitId().toString()
                                    unitHashMap["unitCode"] = unit?.getUnitCode().toString()
                                    unitHashMap["unitType"] = unit?.getUnitType().toString()
                                    unitHashMap["merk"] = unit?.getMerk().toString()
                                    unitHashMap["yom"] =  unit?.getYom().toString()
                                    unitHashMap["status"] =  "RFU"
                                    val databaseReference = refUnit.child("Units").child(unit?.getUnitId().toString())
                                    databaseReference.updateChildren(unitHashMap)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                addingTaskUnitExchange(
                                                    tasksId,
                                                    operator,
                                                    operatorId,
                                                    exaCode,
                                                    alertDialog,
                                                    exaId,
                                                    newTaskId
                                                )
                                            }
                                            else{
                                                binding.llProgressBar.preload.visibility = View.GONE
                                                Helper().showToast("Data Product is Something Wrong!!!", this@PengawasActivity)
                                            }
                                        }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })

                    }
                    else{
                        binding.llProgressBar.preload.visibility = View.GONE
                        Helper().showToast("Data Company is Something Wrong!!!", this)
                    }
                }
        }
    }

    private fun removePlanUnitExchange(
        tasksId: String?,
        sopir: String,
        sopirId: String,
        unitCode: String,
        alertDialog: AlertDialog,
        bindingAdapterPosition: Int,
        mData: ArrayList<PlanUnit>,
        unitId: String
    ) {
        if (mData.size > 0) {
            val refTaskUnit = FirebaseDatabase.getInstance().reference
            val uid =  mData[bindingAdapterPosition].getUid().toString()

            val taskHashMap = HashMap<String, Any>()
            taskHashMap["uid"] = uid
            taskHashMap["taskUnitId"] = mData[bindingAdapterPosition].getTaskUnitId().toString()
            taskHashMap["taskId"] = mData[bindingAdapterPosition].getTaskId().toString()
            taskHashMap["sopir"] = mData[bindingAdapterPosition].getSopir().toString()
            taskHashMap["sopirId"] = mData[bindingAdapterPosition].getSopirId().toString()
            taskHashMap["unitCode"] = mData[bindingAdapterPosition].getUnitCode().toString()
            taskHashMap["status"] = "unitExchange"

            databaseReference = refTaskUnit.child("TasksFleetPlanUnit").child(uid)
            databaseReference.updateChildren(taskHashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val productRefrence = FirebaseDatabase.getInstance().reference
                            .child("Units")
                            .child(mData[bindingAdapterPosition].getTaskUnitId().toString())

                        productRefrence.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists())
                                {
                                    val unit: UnitList? = snapshot.getValue(UnitList::class.java)

                                    val refUnit = FirebaseDatabase.getInstance().reference
                                    val unitHashMap = HashMap<String, Any>()
                                    unitHashMap["uid"] = unit?.getUid().toString()
                                    unitHashMap["unitId"] = unit?.getUnitId().toString()
                                    unitHashMap["unitCode"] = unit?.getUnitCode().toString()
                                    unitHashMap["unitType"] = unit?.getUnitType().toString()
                                    unitHashMap["merk"] = unit?.getMerk().toString()
                                    unitHashMap["yom"] =  unit?.getYom().toString()
                                    unitHashMap["status"] =  "RFU"
                                    val databaseReference = refUnit.child("Units").child(unit?.getUnitId().toString())
                                    databaseReference.updateChildren(unitHashMap)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                addingUnitExchange(
                                                    tasksId,
                                                    sopir,
                                                    sopirId,
                                                    unitCode,
                                                    alertDialog,
                                                    unitId
                                                )
                                            }
                                            else{
                                                binding.llProgressBar.preload.visibility = View.GONE
                                                Helper().showToast("Data Product is Something Wrong!!!", this@PengawasActivity)
                                            }
                                        }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })

                    }
                    else{
                        binding.llProgressBar.preload.visibility = View.GONE
                        Helper().showToast("Data Company is Something Wrong!!!", this)
                    }
                }
        }
    }

    private fun removeOperatorExchange(
        tasksId: String?,
        sopir: String,
        sopirId: String,
        unitCode: String,
        alertDialog: AlertDialog,
        bindingAdapterPosition: Int,
        mData: ArrayList<PlanUnit>,
        unitId: String
    ) {
        if (mData.size > 0) {
            val refTaskUnit = FirebaseDatabase.getInstance().reference
            val uid =  mData[bindingAdapterPosition].getUid().toString()

            val taskHashMap = HashMap<String, Any>()
            taskHashMap["uid"] = uid
            taskHashMap["taskUnitId"] = mData[bindingAdapterPosition].getTaskUnitId().toString()
            taskHashMap["taskId"] = mData[bindingAdapterPosition].getTaskId().toString()
            taskHashMap["sopir"] = mData[bindingAdapterPosition].getSopir().toString()
            taskHashMap["sopirId"] = mData[bindingAdapterPosition].getSopirId().toString()
            taskHashMap["unitCode"] = mData[bindingAdapterPosition].getUnitCode().toString()
            taskHashMap["status"] = "operatorExchange"

            databaseReference = refTaskUnit.child("TasksFleetPlanUnit").child(uid)
            databaseReference.updateChildren(taskHashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val productRefrence = FirebaseDatabase.getInstance().reference
                            .child("Units")
                            .child(unitId)

                        productRefrence.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists())
                                {
                                    val unit: UnitList? = snapshot.getValue(UnitList::class.java)

                                    val refUnit = FirebaseDatabase.getInstance().reference
                                    val unitHashMap = HashMap<String, Any>()
                                    unitHashMap["uid"] = unit?.getUid().toString()
                                    unitHashMap["unitId"] = unit?.getUnitId().toString()
                                    unitHashMap["unitCode"] = unit?.getUnitCode().toString()
                                    unitHashMap["unitType"] = unit?.getUnitType().toString()
                                    unitHashMap["merk"] = unit?.getMerk().toString()
                                    unitHashMap["yom"] =  unit?.getYom().toString()
                                    unitHashMap["status"] =  "RFU"
                                    val databaseReference = refUnit.child("Units").child(unitId)
                                    databaseReference.updateChildren(unitHashMap)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                addingOperatorHaulerExchange(
                                                    tasksId,
                                                    sopir,
                                                    sopirId,
                                                    unitCode,
                                                    alertDialog,
                                                    unitId
                                                )
                                            }
                                            else{
                                                binding.llProgressBar.preload.visibility = View.GONE
                                                Helper().showToast("Data Product is Something Wrong!!!", this@PengawasActivity)
                                            }
                                        }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })

                    }
                    else{
                        binding.llProgressBar.preload.visibility = View.GONE
                        Helper().showToast("Data Company is Something Wrong!!!", this)
                    }
                }
        }
    }

    private fun dialogLoading(
        mData: ArrayList<TaskGroupList>,
        bindingAdapterPosition: Int,
        recyclerDetailFleetList: RecyclerView,
        ivUp: ImageView,
        ivDown: ImageView,
        tvLoadingStart: TextView,
        tvStartShift: TextView,
        tvStopPending: TextView,
        s: String,
        v: View,
        tvBcm: TextView,
        tvTotalRitase: TextView
    ) {
        dialogStart(
            mData,
            bindingAdapterPosition,
            recyclerDetailFleetList,
            ivUp,
            ivDown,
            tvLoadingStart,
            tvStartShift,
            tvStopPending,
            s,
            v,
            tvBcm,
            tvTotalRitase
        )
    }

    private fun dialogStart(
        mData: ArrayList<TaskGroupList>,
        bindingAdapterPosition: Int,
        recyclerDetailFleetList: RecyclerView,
        ivUp: ImageView,
        ivDown: ImageView,
        tvLoadingStart: TextView,
        tvStartShift: TextView,
        tvStopPending: TextView,
        status: String,
        v: View,
        tvBcm: TextView,
        tvTotalRitase: TextView
    ) {
        val alertBuilder = android.app.AlertDialog.Builder(this)
        alertBuilder.setTitle("Start Fleet")
        alertBuilder.setMessage(HtmlCompat.fromHtml("Proses ini akan mencatat <i><b>'Start operasi fleet pada aplikasi'!</b></i><br/>", HtmlCompat.FROM_HTML_MODE_LEGACY))
        alertBuilder.setCancelable(false)
        alertBuilder.setPositiveButton("Ya"){_,_ ->
            upStatusTask(
                mData,
                bindingAdapterPosition,
                recyclerDetailFleetList,
                ivUp,
                ivDown,
                tvLoadingStart,
                tvStartShift,
                tvStopPending,
                status,
                v,
                tvBcm,
                tvTotalRitase
            )
        }
        alertBuilder.setNegativeButton("Tidak"){dialog,_ ->
            dialog.cancel()
            dialog.dismiss()
            tvStartShift.isEnabled = true
        }
        alertBuilder.show()
    }

    private fun dialogStop(
        mData: ArrayList<TaskGroupList>,
        bindingAdapterPosition: Int,
        recyclerDetailFleetList: RecyclerView,
        ivUp: ImageView,
        ivDown: ImageView,
        tvLoadingStart: TextView,
        tvStartShift: TextView,
        tvStopShift: TextView
    ) {
        val alertBuilder = android.app.AlertDialog.Builder(this)
        alertBuilder.setTitle("Stop Fleet")
        alertBuilder.setMessage(HtmlCompat.fromHtml("Proses ini akan mencatat <i><b>'Stop operasi fleet pada aplikasi'!</b></i><br/>", HtmlCompat.FROM_HTML_MODE_LEGACY))
        alertBuilder.setCancelable(false)
        alertBuilder.setPositiveButton("Lanjutkan"){_,_ ->
            upStopTask(mData, bindingAdapterPosition, recyclerDetailFleetList, ivUp, ivDown, tvLoadingStart, tvStartShift)
        }
        alertBuilder.setNegativeButton("Tidak"){dialog,_ ->
            dialog.cancel()
            dialog.dismiss()
            tvStopShift.isEnabled = true
        }
        alertBuilder.show()
    }

    private fun dialogStopWithUnit(
        mData: ArrayList<TaskGroupList>,
        bindingAdapterPosition: Int,
        recyclerDetailFleetList: RecyclerView,
        ivUp: ImageView,
        ivDown: ImageView,
        tvLoadingStart: TextView,
        tvStartShift: TextView,
        tvStopPending: TextView,
        tvStopShift: TextView
    ) {
        val alertBuilder = android.app.AlertDialog.Builder(this)
        alertBuilder.setTitle("Selesai Fleet")
        alertBuilder.setMessage(HtmlCompat.fromHtml("Proses ini akan mencatat <i><b>'Stop operasi fleet pada aplikasi'!</b></i><br/>", HtmlCompat.FROM_HTML_MODE_LEGACY))
        alertBuilder.setCancelable(false)
        alertBuilder.setPositiveButton("Selesai"){_,_ ->
            upStatusTaskWithUnit(
                mData,
                bindingAdapterPosition,
                recyclerDetailFleetList,
                ivUp,
                ivDown,
                tvLoadingStart,
                tvStartShift,
                tvStopPending
            )
        }
        alertBuilder.setNegativeButton("Tidak"){dialog,_ ->
            dialog.cancel()
            dialog.dismiss()
            tvStopShift.isEnabled = true
        }
        alertBuilder.show()
    }

    private fun upStatusTask(
        mData: ArrayList<TaskGroupList>,
        bindingAdapterPosition: Int,
        recyclerDetailFleetList: RecyclerView,
        ivUp: ImageView,
        ivDown: ImageView,
        tvLoadingStart: TextView,
        tvStartShift: TextView,
        tvStopPending: TextView,
        status: String,
        v: View,
        tvBcm: TextView,
        tvTotalRitase: TextView
    ) {
        val taskHashMap = HashMap<String, Any>()
        taskHashMap["uid"] = mData[bindingAdapterPosition].getUid().toString()
        taskHashMap["taskId"] = mData[bindingAdapterPosition].getTaskId().toString()
        taskHashMap["status"] = "doing"
        taskHashMap["date"] = mData[bindingAdapterPosition].getDate().toString()
        taskHashMap["shift"] = mData[bindingAdapterPosition].getShift().toString()
        taskHashMap["shiftId"] = mData[bindingAdapterPosition].getShiftId().toString()
        taskHashMap["shiftTime"] = mData[bindingAdapterPosition].getShiftTime().toString()
        taskHashMap["pengawasId"] = mData[bindingAdapterPosition].getPengawasId().toString()
        taskHashMap["operatorId"] = mData[bindingAdapterPosition].getOperatorId().toString()
        taskHashMap["exaCode"] = mData[bindingAdapterPosition].getExaCode().toString()
        taskHashMap["exaId"] = mData[bindingAdapterPosition].getExaId().toString()
        taskHashMap["locationCode"] = mData[bindingAdapterPosition].getLocationCode().toString()
        taskHashMap["locationName"] = mData[bindingAdapterPosition].getLocationName().toString()
        taskHashMap["galianCode"] = mData[bindingAdapterPosition].getGalianCode().toString()
        taskHashMap["galianName"] = mData[bindingAdapterPosition].getGalianName().toString()
        taskHashMap["timbunanCode"] = mData[bindingAdapterPosition].getTimbunanCode().toString()
        taskHashMap["timbunanName"] = mData[bindingAdapterPosition].getTimbunanName().toString()
        taskHashMap["plan"] = mData[bindingAdapterPosition].getPlan().toString()
        taskHashMap["jarak"] = mData[bindingAdapterPosition].getJarak().toString()
        taskHashMap["targetRit"] = mData[bindingAdapterPosition].getTaskId().toString()
        taskHashMap["createBy"] = mData[bindingAdapterPosition].getCreateBy().toString()
        taskHashMap["updateBy"] = firebaseAuth.currentUser!!.uid
        taskHashMap["createDate"] =  mData[bindingAdapterPosition].getCreateDate()!!.toInt()
        taskHashMap["updateDate"] =  DateHelper().todayTime()
        FirebaseDatabase.getInstance().reference
            .child("TasksFleetPlan")
            .child(mData[bindingAdapterPosition].getTaskId().toString()).updateChildren(taskHashMap).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val refTime = FirebaseDatabase.getInstance().reference
                    val lostTimeId = refTime.push().key
                    val taskTimeHashMap = HashMap<String, Any>()
                    taskTimeHashMap["taskId"] = mData[bindingAdapterPosition].getTaskId().toString()
                    taskTimeHashMap["timestamp"] = Helper().dateTimeNow()
                    taskTimeHashMap["remark"] = "Start operasi fleet pada aplikasi"
                    taskTimeHashMap["createBy"] = firebaseAuth.currentUser!!.uid
                    taskTimeHashMap["updateBy"] = ""
                    taskTimeHashMap["createDate"] =  DateHelper().todayTime()
                    taskTimeHashMap["updateDate"] =  ""
                    refTime.child("FleetHistoryTime")
                        .child(mData[bindingAdapterPosition].getTaskId().toString())
                        .child(lostTimeId!!)
                        .setValue(taskTimeHashMap).addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
                                binding.llProgressBar.preload.visibility = View.GONE
                                recyclerDetailFleetList.visibility = View.VISIBLE
                                ivUp.visibility = View.VISIBLE
                                ivDown.visibility = View.GONE
                                tvLoadingStart.visibility = View.VISIBLE
                                tvLoadingStart.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_fill_grey)
                                tvLoadingStart.setTextColor(ContextCompat.getColor(this, R.color.black))
                                tvLoadingStart.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_play_arrow_24, 0)
                                tvStartShift.visibility = View.GONE
                            }
                        }
                }
            }
    }

    private fun upStatusTaskWithUnit(
        mData: ArrayList<TaskGroupList>,
        bindingAdapterPosition: Int,
        recyclerDetailFleetList: RecyclerView,
        ivUp: ImageView,
        ivDown: ImageView,
        tvLoadingStart: TextView,
        tvStartShift: TextView,
        tvStopPending: TextView
    ) {
        val taskHashMap = HashMap<String, Any>()
        taskHashMap["uid"] = mData[bindingAdapterPosition].getUid().toString()
        taskHashMap["taskId"] = mData[bindingAdapterPosition].getTaskId().toString()
        taskHashMap["status"] = "done"
        taskHashMap["date"] = mData[bindingAdapterPosition].getDate().toString()
        taskHashMap["shift"] = mData[bindingAdapterPosition].getShift().toString()
        taskHashMap["shiftId"] = mData[bindingAdapterPosition].getShiftId().toString()
        taskHashMap["shiftTime"] = mData[bindingAdapterPosition].getShiftTime().toString()
        taskHashMap["pengawasId"] = mData[bindingAdapterPosition].getPengawasId().toString()
        taskHashMap["operatorId"] = mData[bindingAdapterPosition].getOperatorId().toString()
        taskHashMap["exaCode"] = mData[bindingAdapterPosition].getExaCode().toString()
        taskHashMap["exaId"] = mData[bindingAdapterPosition].getExaId().toString()
        taskHashMap["locationCode"] = mData[bindingAdapterPosition].getLocationCode().toString()
        taskHashMap["locationName"] = mData[bindingAdapterPosition].getLocationName().toString()
        taskHashMap["galianCode"] = mData[bindingAdapterPosition].getGalianCode().toString()
        taskHashMap["galianName"] = mData[bindingAdapterPosition].getGalianName().toString()
        taskHashMap["timbunanCode"] = mData[bindingAdapterPosition].getTimbunanCode().toString()
        taskHashMap["timbunanName"] = mData[bindingAdapterPosition].getTimbunanName().toString()
        taskHashMap["plan"] = mData[bindingAdapterPosition].getPlan().toString()
        taskHashMap["jarak"] = mData[bindingAdapterPosition].getJarak().toString()
        taskHashMap["targetRit"] = mData[bindingAdapterPosition].getTaskId().toString()
        taskHashMap["createBy"] = mData[bindingAdapterPosition].getCreateBy().toString()
        taskHashMap["updateBy"] = firebaseAuth.currentUser!!.uid
        taskHashMap["createDate"] =  mData[bindingAdapterPosition].getCreateDate()!!.toInt()
        taskHashMap["updateDate"] =  DateHelper().todayTime()
        FirebaseDatabase.getInstance().reference
            .child("TasksFleetPlan")
            .child(mData[bindingAdapterPosition].getTaskId().toString()).updateChildren(taskHashMap).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val refTime = FirebaseDatabase.getInstance().reference
                    val lostTimeId = refTime.push().key
                    val taskTimeHashMap = HashMap<String, Any>()
                    taskTimeHashMap["taskId"] = mData[bindingAdapterPosition].getTaskId().toString()
                    taskTimeHashMap["timestamp"] = Helper().dateTimeNow()
                    taskTimeHashMap["remark"] = "Stop operasi fleet pada aplikasi"
                    taskTimeHashMap["createBy"] = firebaseAuth.currentUser!!.uid
                    taskTimeHashMap["updateBy"] = ""
                    taskTimeHashMap["createDate"] =  DateHelper().todayTime()
                    taskTimeHashMap["updateDate"] =  ""
                    refTime.child("FleetHistoryTime")
                        .child(mData[bindingAdapterPosition].getTaskId().toString())
                        .child(lostTimeId!!)
                        .setValue(taskTimeHashMap).addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
                                recyclerDetailFleetList.visibility = View.VISIBLE
                                ivUp.visibility = View.VISIBLE
                                ivDown.visibility = View.GONE
                                tvLoadingStart.visibility = View.GONE
                                tvStartShift.visibility = View.GONE
                                tvStopPending.visibility = View.GONE

                                val productRefrence3 = FirebaseDatabase.getInstance().reference
                                    .child("Units")
                                    .child(
                                        mData[bindingAdapterPosition].getExaId()
                                            .toString())

                                productRefrence3.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot3: DataSnapshot) {
                                        if (snapshot3.exists())
                                        {
                                            val unit: UnitList? = snapshot3.getValue(UnitList::class.java)

                                            val refUnit = FirebaseDatabase.getInstance().reference
                                            val unitHashMap = HashMap<String, Any>()
                                            unitHashMap["uid"] = unit?.getUid().toString()
                                            unitHashMap["unitId"] = unit?.getUnitId().toString()
                                            unitHashMap["unitCode"] = unit?.getUnitCode().toString()
                                            unitHashMap["unitType"] = unit?.getUnitType().toString()
                                            unitHashMap["merk"] = unit?.getMerk().toString()
                                            unitHashMap["yom"] =  unit?.getYom().toString()
                                            unitHashMap["status"] =  "RFU"
                                            val databaseReference = refUnit.child("Units").child(unit?.getUnitId().toString())
                                            databaseReference.updateChildren(unitHashMap)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {

                                    }

                                })

                                val refTaskUnit = FirebaseDatabase.getInstance().reference
                                refTaskUnit.child("TasksFleetPlanUnit")
                                    .orderByChild("taskId")
                                    .equalTo(mData[bindingAdapterPosition].getTaskId().toString())
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot2: DataSnapshot) {
                                            for (postSnapshot2 in snapshot2.children) {
                                                val planUnitDataList = postSnapshot2.getValue(PlanUnit::class.java)

                                                val refTaskPlan = FirebaseDatabase.getInstance().reference

                                                val taskPlanHashMap = HashMap<String, Any>()
                                                taskPlanHashMap["sopir"] = planUnitDataList?.getSopir().toString()
                                                taskPlanHashMap["sopirId"] = planUnitDataList?.getSopirId().toString()
                                                taskPlanHashMap["status"] = "done"
                                                taskPlanHashMap["taskId"] = planUnitDataList?.getTaskId().toString()
                                                taskPlanHashMap["taskUnitId"] = planUnitDataList?.getTaskUnitId().toString()
                                                taskPlanHashMap["uid"] = planUnitDataList?.getUid().toString()
                                                taskPlanHashMap["unitCode"] = planUnitDataList?.getUnitCode().toString()

                                                databaseReference = refTaskPlan.child("TasksFleetPlanUnit").child(planUnitDataList?.getUid().toString())
                                                databaseReference.updateChildren(taskPlanHashMap)

                                                val productRefrence = FirebaseDatabase.getInstance().reference
                                                    .child("Units")
                                                    .child(planUnitDataList?.getTaskUnitId().toString())

                                                productRefrence.addListenerForSingleValueEvent(object : ValueEventListener {
                                                    override fun onDataChange(snapshot3: DataSnapshot) {
                                                        if (snapshot3.exists())
                                                        {
                                                            val unit: UnitList? = snapshot3.getValue(UnitList::class.java)

                                                            if (planUnitDataList?.getStatus() != "delete") {
                                                                val refUnit =
                                                                    FirebaseDatabase.getInstance().reference
                                                                val unitHashMap =
                                                                    HashMap<String, Any>()
                                                                unitHashMap["uid"] =
                                                                    unit?.getUid().toString()
                                                                unitHashMap["unitId"] =
                                                                    unit?.getUnitId().toString()
                                                                unitHashMap["unitCode"] =
                                                                    unit?.getUnitCode().toString()
                                                                unitHashMap["unitType"] =
                                                                    unit?.getUnitType().toString()
                                                                unitHashMap["merk"] =
                                                                    unit?.getMerk().toString()
                                                                unitHashMap["yom"] =
                                                                    unit?.getYom().toString()
                                                                unitHashMap["status"] = "RFU"
                                                                val databaseReference =
                                                                    refUnit.child("Units").child(
                                                                        unit?.getUnitId().toString()
                                                                    )
                                                                databaseReference.updateChildren(
                                                                    unitHashMap
                                                                )
                                                            }
                                                        }
                                                    }

                                                    override fun onCancelled(error: DatabaseError) {

                                                    }

                                                })
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

    private fun upStopTask(
        mData: ArrayList<TaskGroupList>,
        bindingAdapterPosition: Int,
        recyclerDetailFleetList: RecyclerView,
        ivUp: ImageView,
        ivDown: ImageView,
        tvLoadingStart: TextView,
        tvStartShift: TextView
    ) {
        val taskHashMap = HashMap<String, Any>()
        taskHashMap["uid"] = mData[bindingAdapterPosition].getUid().toString()
        taskHashMap["taskId"] = mData[bindingAdapterPosition].getTaskId().toString()
        taskHashMap["status"] = "done"
        taskHashMap["date"] = mData[bindingAdapterPosition].getDate().toString()
        taskHashMap["shift"] = mData[bindingAdapterPosition].getShift().toString()
        taskHashMap["shiftId"] = mData[bindingAdapterPosition].getShiftId().toString()
        taskHashMap["shiftTime"] = mData[bindingAdapterPosition].getShiftTime().toString()
        taskHashMap["pengawasId"] = mData[bindingAdapterPosition].getPengawasId().toString()
        taskHashMap["operatorId"] = mData[bindingAdapterPosition].getOperatorId().toString()
        taskHashMap["exaCode"] = mData[bindingAdapterPosition].getExaCode().toString()
        taskHashMap["exaId"] = mData[bindingAdapterPosition].getExaId().toString()
        taskHashMap["locationCode"] = mData[bindingAdapterPosition].getLocationCode().toString()
        taskHashMap["locationName"] = mData[bindingAdapterPosition].getLocationName().toString()
        taskHashMap["galianCode"] = mData[bindingAdapterPosition].getGalianCode().toString()
        taskHashMap["galianName"] = mData[bindingAdapterPosition].getGalianName().toString()
        taskHashMap["timbunanCode"] = mData[bindingAdapterPosition].getTimbunanCode().toString()
        taskHashMap["timbunanName"] = mData[bindingAdapterPosition].getTimbunanName().toString()
        taskHashMap["plan"] = mData[bindingAdapterPosition].getPlan().toString()
        taskHashMap["jarak"] = mData[bindingAdapterPosition].getJarak().toString()
        taskHashMap["targetRit"] = mData[bindingAdapterPosition].getTaskId().toString()
        taskHashMap["createBy"] = mData[bindingAdapterPosition].getCreateBy().toString()
        taskHashMap["updateBy"] = firebaseAuth.currentUser!!.uid
        taskHashMap["createDate"] =  mData[bindingAdapterPosition].getCreateDate()!!.toInt()
        taskHashMap["updateDate"] =  DateHelper().todayTime()
        FirebaseDatabase.getInstance().reference
            .child("TasksFleetPlan")
            .child(mData[bindingAdapterPosition].getTaskId().toString()).updateChildren(taskHashMap).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val refTime = FirebaseDatabase.getInstance().reference
                    val lostTimeId = refTime.push().key
                    val taskTimeHashMap = HashMap<String, Any>()
                    taskTimeHashMap["taskId"] = mData[bindingAdapterPosition].getTaskId().toString()
                    taskTimeHashMap["timestamp"] = Helper().dateTimeNow()
                    taskTimeHashMap["remark"] = "Stop operasi fleet pada aplikasi"
                    taskTimeHashMap["createBy"] = firebaseAuth.currentUser!!.uid
                    taskTimeHashMap["updateBy"] = ""
                    taskTimeHashMap["createDate"] =  DateHelper().todayTime()
                    taskTimeHashMap["updateDate"] =  ""
                    refTime.child("FleetHistoryTime")
                        .child(mData[bindingAdapterPosition].getTaskId().toString())
                        .child(lostTimeId!!)
                        .setValue(taskTimeHashMap).addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
                                recyclerDetailFleetList.visibility = View.VISIBLE
                                ivUp.visibility = View.VISIBLE
                                ivDown.visibility = View.GONE
                                tvLoadingStart.visibility = View.GONE
                                tvStartShift.visibility = View.GONE

                                recyclerDetailFleetList.visibility = View.VISIBLE
                                ivUp.visibility = View.VISIBLE
                                ivDown.visibility = View.GONE
                                tvLoadingStart.visibility = View.GONE
                                tvStartShift.visibility = View.GONE

                                val productRefrence3 = FirebaseDatabase.getInstance().reference
                                    .child("Units")
                                    .child(
                                        mData[bindingAdapterPosition].getExaId()
                                            .toString())

                                productRefrence3.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot3: DataSnapshot) {
                                        if (snapshot3.exists())
                                        {
                                            val unit: UnitList? = snapshot3.getValue(UnitList::class.java)

                                            val refUnit = FirebaseDatabase.getInstance().reference
                                            val unitHashMap = HashMap<String, Any>()
                                            unitHashMap["uid"] = unit?.getUid().toString()
                                            unitHashMap["unitId"] = unit?.getUnitId().toString()
                                            unitHashMap["unitCode"] = unit?.getUnitCode().toString()
                                            unitHashMap["unitType"] = unit?.getUnitType().toString()
                                            unitHashMap["merk"] = unit?.getMerk().toString()
                                            unitHashMap["yom"] =  unit?.getYom().toString()
                                            unitHashMap["status"] =  "RFU"
                                            val databaseReference = refUnit.child("Units").child(unit?.getUnitId().toString())
                                            databaseReference.updateChildren(unitHashMap)
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

    override fun onRemove(bindingAdapterPosition: Int, mData: ArrayList<PlanUnit>) {
        val alertBuilder = android.app.AlertDialog.Builder(this)
        alertBuilder.setTitle("Keluarkan Unit")
        alertBuilder.setMessage("Proses ritase Unit/Hauler pada fleet ini akan di stop!")
        alertBuilder.setCancelable(false)
        alertBuilder.setPositiveButton("Keluarkan/Stop"){_,_ ->
            removePlanUnit(mData, bindingAdapterPosition)
        }
        alertBuilder.setNeutralButton("Batal"){_,_ ->
        }
        alertBuilder.show()
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

    private fun checkUserCurrent() {
        if(firebaseAuth.currentUser?.uid == null){
            val intent = Intent(this@PengawasActivity, LoginActivity::class.java)
            NavigationHelper().navigateToActivityFlags(this@PengawasActivity, intent)
        }
        else{
            firebaseUser = firebaseAuth.currentUser
            usersRefrence = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

            usersRefrence!!.addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists())
                    {
                        user = p0.getValue(Users::class.java)

                        if (context!=null)
                        {
                            if (user?.getStatus() == "pending") {
                                val intent = Intent(this@PengawasActivity, PendingActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }
                            if (user?.getStatus() == "suspend") {
                                val intent = Intent(this@PengawasActivity, SuspendActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }
                        }
                        else{
                            binding.llProgressBar.preload.visibility = View.GONE
                            Toast.makeText(this@PengawasActivity, "Something wrong..", Toast.LENGTH_LONG).show()
                        }
                    }
                    else{
                        binding.llProgressBar.preload.visibility = View.GONE
                        Toast.makeText(this@PengawasActivity, "Something wrong..", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    binding.llProgressBar.preload.visibility = View.GONE
                }
            })
        }
    }

}