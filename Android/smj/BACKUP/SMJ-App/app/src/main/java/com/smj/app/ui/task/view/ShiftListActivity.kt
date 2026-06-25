package com.smj.app.ui.task.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smj.app.R
import com.smj.app.databinding.ActivityShiftListBinding
import com.smj.app.helper.Helper
import com.smj.app.ui.settings.adapter.ShiftAdapter
import com.smj.app.ui.settings.model.ShiftList

class ShiftListActivity : AppCompatActivity(), ShiftAdapter.ShiftAdapterCallback {
    private lateinit var binding: ActivityShiftListBinding
    private lateinit var recyclerViewShift: RecyclerView
    private var firebaseUser: FirebaseUser? = null

    private var shiftList: ArrayList<ShiftList>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShiftListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseUser = FirebaseAuth.getInstance().currentUser

        recyclerViewShift = binding.recyclerViewShiftlist
        recyclerViewShift.setHasFixedSize(true)
        recyclerViewShift.layoutManager = LinearLayoutManager(this)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        toolbar.visibility = View.VISIBLE
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        supportActionBar?.setDisplayShowTitleEnabled(false)

        this.let {
            ContextCompat.getColor(
                it,
                com.google.android.material.R.color.mtrl_btn_transparent_bg_color
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
                R.color.white
            )
        }.let {
            Helper().changeStatusNavColor(
                it, true,
                this
            )
        }

        shiftList = ArrayList()

        val refShift = FirebaseDatabase.getInstance().reference.child("Shift")
        refShift.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (shiftList as ArrayList).clear()
                if(snapshot.exists()){
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewShift.visibility = View.VISIBLE
                    binding.llEmpty.visibility = View.GONE
                    for (dataSnapshot in snapshot.children) {
                        val shift = dataSnapshot.getValue(ShiftList::class.java)
                        (shiftList as ArrayList).add(shift!!)
                    }
                    val ShiftAdapter = ShiftAdapter(this@ShiftListActivity, shiftList!!, this@ShiftListActivity)
                    recyclerViewShift.layoutManager = LinearLayoutManager(this@ShiftListActivity)
                    recyclerViewShift.adapter = ShiftAdapter
                }
                else{
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewShift.visibility = View.GONE
                    binding.llEmpty.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    override fun onDetail(adapterPosition: Int, mData: ArrayList<ShiftList>) {
        val intent = Intent()
        intent.putExtra("shiftName", mData[adapterPosition].getShiftName())
        intent.putExtra("id", mData[adapterPosition].getId())
        intent.putExtra("shiftId", mData[adapterPosition].getShiftId())
        intent.putExtra("shiftTime", mData[adapterPosition].getShiftTimeStart()+"-"+mData[adapterPosition].getshiftTimeEnd())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}