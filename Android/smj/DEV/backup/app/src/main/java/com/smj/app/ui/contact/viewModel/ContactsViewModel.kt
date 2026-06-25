package com.smj.app.ui.contact.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.smj.app.ui.contact.model.ContactList
import com.smj.app.utils.response.BaseResponseFirebase
import kotlinx.coroutines.launch

class ContactsViewModel(application: Application) : AndroidViewModel(application) {
    private var contactsList: ArrayList<ContactList>? = null
    var fDbContactsResult: MutableLiveData<BaseResponseFirebase<ContactList>>? = MutableLiveData()

    fun show(
        refContact: Query
    ) {
        viewModelScope.launch {
            try {
                contactsList = ArrayList()
                refContact.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        (contactsList as ArrayList).clear()
                        if(snapshot.exists()) {
                            for (dataSnapshot in snapshot.children) {
                                Log.i("FirstName", dataSnapshot.child("firstName").value.toString())
                                val contact = dataSnapshot.getValue(ContactList::class.java)
                                (contactsList as ArrayList).add(contact!!)
                            }
                            contactsList?.sortByDescending { it.getPosition() }
                            fDbContactsResult?.value = BaseResponseFirebase.UserShowSuccess(contactsList)
                        }
                        else{
                            fDbContactsResult?.value = BaseResponseFirebase.Failed(null)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        fDbContactsResult?.value = BaseResponseFirebase.Failed(null)
                    }

                })
            }
            catch (ex: Exception) {
                Log.i("Ex.ERROR",ex.message.toString())
            }
        }
    }

    fun search(
        refContact: Query,
        search: String
    ) {
        viewModelScope.launch {
            try {
                contactsList = ArrayList()
                refContact
                    .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        (contactsList as ArrayList).clear()
                        if(snapshot.exists()) {
                            for (dataSnapshot in snapshot.children) {
                                val contact = dataSnapshot.getValue(ContactList::class.java)
                                if(contact?.getFullName()?.lowercase()?.startsWith(search.lowercase()) == true
                                    || contact?.getFullName()?.lowercase()?.endsWith(search.lowercase()) == true
                                    || contact?.getFullName()?.lowercase()?.contains(search.lowercase()) == true
                                ) {
                                    (contactsList as ArrayList).add(contact)
                                }
                            }
                            contactsList?.sortByDescending { it.getCreateDate() }
                            fDbContactsResult?.value = BaseResponseFirebase.UserShowSuccess(contactsList)
                        }
                        else{
                            Log.i("FirstName", "ERROR")
                            fDbContactsResult?.value = BaseResponseFirebase.Failed(null)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        fDbContactsResult?.value = BaseResponseFirebase.Failed(null)
                    }

                })
            }
            catch (ex: Exception) {
                Log.i("Ex.ERROR",ex.message.toString())
            }
        }
    }
}