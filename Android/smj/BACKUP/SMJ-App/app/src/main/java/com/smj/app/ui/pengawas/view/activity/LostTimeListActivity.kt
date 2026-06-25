package com.smj.app.ui.pengawas.view.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.smj.app.R
import com.smj.app.databinding.ActivityLostTimeListBinding
import com.smj.app.helper.Helper
import com.smj.app.ui.pengawas.view.adapter.LostTimeAdapter
import com.smj.app.ui.settings.model.LabelingLostTimeList

class LostTimeListActivity : AppCompatActivity(), LostTimeAdapter.LostTimeAdapterCallback {

    private lateinit var binding: ActivityLostTimeListBinding
    private lateinit var recyclerViewLostTime: RecyclerView
    private var firebaseUser: FirebaseUser? = null

    private var lostTimeList: ArrayList<LabelingLostTimeList>? = null

    //start Unit Type
    private lateinit var selectedFilter: String
    private var selectedFilterIndex: Int = 0
    private val filter = arrayOf(
        "All Lost Time",
        "General",
        "Kerusakan Unit",
        "Kondisi"
    )

    @SuppressLint("PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLostTimeListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val position = intent.getStringExtra("position")

        binding.tvTitle.text = position?.toCapitalizeEachWord()

        recyclerViewLostTime = binding.recyclerViewLostTimeList
        recyclerViewLostTime.setHasFixedSize(true)
        recyclerViewLostTime.layoutManager = LinearLayoutManager(this)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        toolbar.visibility = View.VISIBLE
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
        toolbar.setTitle(position)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.title = position

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

        binding.etSearch.queryHint = "Search"
        binding.etSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val filter = binding.tvFilter.text.toString()
                showDataSearch(query.toString(), filter)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filter = binding.tvFilter.text.toString()
                showDataSearch(newText.toString(), filter)
                return true
            }

        })

        lostTimeList = ArrayList()

        binding.tvFilter.setOnClickListener {
            getFilterLostTime()
        }

        showData()
    }

    private fun getFilterLostTime() {
        selectedFilter = filter[selectedFilterIndex]
        MaterialAlertDialogBuilder(this)
            .setTitle("Select Unit Type")
            .setSingleChoiceItems(filter, selectedFilterIndex) { _, which ->
                selectedFilterIndex = which
                selectedFilter = filter[which]
            }
            .setPositiveButton("Choice") { _, _ ->
                val mapData = HashMap<String, Any>()
                mapData["source"] = "$selectedFilter"
                binding.tvFilter.text = "$selectedFilter"
                val filter = "$selectedFilter"
                val newText = binding.etSearch.query.toString()
                if (filter != "All Lost Time") {
                    showDataSearch(newText, filter)
                }
                else {
                    showData()
                }
            }
            .show()
    }

    private fun showData() {
        val refContact = FirebaseDatabase.getInstance().reference.child("LabelingLostTime")
        refContact.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (lostTimeList as ArrayList).clear()
                if(snapshot.exists()){
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewLostTime.visibility = View.VISIBLE
                    binding.llEmpty.visibility = View.GONE
                    for (dataSnapshot in snapshot.children) {
                        val lostTime = dataSnapshot.getValue(LabelingLostTimeList::class.java)
                        (lostTimeList as ArrayList).add(lostTime!!)
                    }
                    val contactAdapter = LostTimeAdapter(this@LostTimeListActivity, lostTimeList!!, this@LostTimeListActivity)
                    recyclerViewLostTime.layoutManager = LinearLayoutManager(this@LostTimeListActivity)
                    recyclerViewLostTime.adapter = contactAdapter
                }
                else{
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewLostTime.visibility = View.GONE
                    binding.llEmpty.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun showDataSearch(search: String, filter: String?) {
        var refContact: Query? = null
        if (filter != "All Lost Time") {
            refContact = FirebaseDatabase.getInstance().reference.child("LabelingLostTime")
                .orderByChild("labelFilter")
                .equalTo(filter)
        }
        else{
            refContact = FirebaseDatabase.getInstance().reference.child("LabelingLostTime")
        }
        refContact.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (lostTimeList as ArrayList).clear()
                if(snapshot.exists()){
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewLostTime.visibility = View.VISIBLE
                    binding.llEmpty.visibility = View.GONE
                    for (dataSnapshot in snapshot.children) {
                        val lostTime = dataSnapshot.getValue(LabelingLostTimeList::class.java)
                        if(lostTime?.getLabelingLostName()?.lowercase()?.startsWith(search.lowercase()) == true
                            || lostTime?.getLabelingLostName()?.lowercase()?.endsWith(search.lowercase()) == true
                            || lostTime?.getLabelingLostName()?.lowercase()?.contains(search.lowercase()) == true
                        ) {
                            (lostTimeList as ArrayList).add(lostTime)
                        }
                    }
                    val contactAdapter = LostTimeAdapter(this@LostTimeListActivity, lostTimeList!!, this@LostTimeListActivity)
                    recyclerViewLostTime.layoutManager = LinearLayoutManager(this@LostTimeListActivity)
                    recyclerViewLostTime.adapter = contactAdapter
                }
                else{
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewLostTime.visibility = View.GONE
                    binding.llEmpty.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun String.toCapitalizeEachWord() : String{
        return this.split(" ").map {
            if(it.contains("/")){
                it.split("/").map { word -> word.replaceFirstChar { firstChar -> firstChar.uppercase() }
                }.joinToString("/")
            }else{
                it.replaceFirstChar { firstChar -> firstChar.uppercase() }
            }
        }.joinToString(" ") }

    override fun onDetail(
        bindingAdapterPosition: Int,
        mData: ArrayList<LabelingLostTimeList>
    ) {
        val intent = Intent()
        intent.putExtra("lostTimeName", mData[bindingAdapterPosition].getLabelingLostName())
        intent.putExtra("lostTimeId", mData[bindingAdapterPosition].getLabelingLostId())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}