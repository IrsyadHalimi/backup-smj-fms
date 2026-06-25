package com.smj.app.ui.task.view

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
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
import com.smj.app.databinding.ActivityProductListBinding
import com.smj.app.helper.Helper
import com.smj.app.ui.fleet.adapter.UnitAdapter
import com.smj.app.ui.fleet.model.UnitList

class ProductListActivity : AppCompatActivity(), UnitAdapter.UnitAdapterCallback {

    private lateinit var binding: ActivityProductListBinding
    private lateinit var recyclerViewProduct: RecyclerView
    private var firebaseUser: FirebaseUser? = null

    private var productList: ArrayList<UnitList>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val unitType = intent.getStringExtra("unitType")
        val forUse = intent.getStringExtra("forUse")

        binding.tvTitle.text = unitType

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

        recyclerViewProduct = binding.recyclerViewProductList
        recyclerViewProduct.setHasFixedSize(true)
        recyclerViewProduct.layoutManager = LinearLayoutManager(this)

        binding.etSearch.queryHint = "Search"
        binding.etSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                showDataSearch(query.toString(), forUse, unitType)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                showDataSearch(newText.toString(), forUse, unitType)
                return true
            }

        })

        productList = ArrayList()

        val refContact = FirebaseDatabase.getInstance().reference.child("Units")
        refContact.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (productList as ArrayList).clear()
                if(snapshot.exists()){
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewProduct.visibility = View.VISIBLE
                    binding.llEmpty.visibility = View.GONE
                    for (dataSnapshot in snapshot.children) {
                        val product = dataSnapshot.getValue(UnitList::class.java)
                        if (unitType == "DUMP TRUCK" || unitType == "RIGID TRUCK") {
                            if (product?.getUnitType().equals(unitType)
                                || product?.getUnitType().equals("ARTICULATED $unitType")
                                || product?.getUnitType().equals("RIGID TRUCK"))
                            {
                                (productList as ArrayList).add(product!!)
                            }
                        }
                        if (unitType == "EXCAVATOR") {
                            if (product?.getUnitType().equals(unitType))
                            {
                                (productList as ArrayList).add(product!!)
                            }
                        }
                    }
                    val productAdapter = UnitAdapter(this@ProductListActivity, productList!!, this@ProductListActivity, forUse, unitType, binding.llProgressBar)
                    recyclerViewProduct.layoutManager = LinearLayoutManager(this@ProductListActivity)
                    recyclerViewProduct.adapter = productAdapter
                }
                else{
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewProduct.visibility = View.GONE
                    binding.llEmpty.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun showDataSearch(search: String, pengawas: String?, unitType: String?) {
        val refContact = FirebaseDatabase.getInstance().reference.child("Units")
        refContact.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (productList as ArrayList).clear()
                if(snapshot.exists()){
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewProduct.visibility = View.VISIBLE
                    binding.llEmpty.visibility = View.GONE
                    for (dataSnapshot in snapshot.children) {
                        val unit = dataSnapshot.getValue(UnitList::class.java)
                        if (unitType == "DUMP TRUCK" || unitType == "RIGID TRUCK") {
                            if (unit?.getUnitType().equals(unitType)
                                || unit?.getUnitType().equals("ARTICULATED $unitType")
                                || unit?.getUnitType().equals("RIGID TRUCK"))
                            {
                                if(unit?.getUnitCode()?.lowercase()?.startsWith(search.lowercase()) == true
                                    || unit?.getUnitCode()?.lowercase()?.endsWith(search.lowercase()) == true
                                    || unit?.getUnitCode()?.lowercase()?.contains(search.lowercase()) == true
                                ) {
                                    (productList as ArrayList).add(unit)
                                }
                            }
                        }
                        if (unitType == "EXCAVATOR") {
                            if (unit?.getUnitType().equals(unitType))
                            {
                                if(unit?.getUnitCode()?.lowercase()?.startsWith(search.lowercase()) == true
                                    || unit?.getUnitCode()?.lowercase()?.endsWith(search.lowercase()) == true
                                    || unit?.getUnitCode()?.lowercase()?.contains(search.lowercase()) == true
                                ) {
                                    (productList as ArrayList).add(unit)
                                }
                            }
                        }
                    }
                    val productAdapter = UnitAdapter(
                        this@ProductListActivity,
                        productList!!,
                        this@ProductListActivity,
                        pengawas,
                        unitType,
                        binding.llProgressBar
                    )
                    recyclerViewProduct.layoutManager = LinearLayoutManager(this@ProductListActivity)
                    recyclerViewProduct.adapter = productAdapter
                }
                else{
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewProduct.visibility = View.GONE
                    binding.llEmpty.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    override fun onDetail(adapterPosition: Int, unit: ArrayList<UnitList>) {
        val intent = Intent()
        intent.putExtra("unitCode", unit[adapterPosition].getUnitCode())
        intent.putExtra("unitId", unit[adapterPosition].getUnitId())
        intent.putExtra("unitType", unit[adapterPosition].getUnitType())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}