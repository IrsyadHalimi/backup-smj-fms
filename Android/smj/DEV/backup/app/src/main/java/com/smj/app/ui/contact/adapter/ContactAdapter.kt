package com.smj.app.ui.contact.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.smj.app.R
import com.smj.app.databinding.LayoutProgressBinding
import com.smj.app.helper.Helper
import com.smj.app.ui.contact.model.ContactList
import com.smj.app.ui.task.model.PlanUnit
import com.smj.app.ui.task.model.TaskGroupList

class ContactAdapter(
    private var mContext: Context,
    private var mData: ArrayList<ContactList>,
    adapterCallback: ContactAdapterCallback,
    position: String?,
    forUse: String?,
    llProgressBar: LayoutProgressBinding,
    positionCurrent: String,
    taskId: String
) : RecyclerView.Adapter<ContactAdapter.MyViewHolder>()  {

    private val mAdapterCallback: ContactAdapterCallback = adapterCallback
    private var job: String? = null
    private var forUse: String? = null
    private var firebaseAuth: FirebaseAuth
    private val llProgressBar: LayoutProgressBinding
    private val positionCurrent: String
    private val taskId: String

    init {
        this.job = position
        this.forUse = forUse
        this.firebaseAuth = FirebaseAuth.getInstance()
        this.llProgressBar = llProgressBar
        this.positionCurrent = positionCurrent
        this.taskId = taskId
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ContactAdapter.MyViewHolder {
        val v: View =
            LayoutInflater.from(mContext).inflate(R.layout.layout_item_contact, parent, false)
        return MyViewHolder(v)
    }

    @SuppressLint("NewApi")
    override fun onBindViewHolder(holder: ContactAdapter.MyViewHolder, position: Int) {
        holder.tvName.text = mData[position].getFullName()
        holder.tvJob.text = Html.fromHtml("<i>Job: "+Helper().capitalizeWords(mData[position].getPosition())+"</i>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        holder.tvNip.text = Html.fromHtml("<i>ID No: "+mData[position].getIdNumber()+"</i>", HtmlCompat.FROM_HTML_MODE_LEGACY)

        if (mData[position].getStatus().equals("active")) {
            holder.authorized.isChecked = true
            holder.unauthorized.isChecked = false
        }
        else{
            holder.authorized.isChecked = false
            holder.unauthorized.isChecked = true
        }

        if (mData[position].getPosition().equals("operator") || mData[position].getPosition().equals("Sopir")) {
            holder.authorized.isEnabled = false
            holder.unauthorized.isEnabled = false
        }
        else{
            holder.authorized.isEnabled = true
            holder.unauthorized.isEnabled = true
        }

        if (FirebaseAuth.getInstance().currentUser?.uid.equals("zJtefWv1VkMC3mtDzN0Q2VJzm6H3")) {
            holder.llauthorized.visibility = View.VISIBLE
        }
        else{
            holder.llauthorized.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvName: TextView = itemView.findViewById(R.id.tv_name)
        var tvJob: TextView = itemView.findViewById(R.id.tv_job)
        var tvNip: TextView = itemView.findViewById(R.id.tv_nip)
        var llContact: LinearLayout = itemView.findViewById(R.id.ll_contact)
        var llCall: LinearLayout = itemView.findViewById(R.id.ll_call)
        var llWa: LinearLayout = itemView.findViewById(R.id.ll_wa)
        var llEmail: LinearLayout = itemView.findViewById(R.id.ll_email)
        var authorized: RadioButton = itemView.findViewById(R.id.authorized)
        var unauthorized: RadioButton = itemView.findViewById(R.id.unauthorized)
        var llauthorized: LinearLayout = itemView.findViewById(R.id.ll_authorized)

        init {
            if(mAdapterCallback.javaClass.simpleName.equals("ContactListActivity")){
                llWa.visibility = View.GONE
                llCall.visibility = View.GONE
                llEmail.visibility = View.GONE
            }
            llContact.setOnClickListener {
                llProgressBar.preload.visibility = View.VISIBLE
                checkUserCurrent(bindingAdapterPosition, job, forUse)
            }
            llWa.setOnClickListener {
                mAdapterCallback.WhatsApp(
                    bindingAdapterPosition,
                    mData
                )
            }
            llCall.setOnClickListener {
                mAdapterCallback.Call(
                    bindingAdapterPosition,
                    mData
                )
            }
            llEmail.setOnClickListener {
                mAdapterCallback.Email(
                    bindingAdapterPosition,
                    mData
                )
            }
            authorized.setOnClickListener {
                mAdapterCallback.Authorized(
                    bindingAdapterPosition,
                    mData
                )
            }
            unauthorized.setOnClickListener {
                mAdapterCallback.Unauthorized(
                    bindingAdapterPosition,
                    mData
                )
            }
        }
    }

    interface ContactAdapterCallback {
        fun OnDetail(bindingAdapterPosition: Int, mData: ArrayList<ContactList>)
        fun WhatsApp(bindingAdapterPosition: Int, mData: ArrayList<ContactList>)
        fun Call(bindingAdapterPosition: Int, mData: java.util.ArrayList<ContactList>)
        fun Email(bindingAdapterPosition: Int, mData: java.util.ArrayList<ContactList>)
        fun Authorized(bindingAdapterPosition: Int, mData: java.util.ArrayList<ContactList>)
        fun Unauthorized(bindingAdapterPosition: Int, mData: java.util.ArrayList<ContactList>)
    }

    private fun checkUserCurrent(bindingAdapterPosition: Int, job: String?, forUse: String?) {
        var usersRefrence: Query?
        if (
            positionCurrent == "root"
            || positionCurrent == "ADMIN"
            || positionCurrent == "ADMIN HR"
            ) {
            usersRefrence = FirebaseDatabase.getInstance().reference.child("TasksFleetPlan")
                .orderByChild("pengawasId")
                .equalTo(mData[bindingAdapterPosition].getUid())
            usersRefrence.addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        var planTask: TaskGroupList? = null
                        for (dataSnapshot in p0.children) {
                            planTask = dataSnapshot.getValue(TaskGroupList::class.java)
                        }
                        if (planTask?.getStatus() == "todo" || planTask?.getStatus() == "doing" || planTask?.getStatus() == "CrossLoading") {
                            val alertBuilder = android.app.AlertDialog.Builder(mContext)
                            alertBuilder.setIcon(R.drawable.ic_worker)
                            alertBuilder.setTitle(HtmlCompat.fromHtml(" <strong>"+ planTask.getPengawasName()+"</strong>", HtmlCompat.FROM_HTML_MODE_LEGACY))
                            alertBuilder.setMessage(HtmlCompat.fromHtml("Status : <strong>Sedang bertugas di unit "+ planTask.getExaCode()+"</strong>", HtmlCompat.FROM_HTML_MODE_LEGACY))
                            alertBuilder.setCancelable(false)
                            alertBuilder.setNegativeButton("Batal"){dialog,_ ->
                                dialog.cancel()
                                dialog.dismiss()
                                llProgressBar.preload.visibility = View.GONE
                            }
                            alertBuilder.show()
                        } else{
                            llProgressBar.preload.visibility = View.GONE
                            mAdapterCallback.OnDetail(
                                bindingAdapterPosition,
                                mData
                            )
                        }

                    } else{
                        mAdapterCallback.OnDetail(
                            bindingAdapterPosition,
                            mData
                        )
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }
        else if (job == "pengawas") {
            usersRefrence = FirebaseDatabase.getInstance().reference.child("TasksFleetPlan")
                .orderByChild("pengawasId")
                .equalTo(mData[bindingAdapterPosition].getUid())
            usersRefrence.addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        var planTask: TaskGroupList? = null
                        for (dataSnapshot in p0.children) {
                            planTask = dataSnapshot.getValue(TaskGroupList::class.java)
                        }
                        if (planTask?.getStatus() == "todo" || planTask?.getStatus() == "doing" || planTask?.getStatus() == "CrossLoading") {
                            val alertBuilder = android.app.AlertDialog.Builder(mContext)
                            alertBuilder.setIcon(R.drawable.ic_worker)
                            alertBuilder.setTitle(HtmlCompat.fromHtml(" <strong>"+ planTask!!.getPengawasName()+"</strong>", HtmlCompat.FROM_HTML_MODE_LEGACY))
                            alertBuilder.setMessage(HtmlCompat.fromHtml("Status : <strong>Sedang bertugas di unit "+ planTask!!.getExaCode()+"</strong>", HtmlCompat.FROM_HTML_MODE_LEGACY))
                            alertBuilder.setCancelable(false)
                            alertBuilder.setNegativeButton("Batal"){dialog,_ ->
                                dialog.cancel()
                                dialog.dismiss()
                                llProgressBar.preload.visibility = View.GONE
                            }
                            alertBuilder.show()
                        } else{
                            llProgressBar.preload.visibility = View.GONE
                            mAdapterCallback.OnDetail(
                                bindingAdapterPosition,
                                mData
                            )
                        }

                    } else{
                        mAdapterCallback.OnDetail(
                            bindingAdapterPosition,
                            mData
                        )
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }
        else if (job == "operator") {
            usersRefrence = FirebaseDatabase.getInstance().reference.child("TasksFleetPlan")
                .orderByChild("operatorId")
                .equalTo(mData[bindingAdapterPosition].getUid())
            usersRefrence.addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        var planTask2: TaskGroupList? = null
                        for (dataSnapshot in p0.children) {
                            planTask2 = dataSnapshot.getValue(TaskGroupList::class.java)
                        }
                        if (planTask2?.getStatus() == "todo" || planTask2?.getStatus() == "doing" || planTask2?.getStatus() == "CrossLoading") {
                            val alertBuilder = android.app.AlertDialog.Builder(mContext)
                            alertBuilder.setIcon(R.drawable.ic_worker)
                            alertBuilder.setTitle(HtmlCompat.fromHtml(" <strong>"+planTask2.getOperatorName()+"</strong>", HtmlCompat.FROM_HTML_MODE_LEGACY))
                            alertBuilder.setMessage(HtmlCompat.fromHtml("Status : <strong>Sedang bertugas di unit "+planTask2.getExaCode()+"</strong>", HtmlCompat.FROM_HTML_MODE_LEGACY))
                            alertBuilder.setCancelable(false)
                            alertBuilder.setNegativeButton("Batal"){dialog,_ ->
                                dialog.cancel()
                                dialog.dismiss()
                                llProgressBar.preload.visibility = View.GONE
                            }
                            alertBuilder.show()
                        } else{
                            llProgressBar.preload.visibility = View.GONE
                            mAdapterCallback.OnDetail(
                                bindingAdapterPosition,
                                mData
                            )
                        }

                    } else{
                        mAdapterCallback.OnDetail(
                            bindingAdapterPosition,
                            mData
                        )
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }
        else if (job == "Sopir") {
            usersRefrence = FirebaseDatabase.getInstance().reference.child("TasksFleetPlanUnit")
                .orderByChild("sopirId")
                .equalTo(mData[bindingAdapterPosition].getUid())
            usersRefrence.addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        var planUnit: PlanUnit? = null
                        for (dataSnapshot in p0.children) {
                            val getPlanUnit = dataSnapshot.getValue(PlanUnit::class.java)
//                            Toast.makeText(mContext, "${getPlanUnit?.getStatus()} == $taskId", Toast.LENGTH_LONG).show()
                            if (getPlanUnit?.getStatus().equals("todo")) {
                                planUnit = getPlanUnit
                            }
                        }
//                        if (planUnit?.getStatus() == "todo" || planUnit?.getStatus() == "doing") {
//                        Toast.makeText(mContext, "${planUnit?.getTaskId()} == $taskId", Toast.LENGTH_LONG).show()
                        if (planUnit?.getStatus().equals("todo")) {
                            val alertBuilder = android.app.AlertDialog.Builder(mContext)
                            alertBuilder.setIcon(R.drawable.ic_worker)
                            alertBuilder.setTitle(HtmlCompat.fromHtml(" <strong>"+ planUnit?.getSopir() +"</strong>", HtmlCompat.FROM_HTML_MODE_LEGACY))
                            alertBuilder.setMessage(HtmlCompat.fromHtml("Status : <strong>Sedang bertugas di unit "+ planUnit?.getUnitCode() +"</strong>", HtmlCompat.FROM_HTML_MODE_LEGACY))
                            alertBuilder.setCancelable(false)
                            alertBuilder.setNegativeButton("Batal"){dialog,_ ->
                                dialog.cancel()
                                dialog.dismiss()
                                llProgressBar.preload.visibility = View.GONE
                            }
                            alertBuilder.show()
                        } else{
                            llProgressBar.preload.visibility = View.GONE
                            mAdapterCallback.OnDetail(
                                bindingAdapterPosition,
                                mData
                            )
                        }

                    } else{
                        mAdapterCallback.OnDetail(
                            bindingAdapterPosition,
                            mData
                        )
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }
        else {
            val alertBuilder = android.app.AlertDialog.Builder(mContext)
            alertBuilder.setTitle(HtmlCompat.fromHtml(" <strong>Akses dibatasi</strong>", HtmlCompat.FROM_HTML_MODE_LEGACY))
            alertBuilder.setMessage(HtmlCompat.fromHtml("Anda tidak memiliki akses!", HtmlCompat.FROM_HTML_MODE_LEGACY))
            alertBuilder.setCancelable(false)
            alertBuilder.setNegativeButton("OK"){dialog,_ ->
                dialog.cancel()
                dialog.dismiss()
                llProgressBar.preload.visibility = View.GONE
            }
            alertBuilder.show()
        }
    }
}