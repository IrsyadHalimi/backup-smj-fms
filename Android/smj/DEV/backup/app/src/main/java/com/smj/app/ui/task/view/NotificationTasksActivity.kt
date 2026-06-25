package com.smj.app.ui.task.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.smj.app.R
import com.smj.app.databinding.ActivityNotificationTasksBinding
import com.smj.app.ui.task.adapter.TaskConfirmAdapter
import com.smj.app.ui.task.model.TaskGroupList
import com.smj.app.ui.task.viewModel.TaskViewModel
import com.smj.app.utils.response.BaseResponseFirebase

class NotificationTasksActivity : AppCompatActivity(), TaskConfirmAdapter.TaskConfirmAdapterCallback {

    private lateinit var binding: ActivityNotificationTasksBinding
    private var context: Context? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var recyclerViewTasks: RecyclerView
    private lateinit var recyclerViewUserList: RecyclerView

    private var firebaseUser: FirebaseUser? = null
    private val taskViewModel by viewModels<TaskViewModel>()

    private var taskGroupList: ArrayList<TaskGroupList>? = null

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this.applicationContext
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        toolbar.visibility = View.VISIBLE
        toolbar.setNavigationIcon(R.drawable.ic_times)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        supportActionBar?.setDisplayShowTitleEnabled(false)

        taskGroupList = ArrayList()

        recyclerViewTasks = binding.recyclerTasksList
        recyclerViewTasks.setHasFixedSize(true)
        recyclerViewTasks.layoutManager = LinearLayoutManager(context)

        taskViewModel.fDbTaskResult?.observe(this){
            when (it) {
                is BaseResponseFirebase.TaskShowSuccess -> {
                    (taskGroupList as ArrayList).clear()
                    if (it.value?.isNotEmpty() == true) {
                        binding.shimmerViewContainer.visibility = View.GONE
                        recyclerViewTasks.visibility = View.VISIBLE
                        binding.llEmpty.visibility = View.GONE
                        for (data in it.value) {
                            (taskGroupList as ArrayList).add(data)
                        }

                        val taskConfirmAdapter = TaskConfirmAdapter(
                            this,
                            taskGroupList!!,
                            this@NotificationTasksActivity
                        )
                        recyclerViewTasks.layoutManager = LinearLayoutManager(context)
                        recyclerViewTasks.adapter = taskConfirmAdapter
                    } else {
                        binding.shimmerViewContainer.visibility = View.GONE
                        recyclerViewTasks.visibility = View.GONE
                        binding.llEmpty.visibility = View.VISIBLE
                    }
                }
                is BaseResponseFirebase.Failed -> {
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewTasks.visibility = View.GONE
                    binding.llEmpty.visibility = View.VISIBLE
                }
                else -> {
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewTasks.visibility = View.GONE
                    binding.llEmpty.visibility = View.VISIBLE
                }
            }
        }

        binding.done.setOnClickListener {
            importTasks()
        }
    }

    private fun importTasks() {
        val dialogBuilder = MaterialAlertDialogBuilder(this, R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
        dialogBuilder.setCancelable(false)
        dialogBuilder.setMessage(HtmlCompat.fromHtml("The data will be imported to your account list<br/>", HtmlCompat.FROM_HTML_MODE_LEGACY))
        dialogBuilder.setPositiveButton("Yes"){ dialog, _ ->
            addTasks(dialog)
        }
        dialogBuilder.setNegativeButton("Cancel") { dialog,_ ->
            dialog.dismiss()
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(alertDialog.window!!.attributes)
        layoutParams.width = 900
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        alertDialog.window!!.attributes = layoutParams
    }

    private fun addTasks(dialog: DialogInterface) {
        dialog.dismiss()
        binding.llProgressBar.preload.visibility = View.VISIBLE

        val refTasks = FirebaseDatabase.getInstance().reference
//        for (task in taskGroupList!!){
//            val taskHashMap = HashMap<String, Any>()
//            taskHashMap["uid"] = task.getUid().toString()
//            taskHashMap["taskId"] = task.getTaskId().toString()
//            taskHashMap["category"] = task.getCategory().toString()
//            taskHashMap["reasonRejected"] = task.getReasonRejected().toString()
//            taskHashMap["contactId"] = task.getContactId().toString()
//            taskHashMap["productId"] = task.getProductId().toString()
//            val nominal = task.getNominal().toString()
//            taskHashMap["nominal"] = nominal.replace(",","")
//            taskHashMap["priority"] = task.getPriority()!!.toInt()
//            taskHashMap["dueDate"] = task.getDueDate().toString()
//            taskHashMap["reminder"] = task.getReminder().toString()
//            taskHashMap["status"] = task.getStatus()!!.toInt()
//            taskHashMap["note"] = task.getNote().toString()
//            taskHashMap["createDate"] =  DateHelper().todayTime()
//            taskHashMap["mapTime"] = task.getMapTime().toString()
//            taskHashMap["mapAddress"] = task.getMapAddress().toString()
//            taskHashMap["latitude"] = task.getLatitude().toString().ifEmpty { "-2.548926" }
//            taskHashMap["longitude"] = task.getLongitude().toString().ifEmpty { "118.0148634" }
//
//            databaseReference = refTasks
//                .child("Tasks")
//                .child(firebaseUser?.uid.toString())
//                .child(task.getTaskId().toString())
//            databaseReference.updateChildren(taskHashMap)
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        binding.llProgressBar.preload.visibility = View.GONE
//                        databaseReference = FirebaseDatabase.getInstance().reference
//                            .child("Shared")
//                            .child(firebaseUser!!.uid)
//                            .child("Tasks")
//                        databaseReference.removeValue()
//                        tasksListShow()
//                    }
//                    else{
//                        binding.llProgressBar.preload.visibility = View.GONE
//                        Helper().showToast("Data Task is Something Wrong!!!", this)
//                    }
//                }
//        }
    }

    override fun onDetail(bindingAdapterPosition: Int, mData: java.util.ArrayList<TaskGroupList>) {

    }
}