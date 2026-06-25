package com.smj.app.ui.fleet.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.smj.app.ui.fleet.model.UnitList
import com.smj.app.utils.response.BaseResponseFirebase
import kotlinx.coroutines.launch

class ProductsViewModel(application: Application) : AndroidViewModel(application) {
    private var productsList: ArrayList<UnitList>? = null
    var fDbProductsResult: MutableLiveData<BaseResponseFirebase<UnitList>>? = MutableLiveData()

    fun search(refProduct: Query, search: String) {
        viewModelScope.launch {
            try {
                productsList = ArrayList()
                refProduct.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        (productsList as ArrayList).clear()
                        if(snapshot.exists()) {
                            for (dataSnapshot in snapshot.children) {
                                val unit = dataSnapshot.getValue(UnitList::class.java)
                                if(unit?.getUnitCode()?.lowercase()?.startsWith(search.lowercase()) == true
                                    || unit?.getUnitCode()?.lowercase()?.endsWith(search.lowercase()) == true
                                    || unit?.getUnitCode()?.lowercase()?.contains(search.lowercase()) == true
                                ) {
                                    (productsList as ArrayList).add(unit)
                                }
                            }
                            productsList?.sortByDescending { it.getCreateDate() }
                            fDbProductsResult?.value = BaseResponseFirebase.ProductShowSuccess(productsList)
                        }
                        else{
                            fDbProductsResult?.value = BaseResponseFirebase.Failed(null)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        fDbProductsResult?.value = BaseResponseFirebase.Failed(null)
                    }

                })
            }
            catch (ex: Exception) {
                Log.i("Ex.ERROR",ex.message.toString())
            }
        }
    }

    fun show(refProduct: Query) {
        viewModelScope.launch {
            try {
                productsList = ArrayList()
                refProduct.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        (productsList as ArrayList).clear()
                        if(snapshot.exists()) {
                            for (dataSnapshot in snapshot.children) {
                                val products = dataSnapshot.getValue(UnitList::class.java)
                                (productsList as ArrayList).add(products!!)
                            }
                            productsList?.sortByDescending { it.getCreateDate() }
                            fDbProductsResult?.value = BaseResponseFirebase.ProductShowSuccess(productsList)
                        }
                        else{
                            fDbProductsResult?.value = BaseResponseFirebase.Failed(null)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        fDbProductsResult?.value = BaseResponseFirebase.Failed(null)
                    }

                })
            }
            catch (ex: Exception) {
                Log.i("Ex.ERROR",ex.message.toString())
            }
        }
    }
}