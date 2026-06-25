package com.smj.app.ui.main.view.fragment

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smj.app.databinding.FragmentFleetBinding
import com.smj.app.helper.FormatNumber
import com.smj.app.ui.auth.model.Users
import com.smj.app.ui.fleet.adapter.UnitAdapter
import com.smj.app.ui.fleet.model.UnitList
import com.smj.app.ui.fleet.view.AddFleetActivity
import com.smj.app.ui.fleet.view.EditProductActivity
import com.smj.app.ui.fleet.viewModel.ProductsViewModel
import com.smj.app.utils.response.BaseResponseFirebase

class FleetFragment : Fragment(), UnitAdapter.UnitAdapterCallback {

    private lateinit var binding: FragmentFleetBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var recyclerViewUserList: RecyclerView
    private lateinit var recyclerViewProduct: RecyclerView
    private var firebaseUser: FirebaseUser? = null
    private var formatNumber: FormatNumber? = null
    private val productsViewModel by viewModels<ProductsViewModel>()

    private var context: Context? = null

    private var productList: ArrayList<UnitList>? = null
    private var userList: ArrayList<Users>? = null
    private var shareUserList: ArrayList<Users>? = null

    lateinit var dialogView: View

    //start Unit Type
    private lateinit var selectedUnitType: String
    private var selectedUnitTypeIndex: Int = 0
    private val unit_type = arrayOf(
        "All Units",
        "ARTICULATED DUMP TRUCK",
        "BULLDOZER",
        "COMPACTOR",
        "DRILL MACHINE",
        "DUMP TRUCK",
        "EXCAVATOR",
        "FORKLIFT",
        "FUEL TRUCK",
        "LIGHT VEHICLE",
        "LUBE TRUCK",
        "MANHAUL",
        "MOTOR GRADER",
        "RIGID TRUCK",
        "ROUGH TERRAIN CRANE",
        "WATER TRUCK"
    )

    private val addFleetResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if (result.resultCode == RESULT_OK){
            binding.llProgressBar.preload.visibility = View.GONE
        }
        else{
            binding.llProgressBar.preload.visibility = View.GONE
        }
    }

    private val detailFleetResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if (result.resultCode == RESULT_OK){
            binding.llProgressBar.preload.visibility = View.GONE
        }
        else{
            binding.llProgressBar.preload.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFleetBinding.inflate(layoutInflater)
        context = requireContext().applicationContext
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser
        formatNumber = FormatNumber()

        FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val user: Users? = p0.getValue(Users::class.java)

                        if (context != null) {
                            if (user?.getPosition() == "ADMIN" || user?.getPosition() == "ADMIN RM" || user?.getPosition() == "root") {
                                binding.llAddFleet.visibility = View.VISIBLE
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        binding.addUnit.setOnClickListener {
            binding.llProgressBar.preload.visibility = View.VISIBLE
            val intent = Intent(requireContext(), AddFleetActivity::class.java)
            intent.putExtra("fragmentToLoad", "FleetFragment")
            addFleetResult.launch(intent)
        }

        binding.etSearch.queryHint = "Search"
        binding.etSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val filter = binding.tvFilter.text.toString()
                unitsListShow(query.toString(), filter)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filter = binding.tvFilter.text.toString()
                unitsListShow(newText.toString(), filter)
                return true
            }

        })

        setDefaultFilter()

        recyclerViewProduct = binding.recyclerViewProductList
        recyclerViewProduct.setHasFixedSize(true)
        recyclerViewProduct.layoutManager = LinearLayoutManager(context)


        productList = ArrayList()
        userList = ArrayList()
        shareUserList = ArrayList()

        productsViewModel.fDbProductsResult?.observe(requireActivity()){
            when (it) {
                is BaseResponseFirebase.ProductShowSuccess -> {
                    (productList as ArrayList).clear()
                    if(isAdded) {
                        if (it.value?.isNotEmpty() == true) {
                            binding.shimmerViewContainer.visibility = View.GONE
                            recyclerViewProduct.visibility = View.VISIBLE
                            binding.llEmpty.visibility = View.GONE
                            val filterBy = binding.tvFilter.text.toString()
                            for (data in it.value) {
                                (productList as ArrayList).add(data)
//                                if (filterBy == "All Product") {
//                                    binding.tvFilter.text = "All Product"
//                                    (productList as ArrayList).add(data)
//                                } else if (filterBy == "My Product") {
//                                    binding.tvFilter.text = "My Product"
//                                    if (data.getUid() == firebaseUser!!.uid) {
//                                        (productList as ArrayList).add(data)
//                                    }
//                                } else if (filterBy == "Member") {
//                                    binding.tvFilter.text = "Member"
//                                    if (data.getUid() != firebaseUser!!.uid) {
//                                        (productList as ArrayList).add(data)
//                                    }
//                                }
                            }

                            val pengawas = ""
                            val productAdapter = UnitAdapter(
                                requireContext(),
                                productList!!,
                                this@FleetFragment,
                                pengawas,
                                binding.tvFilter.text.toString(),
                                binding.llProgressBar
                            )
                            recyclerViewProduct.layoutManager = LinearLayoutManager(activity)
                            recyclerViewProduct.adapter = productAdapter
                        } else {
                            binding.shimmerViewContainer.visibility = View.GONE
                            recyclerViewProduct.visibility = View.GONE
                            binding.llEmpty.visibility = View.VISIBLE
                        }
                    }
                }
                is BaseResponseFirebase.Failed -> {
                    if(isAdded) {
                        binding.shimmerViewContainer.visibility = View.GONE
                        recyclerViewProduct.visibility = View.GONE
                        binding.llEmpty.visibility = View.VISIBLE
                    }
                }
                else -> {
                    if(isAdded) {
                        binding.shimmerViewContainer.visibility = View.GONE
                        recyclerViewProduct.visibility = View.GONE
                        binding.llEmpty.visibility = View.VISIBLE
                    }
                }
            }
        }

        binding.tvFilter.setOnClickListener {
            getUnitType()
        }

        return binding.root
    }

    private fun getUnitType() {
        selectedUnitType = unit_type[selectedUnitTypeIndex]
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Unit Type")
            .setSingleChoiceItems(unit_type, selectedUnitTypeIndex) { _, which ->
                selectedUnitTypeIndex = which
                selectedUnitType = unit_type[which]
            }
            .setPositiveButton("Choice") { _, _ ->
                val mapData = HashMap<String, Any>()
                mapData["source"] = "$selectedUnitType"
                binding.tvFilter.text = "$selectedUnitType"
                val filter = "$selectedUnitType"
                val newText = binding.etSearch.query.toString()
                unitsListShow(newText, filter)
            }
            .show()
    }

    private fun setDefaultFilter() {
        binding.tvFilter.text = "All Units"

        val filter = binding.tvFilter.text.toString()
        val newText = binding.etSearch.query.toString()
        unitsListShow(newText, filter)
//        val refFilter = FirebaseDatabase.getInstance().reference
//            .child("SettingUpProduct")
//            .child(firebaseUser!!.uid)
//        refFilter.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if(snapshot.exists()) {
//                    binding.tvFilter.text = snapshot.child("filterBy").value.toString()
//
//                    val newText = binding.etSearch.query.toString()
//                    unitsListShow(newText, filter)
//                }
//                else{
//                    binding.tvFilter.text = "All Product"
//                    val newText = ""
//                    unitsListShow(newText, filter)
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                binding.tvFilter.text = "All Product"
//            }
//        })
    }

    private fun unitsListShow(search: String, filter: String) {
        if(search.isNotEmpty()){
            if (filter != "All Units") {
                val refProduct = FirebaseDatabase.getInstance().reference
                    .child("Units")
                    .orderByChild("unitType")
                    .equalTo(filter)
                productsViewModel.search(refProduct, search)
            }
            else {
                val refProduct = FirebaseDatabase.getInstance().reference
                    .child("Units")
                productsViewModel.search(refProduct, search)
            }
        }
        else{
            if (filter != "All Units") {
                val refProduct = FirebaseDatabase.getInstance().reference
                    .child("Units")
                    .orderByChild("unitType")
                    .equalTo(filter)
                productsViewModel.show(refProduct)
            }
            else{
                val refProduct = FirebaseDatabase.getInstance().reference
                    .child("Units")
                productsViewModel.show(refProduct)
            }
        }
    }

    companion object {
    }

    override fun onDetail(adapterPosition: Int, mData: ArrayList<UnitList>) {
        FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val user: Users? = p0.getValue(Users::class.java)

                        if (context != null) {
                            if (user?.getPosition() == "ADMIN" || user?.getPosition() == "ADMIN RM" || user?.getPosition() == "root") {
                                binding.llProgressBar.preload.visibility = View.VISIBLE
                                val intent = Intent(requireContext(), EditProductActivity::class.java)
                                intent.putExtra("fragmentToLoad", "FleetFragment")
                                intent.putExtra("uid", mData[adapterPosition].getUid())
                                intent.putExtra("unitId", mData[adapterPosition].getUnitId())
                                detailFleetResult.launch(intent)
                            }
                            else{
                                binding.llProgressBar.preload.visibility = View.GONE
                                val alertBuilder = android.app.AlertDialog.Builder(requireContext())
                                alertBuilder.setTitle("Akses dibatasi")
                                alertBuilder.setMessage("Anda tidak memiliki akses!")
                                alertBuilder.setCancelable(false)
                                alertBuilder.setPositiveButton("OK"){_,_ ->

                                }
                                alertBuilder.show()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}