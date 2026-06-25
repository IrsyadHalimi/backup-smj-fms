package com.smj.app.ui.main.view.adapter

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
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smj.app.R
import com.smj.app.ui.pengawas.model.Ritase
import com.smj.app.ui.task.adapter.TaskRitaseAdapter
import com.smj.app.ui.task.adapter.TaskAdapter
import com.smj.app.ui.task.model.PlanUnit

class TaskPlanDetailAdapter(
    private var mContext: Context,
    private var mData: ArrayList<PlanUnit>,
    tvTotalRitase: TextView,
    adapterCallback: TaskAdapter.TaskAdapterCallback,
    tvTarget: TextView,
    plan: String?
) : RecyclerView.Adapter<TaskPlanDetailAdapter.MyViewHolder>()  {

    private val mAdapterCallback: TaskAdapterCallback
    var ritase: ArrayList<Ritase>? = null
    private var tvTotalRitase: TextView
    private var tvTarget: TextView
    private var plan: String
    var count = 0
    private var different: Long = 0

    init {
        this.ritase = ritase
        this.tvTotalRitase = tvTotalRitase
        this.tvTarget = tvTarget
        this.plan = plan!!
        this.mAdapterCallback = adapterCallback
        this.different
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val v: View =
            LayoutInflater.from(mContext).inflate(R.layout.layout_task_unit, parent, false)
        return MyViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = mData[position]
        holder.tvUnit.text = HtmlCompat.fromHtml("<strong>"+data.getUnitCode()+"</strong><br/><i><small> Hauler</small></i>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        holder.tvSopir.text = HtmlCompat.fromHtml("<strong>"+data.getSopir()+"</strong><br/><i><small> Sopir</small></i>", HtmlCompat.FROM_HTML_MODE_LEGACY)

        if (mData[position].getStatus() == "done") {
            holder.llMenuUnit.visibility = View.GONE
            holder.tvLocked.visibility = View.VISIBLE
            holder.tvLocked.text = "Stopped"
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
            holder.tvCtRite
        )
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvUnit: TextView = itemView.findViewById(R.id.tv_unit)
        var tvSopir: TextView = itemView.findViewById(R.id.tv_sopir)
        var tvCircleTime: TextView = itemView.findViewById(R.id.tv_circle_time)
        var headerRitase: LinearLayout = itemView.findViewById(R.id.ll_header_ritase)
        var recyclerDetailRitaseList: RecyclerView = itemView.findViewById(R.id.recycler_detail_ritase_list)
        var llRecyclerDetailRitaseList: LinearLayout = itemView.findViewById(R.id.ll_ritase_list)
        var tvCount: TextView = itemView.findViewById(R.id.tv_count)
        var tvCtRite: TextView = itemView.findViewById(R.id.tv_ct_rite)
        var tvHide: TextView = itemView.findViewById(R.id.tv_hide)
        var tvShow: TextView = itemView.findViewById(R.id.tv_show)
        var llMenuUnit: LinearLayout = itemView.findViewById(R.id.ll_menu_unit)
        var tvUndo: TextView = itemView.findViewById(R.id.tv_undo)
        var phone: ImageView = itemView.findViewById(R.id.phone_sopir)
        var whatsapp: ImageView = itemView.findViewById(R.id.whatsapp_sopir)
        var tvLocked: TextView = itemView.findViewById(R.id.tv_locked)
        var llSosial2: LinearLayout = itemView.findViewById(R.id.ll_sosial_2)

        init {
            llMenuUnit.setOnClickListener { v ->
                mAdapterCallback.llMenuUnit(
                    bindingAdapterPosition,
                    mData,
                    v
                )
            }
            phone.setOnClickListener { v ->
                mAdapterCallback.phone(
                    bindingAdapterPosition,
                    mData,
                    v
                )
            }
            whatsapp.setOnClickListener { v ->
                mAdapterCallback.whatsapp(
                    bindingAdapterPosition,
                    mData,
                    v
                )
            }
            tvUnit.setOnClickListener {
                mAdapterCallback.tvUnit(tvSopir, tvUnit, llSosial2)
            }
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
        tvCtRite: TextView
    ) {
        val refShareContact = FirebaseDatabase.getInstance().reference

        refShareContact.child("Ritase")
            .child(mData[position].getTaskId().toString())
            .child(mData[position].getTaskUnitId().toString())
            .addValueEventListener(object : ValueEventListener {
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
                            }
                        }
                        val planUnitAdapter = TaskRitaseAdapter(
                            mContext,
                            ritase,
                            tvCtRite,
                            mData[position].getTaskId().toString(),
                            mAdapterCallback
                        )
                        ritase.sortByDescending { it.getUnitId() }
                        recyclerDetailRitaseList.layoutManager = GridLayoutManager(mContext, 1, GridLayoutManager.VERTICAL, false)
                        recyclerDetailRitaseList.adapter = planUnitAdapter

                    }
                    else{
                        headerRitase.visibility = View.GONE
                        recyclerDetailRitaseList.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    recyclerDetailRitaseList.visibility = View.GONE
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

    interface TaskAdapterCallback {
        fun llMenuUnit(
            bindingAdapterPosition: Int,
            mData: ArrayList<PlanUnit>,
            v: View
        )
        fun phone(
            bindingAdapterPosition: Int,
            mData: ArrayList<PlanUnit>,
            v: View
        )
        fun whatsapp(
            bindingAdapterPosition: Int,
            mData: ArrayList<PlanUnit>,
            v: View
        )
        fun tvUnit(tvSopir: TextView, tvUnit: TextView, llSosial2: LinearLayout)
        fun showFormRemark(
            bindingAdapterPosition: Int,
            mData: java.util.ArrayList<Ritase>,
            v: View?,
            tvRemark: TextView
        )
    }
}