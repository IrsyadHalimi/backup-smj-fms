package com.smj.app.ui.task.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.smj.app.ui.contact.model.ContactList
import com.smj.app.ui.task.model.TaskGroupList
import com.smj.app.utils.response.BaseResponseFirebase
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private var taskGroupList: ArrayList<TaskGroupList>? = null
    var fDbTaskResult: MutableLiveData<BaseResponseFirebase<TaskGroupList>>? = MutableLiveData()

    fun show(shift: String, refTask: Query, currentDate: String) {
        viewModelScope.launch {
            try {
                taskGroupList = ArrayList()
                refTask.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        (taskGroupList as ArrayList).clear()
                        if(snapshot.exists()){
                            Log.i("snapshot", snapshot.toString())
                            for (dataSnapshot in snapshot.children) {
                                val task = dataSnapshot.getValue(TaskGroupList::class.java)
                                Log.i("TASKS", task.toString())
                                if (task?.getShift() == shift && task.getDate() == currentDate) {
                                    val refPengawas = FirebaseDatabase.getInstance().reference.child("Users").child(
                                    task.getPengawasId().toString())
                                    refPengawas.addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(p0: DataSnapshot) {
                                            val pengawas = p0.getValue(ContactList::class.java)
                                            val refOperator = FirebaseDatabase.getInstance().reference.child("Users") .child(
                                                task.getOperatorId().toString())
                                            refOperator.addValueEventListener(object : ValueEventListener {
                                                override fun onDataChange(p1: DataSnapshot) {
                                                    val operator = p1.getValue(ContactList::class.java)
                                                    (taskGroupList as ArrayList).add(
                                                        TaskGroupList(
                                                            taskId = task.getTaskId().toString(),
                                                            uid = task.getUid().toString(),
                                                            date = task.getDate().toString(),
                                                            shift = task.getShift().toString(),
                                                            shiftId = task.getShiftId().toString(),
                                                            shiftTime = task.getShiftTime().toString(),
                                                            pengawasId = task.getPengawasId().toString(),
                                                            pengawasName = pengawas?.getFullName().toString(),
                                                            operatorId = task.getOperatorId().toString(),
                                                            operatorName = operator?.getFullName().toString(),
                                                            exaCode = task.getExaCode().toString(),
                                                            exaId = task.getExaId().toString(),
                                                            locationCode = task.getLocationCode().toString(),
                                                            locationName = task.getLocationName().toString(),
                                                            galianCode = task.getGalianCode().toString(),
                                                            galianName = task.getGalianName().toString(),
                                                            timbunanCode = task.getTimbunanCode().toString(),
                                                            timbunanName = task.getTimbunanName().toString(),
                                                            plan = task.getPlan().toString(),
                                                            jarak = task.getJarak().toString(),
                                                            targetRit = task.getTargetRit().toString(),
                                                            status = task.getStatus().toString(),
                                                            createBy = task.getCreateBy().toString(),
                                                            createDate = task.getCreateDate()
                                                        )
                                                    )
                                                    taskGroupList?.sortBy { it.getDate() }
                                                    fDbTaskResult?.value = BaseResponseFirebase.TaskShowSuccess(taskGroupList)
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                }

                                            })
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                        }

                                    })
                                }
                                else{
                                    fDbTaskResult?.value = BaseResponseFirebase.Failed(null)
                                }
                            }
                        }
                        else{
                            fDbTaskResult?.value = BaseResponseFirebase.Failed(null)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
            }
            catch (ex: Exception) {
                Log.i("Ex.ERROR",ex.message.toString())
            }
        }
    }
}