package com.smj.app.ui.main.view.tab

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
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
import com.smj.app.R
import com.smj.app.databinding.FragmentSiangBinding
import com.smj.app.helper.DateHelper
import com.smj.app.helper.Helper
import com.smj.app.helper.NavigationHelper
import com.smj.app.ui.auth.model.Users
import com.smj.app.ui.fleet.model.UnitList
import com.smj.app.ui.pengawas.model.FleetHistoryTime
import com.smj.app.ui.pengawas.model.Ritase
import com.smj.app.ui.pengawas.view.activity.LostTimeListActivity
import com.smj.app.ui.pengawas.view.adapter.HistoryTimeAdapter
import com.smj.app.ui.task.adapter.PlanUnitAdapter
import com.smj.app.ui.task.adapter.TaskAdapter
import com.smj.app.ui.task.model.PlanUnit
import com.smj.app.ui.task.model.TaskGroupList
import com.smj.app.ui.task.view.ContactListActivity
import com.smj.app.ui.task.view.EditTaskActivity
import com.smj.app.ui.task.view.ProductListActivity
import com.smj.app.ui.task.viewModel.TaskViewModel
import com.smj.app.utils.response.BaseResponseFirebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SiangFragment : Fragment(), TaskAdapter.TaskAdapterCallback, PlanUnitAdapter.PlanUnitAdapterCallback {
    private lateinit var binding: FragmentSiangBinding
    private lateinit var recyclerViewTask: RecyclerView
    private lateinit var recyclePlanUnitList: RecyclerView
    private lateinit var recycleHistoryList: RecyclerView
    private lateinit var firebaseAuth: FirebaseAuth
    private var firebaseUser: FirebaseUser? = null
    private lateinit var databaseReference: DatabaseReference
    private val taskViewModel by viewModels<TaskViewModel>()
    private var taskGroupList: ArrayList<TaskGroupList>? = null
    private val myCalendar: Calendar = Calendar.getInstance()
    lateinit var dialogView: View
    private var usersRefrence: DatabaseReference? = null

    private var planUnitList: ArrayList<PlanUnit>? = null
    private var fleetHistoryTime: ArrayList<FleetHistoryTime>? = null

    var tvSopir: TextView? = null
    var tvSopirId: TextView? = null
    var tvSopirPosition: TextView? = null

    var tvUnit: TextView? = null
    var tvUnitId: TextView? = null
    var tvUnitCode: TextView? = null

    var llEmpty: LinearLayout? = null
    var dataRemark = "...."

    //start operator
    private val sopirResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK){
                binding.llProgressBar.preload.visibility = View.GONE
                tvSopir?.text = result.data?.getStringExtra("userName")
                tvSopirId?.text = result.data?.getStringExtra("userId")
                tvSopirPosition?.text = result.data?.getStringExtra("userPosition")
            }
        }

    //start unit
    private val unitResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK){
                binding.llProgressBar.preload.visibility = View.GONE
                tvUnit?.text = result.data?.getStringExtra("unitType")
                tvUnitId?.text = result.data?.getStringExtra("unitId")
                tvUnitCode?.text = result.data?.getStringExtra("unitCode")
            }
        }

    private val lostTimeResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK){
                binding.llProgressBar.preload.visibility = View.GONE
                dialogView.findViewById<TextView>(R.id.et_lost_time).text = result.data?.getStringExtra("lostTimeName")
                dialogView.findViewById<TextView>(R.id.tv_lost_time_id).text = result.data?.getStringExtra("lostTimeId")
            }
        }

    @SuppressLint("NotifyDataSetChanged", "SimpleDateFormat", "ClickableViewAccessibility",
        "CommitTransaction"
    )
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSiangBinding.inflate(layoutInflater)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseAuth = FirebaseAuth.getInstance()

        taskGroupList = ArrayList()
        fleetHistoryTime = ArrayList()

        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        toolbar?.visibility = View.VISIBLE
        toolbar?.setNavigationOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
        toolbar?.findViewById<ViewPager2>(R.id.view_pager_id)?.offscreenPageLimit = 1

        if(!isNetworkAvailable(requireContext())){
            Helper().showToast(requireContext().getString(R.string.connection_status), requireActivity())
        }

        val etSearch = toolbar?.findViewById<SearchView>(R.id.et_search_todo)
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val currentDate = sdf.format(Date())
        binding.tvDate.text = currentDate

        val date = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, day)
            binding.recyclerViewTodoList.visibility = View.GONE
            binding.shimmerViewTodoList.visibility = View.VISIBLE
            updateLabel()
        }
        binding.ivDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
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
                val shift = "Shift Siang"
                fleetListShow(shift, etSearch, binding.tvDate.text.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        val shift = "Shift Siang"
        fleetListShow(shift, etSearch, binding.tvDate.text.toString())

        recyclerViewTask = binding.recyclerViewTodoList
        recyclerViewTask.setHasFixedSize(true)
        recyclerViewTask.layoutManager = LinearLayoutManager(context)
        recyclerViewTask.clearOnChildAttachStateChangeListeners()

        planUnitList = ArrayList()

        taskViewModel.fDbTaskResult?.observe(requireActivity()){
            when (it) {
                is BaseResponseFirebase.TaskShowSuccess -> {
                    (taskGroupList as ArrayList).clear()
                    if(isAdded) {
                        if (it.value?.isNotEmpty() == true) {
                            recyclerViewTask.visibility = View.VISIBLE
                            for (data in it.value) {
                                if (data.getStatus() != "delete") {
                                    (taskGroupList as ArrayList).add(data)
                                }
                            }
                            val taskAdapter = TaskAdapter(
                                requireContext(),
                                taskGroupList!!,
                                this@SiangFragment
                            )
                            recyclerViewTask.layoutManager = LinearLayoutManager(activity)
                            recyclerViewTask.adapter = taskAdapter
                            recyclerViewTask.adapter?.notifyDataSetChanged()
                            recyclerViewTask.clearOnChildAttachStateChangeListeners()
                            taskAdapter.notifyDataSetChanged()
                            binding.shimmerViewTodoList.visibility = View.GONE
                            binding.llEmptyTodoList.visibility = View.GONE
                        }
                        else {
                            binding.llProgressBar.preload.visibility = View.GONE
                            binding.shimmerViewTodoList.visibility = View.GONE
                            recyclerViewTask.visibility = View.GONE
                            binding.llEmptyTodoList.visibility = View.VISIBLE
                        }
                    }
                }
                is BaseResponseFirebase.Failed -> {
                    binding.llProgressBar.preload.visibility = View.GONE
                    binding.shimmerViewTodoList.visibility = View.GONE
                    recyclerViewTask.visibility = View.GONE
                    binding.llEmptyTodoList.visibility = View.VISIBLE
                }
                else -> {}
            }
        }

        return binding.root
    }

    private fun updateLabel() {
        val myFormat = "dd/MM/yyyy"
        val dateFormat = SimpleDateFormat(myFormat, Locale.US)
        binding.tvDate.text = dateFormat.format(myCalendar.time)
    }

    companion object {
    }

    private fun fleetListShow(shift: String, search: SearchView?, currentDate: String) {
        val refTasks = FirebaseDatabase.getInstance().reference
            .child("TasksFleetPlan")
            .orderByChild("date")
            .equalTo(currentDate)
        taskViewModel.show(shift, refTasks, currentDate)
//        if(search != null){
//            val refTasks = FirebaseDatabase.getInstance().reference
//                .child("TasksFleetPlan")
//            taskViewModel.search(shift, search.toString(), refTasks)
//        }
//        else{
//            val refTasks = FirebaseDatabase.getInstance().reference
//                .child("TasksFleetPlan")
//            taskViewModel.show(shift, refTasks)
//        }
    }

    override fun onDetail(bindingAdapterPosition: Int, mData: ArrayList<TaskGroupList>) {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        showFormDialog(bindingAdapterPosition, mData)
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
        val refTasks = FirebaseDatabase.getInstance().reference
            .child("TasksFleetPlan")
            .child(mData[bindingAdapterPosition].getTaskId().toString())
        refTasks.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val task = snapshot.getValue(TaskGroupList::class.java)

                    if (task?.getStatus().equals("todo")) {
                        val refTasksUnit = FirebaseDatabase.getInstance().reference
                            .child("TasksFleetPlanUnit")
                            .orderByChild("taskId")
                            .equalTo(mData[bindingAdapterPosition].getTaskId().toString())
                        refTasksUnit.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshotUnit: DataSnapshot) {
                                if(snapshotUnit.exists()){
                                    recyclerDetailFleetList.visibility = View.VISIBLE
                                    ivUp.visibility = View.VISIBLE
                                    ivDown.visibility = View.GONE
                                }
                                else{
                                    showFormDialog(bindingAdapterPosition, mData)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                    }
                    else{
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
        v: View
    ) {
        @SuppressLint("InflateParams")
        val bubbleLayout = LayoutInflater.from(requireContext()).inflate(R.layout.bubble_menu, null) as BubbleLayout
        val location = IntArray(2)
        v.getLocationInWindow(location)
        bubbleLayout.arrowDirection = ArrowDirection.TOP
        val popupWindow = BubblePopupHelper.create(requireContext(), bubbleLayout)
        bubbleLayout.arrowDirection = ArrowDirection.TOP
        popupWindow.showAtLocation(
            v,
            Gravity.TOP,
            location[0],
            v.height + location[1]
        )

        bubbleLayout.findViewById<TextView>(R.id.bubble_add_unit).setOnClickListener {
            binding.llProgressBar.preload.visibility = View.VISIBLE
            showFormDialog(bindingAdapterPosition, mData)
            popupWindow.dismiss()
        }

        bubbleLayout.findViewById<TextView>(R.id.bubble_lost_time).setOnClickListener {
            binding.llProgressBar.preload.visibility = View.VISIBLE
            showLostTimeDialog(bindingAdapterPosition, mData)
            popupWindow.dismiss()
        }
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

    override fun whatsappOperator(
        bindingAdapterPosition: Int,
        mData: java.util.ArrayList<TaskGroupList>
    ) {
        getUserForWa(mData[bindingAdapterPosition].getOperatorId().toString())
    }

    override fun phoneOperator(bindingAdapterPosition: Int, mData: ArrayList<TaskGroupList>) {
        getUserForPhone(mData[bindingAdapterPosition].getOperatorId().toString())
    }

    override fun tvHistory(mData: ArrayList<TaskGroupList>, bindingAdapterPosition: Int) {
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext(), R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
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
                        val historyTimeAdapter = HistoryTimeAdapter(requireContext(), fleetHistoryTime!!)
                        recycleHistoryList.layoutManager = LinearLayoutManager(requireContext())
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

    override fun llMenuUnit(bindingAdapterPosition: Int, mData: ArrayList<PlanUnit>, v: View) {
        @SuppressLint("InflateParams")
        val bubbleLayout = LayoutInflater.from(requireContext()).inflate(R.layout.bubble_menu_unit, null) as BubbleLayout
        val location = IntArray(2)
        v.getLocationInWindow(location)
        bubbleLayout.arrowDirection = ArrowDirection.TOP
        val popupWindow = BubblePopupHelper.create(requireContext(), bubbleLayout)
        bubbleLayout.arrowDirection = ArrowDirection.TOP
        popupWindow.showAtLocation(
            v,
            Gravity.TOP,
            location[0],
            v.height + location[1]
        )
    }

    override fun phone(bindingAdapterPosition: Int, mData: ArrayList<PlanUnit>, v: View) {
        getUserForPhone(mData[bindingAdapterPosition].getSopirId().toString())
    }

    override fun whatsapp(bindingAdapterPosition: Int, mData: ArrayList<PlanUnit>, v: View) {
        getUserForWa(mData[bindingAdapterPosition].getSopirId().toString())
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

    override fun showFormRemark(
        bindingAdapterPosition: Int,
        mData: java.util.ArrayList<Ritase>,
        v: View?,
        tvRemark: TextView
    ) {
        binding.llProgressBar.preload.visibility = View.VISIBLE

        val refShareContact = FirebaseDatabase.getInstance().reference
        refShareContact.child("Remark")
            .child(mData[bindingAdapterPosition].getRitaseId().toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        dataRemark = snapshot.child("remark").value.toString()
                        viewRemark(mData, bindingAdapterPosition, v, tvRemark, "Update Remark")
                    }
                    else {
                        viewRemark(mData, bindingAdapterPosition, v, tvRemark, "Save Remark")
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    @SuppressLint("SetTextI18n")
    private fun viewRemark(
        mData: java.util.ArrayList<Ritase>,
        bindingAdapterPosition: Int,
        v: View?,
        tvRemark: TextView,
        s: String
    ) {
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext(), R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
        val inflater = LayoutInflater.from(requireContext())
        dialogView = inflater.inflate(R.layout.layout_form_remark, null)

        val close = dialogView.findViewById<LinearLayout>(R.id.icon_close)
        val hauler = dialogView.findViewById<TextView>(R.id.tv_hauler)
        val time = dialogView.findViewById<TextView>(R.id.tv_time)
        val remark = dialogView.findViewById<EditText>(R.id.et_remark)
        remark.isEnabled = false
        val addRemark = dialogView.findViewById<LinearLayout>(R.id.add_remark)
        addRemark.visibility = View.GONE
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

        close.setOnClickListener {
            alertDialog.dismiss()
            binding.llProgressBar.preload.visibility = View.GONE
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun showLostTimeDialog(bindingAdapterPosition: Int, mData: ArrayList<TaskGroupList>) {
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext(), R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
        val inflater = this.layoutInflater
        dialogView = inflater.inflate(R.layout.layout_form_lost_time, null)

        val close = dialogView.findViewById<LinearLayout>(R.id.icon_close)
        val etLostTime = dialogView.findViewById<EditText>(R.id.et_lost_time)
        val btnTimeStart = dialogView.findViewById<ImageView>(R.id.iv_time_start)
        val timeStart = dialogView.findViewById<TextView>(R.id.et_time_start)
        val btnTimeEnd = dialogView.findViewById<ImageView>(R.id.iv_time_end)
        val timeEnd = dialogView.findViewById<TextView>(R.id.et_time_end)

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
            TimePickerDialog(requireContext(), timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        btnTimeEnd.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                timeEnd.text = SimpleDateFormat("HH:mm").format(cal.time)
            }
            TimePickerDialog(requireContext(), timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        close.setOnClickListener {
            alertDialog.dismiss()
            binding.llProgressBar.preload.visibility = View.GONE
        }
    }

    private fun getItemLostTimeActivity() {
        val intent = Intent(requireContext(), LostTimeListActivity::class.java)
        lostTimeResult.launch(intent)
    }

    @SuppressLint("ClickableViewAccessibility", "SimpleDateFormat", "InflateParams", "SetTextI18n")
    private fun showFormDialog(bindingAdapterPosition: Int, mData: ArrayList<TaskGroupList>) {
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext(), R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
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
        tvOperator.text = HtmlCompat.fromHtml("<small><i>Operator: </i></small>"+mData[bindingAdapterPosition].getOperatorName(), HtmlCompat.FROM_HTML_MODE_LEGACY)
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

        val tvStatus = dialogView.findViewById<TextView>(R.id.tv_status)
        tvStatus.text = Helper().capitalizeWords(mData[bindingAdapterPosition].getStatus())
        val tvStatusLabel = dialogView.findViewById<TextView>(R.id.tv_status_label)
        tvStatusLabel.text = HtmlCompat.fromHtml("<small><i>Status</i></small>", HtmlCompat.FROM_HTML_MODE_LEGACY)

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
            val intent = Intent(requireContext(), ContactListActivity::class.java)
            intent.putExtra("position", "Sopir")
            intent.putExtra("taskId", "pengawas")
            sopirResult.launch(intent)
        }

        tvUnit?.setOnClickListener {
            val intent = Intent(requireContext(), ProductListActivity::class.java)
            intent.putExtra("unitType", "DUMP TRUCK")
            intent.putExtra("forUse", "pengawas")
            unitResult.launch(intent)
        }

        tvExa.setOnClickListener {
            val intent = Intent(requireContext(), EditTaskActivity::class.java)
            intent.putExtra("uid", mData[bindingAdapterPosition].getUid())
            intent.putExtra("taskId", mData[bindingAdapterPosition].getTaskId())
            NavigationHelper().navigateToActivityCallback(requireActivity(), intent)
        }

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        close.setOnClickListener {
            alertDialog.dismiss()
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
                unitId
            )
        }

        recyclePlanUnitList = dialogView.findViewById(R.id.recycler_plan_unit_list)
        recyclePlanUnitList.setHasFixedSize(true)
        recyclePlanUnitList.layoutManager = LinearLayoutManager(context)
        planUnit(mData[bindingAdapterPosition].getTaskId(), llEmpty!!)
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
        unitId: String
    ) {
        if(tasksId!!.isNotEmpty()
            && sopir.isNotEmpty()
            && sopirId.isNotEmpty()
            && unitCode.isNotEmpty()
        ) {
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
                                                Helper().showToast("Adding data is Successful!", requireActivity())
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
                                                Helper().showToast("Data Product is Something Wrong!!!", requireActivity())
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
                        Helper().showToast("Data Company is Something Wrong!!!", requireActivity())
                    }
                }
        }
        else{
            binding.llProgressBar.preload.visibility = View.GONE
            Helper().showToast("All field is required!", requireActivity())
        }
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
                        val planUnitAdapter = PlanUnitAdapter(requireActivity(), planUnitList!!, this@SiangFragment)
                        recyclePlanUnitList.layoutManager = LinearLayoutManager(activity)
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
                    Helper().showToast(error.message, requireActivity())
                }

            })
    }

    private fun removePlanUnit(mData: ArrayList<PlanUnit>, bindingAdapterPosition: Int) {
        if (mData.size > 0) {
//            FirebaseDatabase.getInstance().reference
//                .child("TasksFleetPlanUnit")
//                .child(mData[bindingAdapterPosition].getTaskUnitId().toString()).removeValue()
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
                                                Helper().showToast("Data Product is Something Wrong!!!", requireActivity())
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
                        Helper().showToast("Data Company is Something Wrong!!!", requireActivity())
                    }
                }
        }
    }

    override fun onRemove(bindingAdapterPosition: Int, mData: ArrayList<PlanUnit>) {
        val alertBuilder = android.app.AlertDialog.Builder(requireContext())
        alertBuilder.setTitle("Keluarkan Unit")
        alertBuilder.setMessage("Unit akan di keluarkan, riwayat ritase unit pada laporan fleet akan ikut terhapus!")
        alertBuilder.setCancelable(false)
        alertBuilder.setPositiveButton("Keluarkan"){_,_ ->
            removePlanUnit(mData, bindingAdapterPosition)
        }
        alertBuilder.setNeutralButton("Batal"){_,_ ->
        }
        alertBuilder.show()
    }

    private fun getUserForWa(operatorId: String) {
        firebaseUser = firebaseAuth.currentUser
        usersRefrence = FirebaseDatabase.getInstance().reference.child("Users").child(operatorId)

        usersRefrence!!.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists())
                {
                    val user: Users? = p0.getValue(Users::class.java)

                    if (context!=null)
                    {
                        var phone = user?.getPhoneNumber()
                        val uri = Uri.parse("smsto:$phone")
                        val sendIntent = Intent(Intent.ACTION_SENDTO, uri)

                        val shareIntent = Intent.createChooser(sendIntent, null)
                        NavigationHelper().navigateToActivityCallback(requireActivity(), shareIntent)
                    }
                    else{
//                            binding.llProgressBar.preload.visibility = View.GONE
                        Toast.makeText(requireContext(), "Something wrong..", Toast.LENGTH_LONG).show()
                    }
                }
                else{
//                        binding.llProgressBar.preload.visibility = View.GONE
                    Toast.makeText(requireContext(), "Something wrong..", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
//                    binding.llProgressBar.preload.visibility = View.GONE
            }
        })
    }

    private fun getUserForPhone(operatorId: String) {
        firebaseUser = firebaseAuth.currentUser
        usersRefrence = FirebaseDatabase.getInstance().reference.child("Users").child(operatorId)

        usersRefrence!!.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists())
                {
                    val user: Users? = p0.getValue(Users::class.java)

                    if (context!=null)
                    {
                        var phone = user?.getPhoneNumber()
                        val dialIntent = Intent(Intent.ACTION_DIAL)
                        dialIntent.data = Uri.parse("tel:$phone")
                        startActivity(dialIntent)
                    }
                    else{
//                            binding.llProgressBar.preload.visibility = View.GONE
                        Toast.makeText(requireContext(), "Something wrong..", Toast.LENGTH_LONG).show()
                    }
                }
                else{
//                        binding.llProgressBar.preload.visibility = View.GONE
                    Toast.makeText(requireContext(), "Something wrong..", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
//                    binding.llProgressBar.preload.visibility = View.GONE
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