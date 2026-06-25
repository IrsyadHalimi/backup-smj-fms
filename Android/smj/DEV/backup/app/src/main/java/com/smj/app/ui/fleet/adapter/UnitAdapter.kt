package com.smj.app.ui.fleet.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
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
import com.smj.app.helper.FormatNumber
import com.smj.app.ui.auth.model.Users
import com.smj.app.ui.fleet.model.UnitList
import com.smj.app.ui.task.model.PlanUnit
import com.smj.app.ui.task.model.TaskGroupList

class UnitAdapter(
    private var mContext: Context,
    private var mData: ArrayList<UnitList>,
    adapterCallback: UnitAdapterCallback,
    forUse: String?,
    unitType: String?,
    llProgressBar: LayoutProgressBinding,
    taskId: String?,
    exaCode: String?
) : RecyclerView.Adapter<UnitAdapter.MyViewHolder>()  {

    private val mAdapterCallback: UnitAdapterCallback = adapterCallback
    private var formatNumber: FormatNumber? = null
    private var firebaseAuth: FirebaseAuth
    private var forUse: String? = null
    private var unit: String? = null
    private val llProgressBar: LayoutProgressBinding
    private val taskId: String?
    private val exaCode: String?

    init {
        this.formatNumber = FormatNumber()
        this.firebaseAuth = FirebaseAuth.getInstance()
        this.forUse = forUse
        this.unit= unitType
        this.llProgressBar = llProgressBar
        this.taskId = taskId
        this.exaCode = exaCode
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UnitAdapter.MyViewHolder {
        val v: View =
            LayoutInflater.from(mContext).inflate(R.layout.layout_item_product, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: UnitAdapter.MyViewHolder, position: Int) {
        holder.unitCode.text = mData[position].getUnitCode()
        holder.unitType.text = mData[position].getUnitType()
        holder.unitId.text = mData[position].getUnitId()
        holder.tvStatus.text = mData[position].getStatus()

        if (mData[position].getUnitType().equals("EXCAVATOR")) {
            val drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_excavator)
            holder.unitCode.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }

        if (mData[position].getUnitType().equals("BULLDOZER")) {
            val drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_buldozzer)
            holder.unitCode.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }

        if (mData[position].getUnitType().equals("COMPACTOR")) {
            val drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_compactor)
            holder.unitCode.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }

        if (mData[position].getUnitType().equals("DRILL MACHINE")) {
            val drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_drill_machine)
            holder.unitCode.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }

        if (mData[position].getUnitType().equals("FORKLIFT")) {
            val drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_forklift)
            holder.unitCode.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }

        if (mData[position].getUnitType().equals("FUEL TRUCK")) {
            val drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_fuel_truck)
            holder.unitCode.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }

        if (mData[position].getUnitType().equals("ARTICULATED DUMP TRUCK") || mData[position].getUnitType().equals("DUMP TRUCK")) {
            val drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_dump_truck)
            holder.unitCode.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }

        if (mData[position].getUnitType().equals("LIGHT VEHICLE")) {
            val drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_light_vehicle)
            holder.unitCode.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }

        if (mData[position].getUnitType().equals("LUBE TRUCK")) {
            val drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_lube_truck)
            holder.unitCode.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }

        if (mData[position].getUnitType().equals("MANHAUL")) {
            val drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_manhaul)
            holder.unitCode.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }

        if (mData[position].getUnitType().equals("MOTOR GRADER")) {
            val drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_motor_grader)
            holder.unitCode.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }

        if (mData[position].getUnitType().equals("RIGID TRUCK")) {
            val drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_dump_truck)
            holder.unitCode.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }

        if (mData[position].getUnitType().equals("ROUGH TERRAIN CRANE")) {
            val drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_crane_truck)
            holder.unitCode.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }

        if (mData[position].getUnitType().equals("WATER TRUCK")) {
            val drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_water_truck)
            holder.unitCode.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var unitId: CheckBox = itemView.findViewById(R.id.unit_id)
        var tvStatus: TextView = itemView.findViewById(R.id.tv_status)
        var unitCode: TextView = itemView.findViewById(R.id.tv_unit_code)
        var unitType: TextView = itemView.findViewById(R.id.tv_unit_type)
        var detail: LinearLayout = itemView.findViewById(R.id.detail)

        init {
            detail.setOnClickListener {
                llProgressBar.preload.visibility = View.VISIBLE
                if (mData[bindingAdapterPosition].getStatus() != "RFU" && forUse?.isNotEmpty() == true) {
                    checkUserCurrent(bindingAdapterPosition, unitType, unit)
                }
                else {
                    mAdapterCallback.onDetail(
                        bindingAdapterPosition,
                        mData,
                        planUnitDataList = null,
                        null
                    )
                }
            }
        }
    }

    interface UnitAdapterCallback {
        fun onDetail(
            adapterPosition: Int,
            product: ArrayList<UnitList>,
            planUnitDataList: Users?,
            dataTask: TaskGroupList?
        )

    }

    private fun checkUserCurrent(bindingAdapterPosition: Int, unitType: TextView, unit: String?) {
        val firebaseUser = firebaseAuth.currentUser
        val usersRefrence = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        usersRefrence.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists())
                {
                    val userPengawas:Users? = p0.getValue(Users::class.java)
                    if (userPengawas?.getStatus() == "active") {
                        if (
                            userPengawas.getPosition() == "Foreman Produksi"
                            || userPengawas.getPosition() == "Sr Foreman Produksi"
                            || userPengawas.getPosition() == "Jr Foreman Produksi"
                            || userPengawas.getPosition() == "SPV Produksi"
                            || userPengawas.getPosition() == "Jr SPV Produksi"
                            || userPengawas.getPosition() == "ADMIN"
                        ) {
                            val alertBuilder = android.app.AlertDialog.Builder(mContext)
                            if (this@UnitAdapter.unit.equals("EXCAVATOR")) {
                                alertBuilder.setIcon(R.drawable.ic_excavator)
                            }
                            if (this@UnitAdapter.unit.equals("DUMP TRUCK")) {
                                alertBuilder.setIcon(R.drawable.ic_dump_truck)
                            }
                            alertBuilder.setTitle(HtmlCompat.fromHtml(mData[bindingAdapterPosition].getUnitCode()+" <br/><small>"+mData[bindingAdapterPosition].getUnitType()+"</small>", HtmlCompat.FROM_HTML_MODE_LEGACY))
                            alertBuilder.setMessage(HtmlCompat.fromHtml("Status Unit : <strong>"+mData[bindingAdapterPosition].getStatus()+"</strong> <br/><i>Saat ini unit tidak bisa digunakan!</i>", HtmlCompat.FROM_HTML_MODE_LEGACY))
                            alertBuilder.setCancelable(false)
                            alertBuilder.setNegativeButton("Batal"){ dialog,_ ->
                                dialog.cancel()
                                dialog.dismiss()
                                llProgressBar.preload.visibility = View.GONE
                            }
                            if (mData[bindingAdapterPosition].getStatus() == "USED") {
                                alertBuilder.setNeutralButton("Set Cros Loading") { dialog, _ ->
                                    checkUnit(
                                        bindingAdapterPosition,
                                        mData,
                                        taskId,
                                        unit,
                                        userPengawas
                                    )
                                }
                            }
                            alertBuilder.show()
                        }
                        else if (forUse?.isNotEmpty() == true && forUse.equals("pengawas")) {
                            val alertBuilder = android.app.AlertDialog.Builder(mContext)
                            if (this@UnitAdapter.unit.equals("EXCAVATOR")) {
                                alertBuilder.setIcon(R.drawable.ic_excavator)
                            }
                            if (this@UnitAdapter.unit.equals("DUMP TRUCK")) {
                                alertBuilder.setIcon(R.drawable.ic_dump_truck)
                            }
                            alertBuilder.setTitle(HtmlCompat.fromHtml(mData[bindingAdapterPosition].getUnitCode()+" <br/><small>"+mData[bindingAdapterPosition].getUnitType()+"</small>", HtmlCompat.FROM_HTML_MODE_LEGACY))
                            alertBuilder.setMessage(HtmlCompat.fromHtml("Status Unit : <strong>"+mData[bindingAdapterPosition].getStatus()+"</strong> <br/><i>Saat ini unit tidak bisa digunakan!</i>", HtmlCompat.FROM_HTML_MODE_LEGACY))
                            alertBuilder.setCancelable(false)
                            alertBuilder.setNegativeButton("OK"){dialog,_ ->
                                dialog.cancel()
                                llProgressBar.preload.visibility = View.GONE
                            }
                            alertBuilder.show()
                        }
                        else{
                            llProgressBar.preload.visibility = View.GONE
                            mAdapterCallback.onDetail(
                                bindingAdapterPosition,
                                mData,
                                planUnitDataList = null,
                                null
                            )
                        }
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun checkUnit(
        bindingAdapterPosition: Int,
        mData: ArrayList<UnitList>,
        taskId: String?,
        unit: String?,
        userPengawas: Users
    ) {
        var refPlanUnit: Query? = null
        if (unit.equals("DUMP TRUCK")) {
            refPlanUnit = FirebaseDatabase.getInstance().reference.child("TasksFleetPlanUnit")
                .orderByChild("taskId")
                .equalTo(taskId)
        }
        if (unit.equals("EXCAVATOR")) {
            refPlanUnit = FirebaseDatabase.getInstance().reference.child("TasksFleetPlan")
                .orderByChild("exaId")
                .equalTo(mData[bindingAdapterPosition].getUnitId())
        }
        refPlanUnit?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    if (unit.equals("DUMP TRUCK")) {
                        var planUnit:PlanUnit? = null
                        for (postSnapshot in snapshot.children) {
                            val planUnitDataList = postSnapshot.getValue(PlanUnit::class.java)
                            if (planUnitDataList?.getUnitCode() == mData[bindingAdapterPosition].getUnitCode()) {
                                planUnit = planUnitDataList
                            }
                        }
                        if (planUnit?.getUnitCode() != mData[bindingAdapterPosition].getUnitCode()) {
                            val usersRefrence =
                                FirebaseDatabase.getInstance().reference.child("Users")
                                    .child(planUnit?.getSopirId().toString())

                            usersRefrence.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        val user: Users? = snapshot.getValue(Users::class.java)
                                        llProgressBar.preload.visibility = View.GONE
                                        llProgressBar.preload.visibility = View.GONE
                                        mAdapterCallback.onDetail(
                                            bindingAdapterPosition,
                                            mData,
                                            user,
                                            null
                                        )
                                    }
                                    else{
                                        FirebaseDatabase.getInstance().reference.child("TasksFleetPlanUnit")
                                            .orderByChild("taskUnitId")
                                            .equalTo(mData[bindingAdapterPosition].getUnitId())
                                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    if (snapshot.exists()) {
                                                        var planUnit2:PlanUnit? = null
                                                        for (postSnapshot in snapshot.children) {
                                                            val planUnitDataList2 = postSnapshot.getValue(PlanUnit::class.java)
                                                            if (planUnitDataList2?.getStatus() == "todo" || planUnitDataList2?.getStatus() == "CrossLoading") {
                                                                planUnit2 = planUnitDataList2
                                                            }
                                                        }
                                                        val usersRefrence2 =
                                                            FirebaseDatabase.getInstance().reference.child("Users")
                                                                .child(planUnit2?.getSopirId().toString())

                                                        usersRefrence2.addValueEventListener(object : ValueEventListener {
                                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                                if (snapshot.exists()) {
                                                                    val user: Users? = snapshot.getValue(Users::class.java)
                                                                    llProgressBar.preload.visibility = View.GONE
                                                                    llProgressBar.preload.visibility = View.GONE
                                                                    mAdapterCallback.onDetail(
                                                                        bindingAdapterPosition,
                                                                        mData,
                                                                        user,
                                                                        null
                                                                    )
                                                                }
                                                                else{
                                                                    llProgressBar.preload.visibility = View.GONE
                                                                    Toast.makeText(mContext, "Something Wrong!", Toast.LENGTH_LONG).show()
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

                                override fun onCancelled(error: DatabaseError) {
                                }

                            })
                        }
                        else{
                            llProgressBar.preload.visibility = View.GONE
                            Toast.makeText(mContext, "Hauler Alreary Exists!", Toast.LENGTH_LONG).show()
                        }
                    }
                    if (unit.equals("EXCAVATOR")) {
                        var dataTask:TaskGroupList? = null
                        for (postSnapshot in snapshot.children) {
                            val planUnitDataList = postSnapshot.getValue(TaskGroupList::class.java)
                            if (planUnitDataList?.getPengawasId() == userPengawas.getUid() || planUnitDataList?.getStatus() == "doing") {
                                dataTask = planUnitDataList
                            }
                        }
                        if (dataTask?.getPengawasId() != userPengawas.getUid()) {
                            val usersRefrence = FirebaseDatabase.getInstance().reference
                                .child("Users")
                                .child(dataTask?.getOperatorId().toString())
                            usersRefrence.addValueEventListener(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        val userOperator: Users? =
                                            snapshot.getValue(Users::class.java)
                                        llProgressBar.preload.visibility = View.GONE
                                        mAdapterCallback.onDetail(
                                            bindingAdapterPosition,
                                            mData,
                                            userOperator,
                                            dataTask
                                        )
                                    }
                                    else {
                                        llProgressBar.preload.visibility = View.GONE
                                        mAdapterCallback.onDetail(
                                            bindingAdapterPosition,
                                            mData,
                                            planUnitDataList = null,
                                            dataTask
                                        )
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                }

                            })
                        }
                        else{
                            llProgressBar.preload.visibility = View.GONE
                            Toast.makeText(mContext, "Excavator Alreary Exists!", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                else {
//                    llProgressBar.preload.visibility = View.GONE
//                    Toast.makeText(mContext, "No Exists", Toast.LENGTH_LONG).show()
                    FirebaseDatabase.getInstance().reference.child("TasksFleetPlanUnit")
                        .orderByChild("taskUnitId")
                        .equalTo(mData[bindingAdapterPosition].getUnitId())
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    var planUnit2:PlanUnit? = null
                                    for (postSnapshot in snapshot.children) {
                                        val planUnitDataList2 = postSnapshot.getValue(PlanUnit::class.java)
                                        if (planUnitDataList2?.getStatus() == "todo") {
                                            planUnit2 = planUnitDataList2
                                        }
                                    }
                                    val usersRefrence2 =
                                        FirebaseDatabase.getInstance().reference.child("Users")
                                            .child(planUnit2?.getSopirId().toString())

                                    usersRefrence2.addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (snapshot.exists()) {
                                                val user: Users? = snapshot.getValue(Users::class.java)
                                                llProgressBar.preload.visibility = View.GONE
                                                llProgressBar.preload.visibility = View.GONE
                                                mAdapterCallback.onDetail(
                                                    bindingAdapterPosition,
                                                    mData,
                                                    user,
                                                    null
                                                )
                                            }
                                            else{
                                                llProgressBar.preload.visibility = View.GONE
                                                Toast.makeText(mContext, "Something Wrong!", Toast.LENGTH_LONG).show()
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {

                                        }

                                    })
                                }
                                else{
                                    llProgressBar.preload.visibility = View.GONE
                                    Toast.makeText(mContext, "$unit Alreary Exists!", Toast.LENGTH_LONG).show()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })

                }
            }

            override fun onCancelled(error: DatabaseError) {
                llProgressBar.preload.visibility = View.GONE
                Toast.makeText(mContext, "error", Toast.LENGTH_LONG).show()
            }

        })
    }
}