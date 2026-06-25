package com.smj.app.ui.auth.viewModel

import android.app.Application
import android.text.Editable
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.smj.app.ui.auth.model.Users
import com.smj.app.utils.response.BaseResponseFirebase
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private lateinit var userList: ArrayList<Users>
    var firebaseDatabaseUserResult: MutableLiveData<BaseResponseFirebase<Users>>? = MutableLiveData()
    var firebaseDatabaseUserSingleResult: MutableLiveData<BaseResponseFirebase<Users>>? = MutableLiveData()
    var firebaseDatabaseLoginResult: MutableLiveData<BaseResponseFirebase<String>>? = MutableLiveData()

    //FIREBASE
    fun firebaseDatabaseUser(
        firebaseDatabase: FirebaseDatabase,
        child: String,
        path: String,
        uid: String
    ) {
        viewModelScope.launch {
            try {
                userList = ArrayList()
                firebaseDatabase.reference.child(child).orderByChild(path).equalTo(uid).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        userList.clear()
                        if (snapshot.value != null) {
                            for (postSnapshot in snapshot.children) {
                                val currentUser = postSnapshot.getValue(Users::class.java)
                                firebaseDatabaseUserResult?.value = BaseResponseFirebase.Success(currentUser)
                            }
                        }
                        else{
                            firebaseDatabaseUserResult?.value = BaseResponseFirebase.Failed(null)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.i("dataUserError", error.message)
                    }
                })
            }
            catch (ex: Exception) {
                Log.i("Ex.ERROR",ex.message.toString())
            }
        }
    }

    fun login(
        firebaseAuth: FirebaseAuth,
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            try {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if(it.isSuccessful) {
                            firebaseDatabaseLoginResult?.value = BaseResponseFirebase.LoginSuccess(it.isSuccessful.toString())
                        }
                        else{
                            Log.i("LoginFailedTES", it.exception?.message.toString())
                        }
                    }
                    .addOnFailureListener {e ->
                        Log.i("LoginErrorTES", e.message.toString())
                        firebaseDatabaseLoginResult?.value = BaseResponseFirebase.LoginFailure(e.message.toString())
                    }
            }
            catch (ex: Exception) {
                Log.i("Ex.ERROR",ex.message.toString())
            }
        }
    }

    fun checkEmailExists(firebaseDatabase: DatabaseReference, email: Editable?) {
        viewModelScope.launch {
            try {
                firebaseDatabase
                    .child("Users")
                    .orderByChild("email")
                    .equalTo(email.toString())
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()) {
                            Log.i("snapshot", snapshot.value.toString())
                            val user = snapshot.getValue(Users::class.java)
                            firebaseDatabaseUserSingleResult?.value = BaseResponseFirebase.Success(user)

                        }
                        else{
                            firebaseDatabaseUserSingleResult?.value = BaseResponseFirebase.Failed(null)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.i("error", error.message)
                        firebaseDatabaseUserSingleResult?.value = BaseResponseFirebase.Failed(error.message)
                    }
                })
            }
            catch (ex: Exception) {
                Log.i("Ex.ERROR",ex.message.toString())
            }
        }
    }
}