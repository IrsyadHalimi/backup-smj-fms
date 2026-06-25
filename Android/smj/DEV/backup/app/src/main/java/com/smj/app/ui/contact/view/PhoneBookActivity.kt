package com.smj.app.ui.contact.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.smj.app.R
import com.smj.app.databinding.ActivityPhoneBookBinding
import com.smj.app.helper.Helper
import com.smj.app.ui.contact.adapter.PhoneBookAdapter
import com.smj.app.ui.contact.model.PhoneSpot


class PhoneBookActivity : AppCompatActivity(), PhoneBookAdapter.PhoneBookAdapterCallback {

    private lateinit var binding: ActivityPhoneBookBinding
    private lateinit var recyclerViewPhoneBook: RecyclerView
    private var firebaseUser: FirebaseUser? = null

    var phoneBookAdapter: PhoneBookAdapter? = null
    var searchText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseUser = FirebaseAuth.getInstance().currentUser

        recyclerViewPhoneBook = binding.recyclerViewPhoneBookList
        recyclerViewPhoneBook.setHasFixedSize(true)
        recyclerViewPhoneBook.layoutManager = LinearLayoutManager(this)

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

        if(getNamePhoneDetails(searchText).isNotEmpty()){
            recyclerViewPhoneBook.visibility = View.VISIBLE
            phoneBookAdapter = PhoneBookAdapter(this@PhoneBookActivity, getNamePhoneDetails(
                searchText
            ), this@PhoneBookActivity)
            recyclerViewPhoneBook.layoutManager = LinearLayoutManager(this@PhoneBookActivity)
            recyclerViewPhoneBook.adapter = phoneBookAdapter
        }

        binding.etSearch.queryHint = "Search"
        binding.etSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchText = query.toString()
                phoneBookAdapter = PhoneBookAdapter(this@PhoneBookActivity, getNamePhoneDetails(
                    searchText
                ), this@PhoneBookActivity)
                recyclerViewPhoneBook.layoutManager = LinearLayoutManager(this@PhoneBookActivity)
                recyclerViewPhoneBook.adapter = phoneBookAdapter
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchText = newText.toString()
                phoneBookAdapter = PhoneBookAdapter(this@PhoneBookActivity, getNamePhoneDetails(
                    searchText
                ), this@PhoneBookActivity)
                recyclerViewPhoneBook.layoutManager = LinearLayoutManager(this@PhoneBookActivity)
                recyclerViewPhoneBook.adapter = phoneBookAdapter
                return true
            }

        })

    }

    @SuppressLint("Range")
    fun getNamePhoneDetails(searchText: String): ArrayList<PhoneSpot>{
        val names = ArrayList<PhoneSpot>()
        val cr = contentResolver
        val selectionFields = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE '%" + searchText + "%'"
        val cur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
            selectionFields, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC")

        val normalizedNumbers: HashSet<String> = HashSet()
        if (cur!!.count > 0) {
            while (cur.moveToNext()) {
                val id = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NAME_RAW_CONTACT_ID))
                val name = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER))
                if(normalizedNumbers.add(number)) {
                    names.add(PhoneSpot(id, name, number))
                }
            }
        }
        return names
    }

    override fun onDetail(bindingAdapterPosition: Int, mData: ArrayList<PhoneSpot>) {
        val intent = Intent()
        intent.putExtra("name", mData[bindingAdapterPosition].name)
        intent.putExtra("number", mData[bindingAdapterPosition].number)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}