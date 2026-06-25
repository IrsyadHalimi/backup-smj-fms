package com.smj.app.ui.main.view.fragment

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.smj.app.databinding.FragmentContactBinding
import com.smj.app.databinding.LayoutProgressBinding
import com.smj.app.helper.DateHelper
import com.smj.app.helper.Helper
import com.smj.app.helper.NavigationHelper
import com.smj.app.ui.auth.model.Users
import com.smj.app.ui.auth.view.LoginActivity
import com.smj.app.ui.contact.adapter.ContactAdapter
import com.smj.app.ui.contact.adapter.ShareUsersAdapter
import com.smj.app.ui.contact.model.ContactList
import com.smj.app.ui.contact.view.AddContactActivity
import com.smj.app.ui.contact.view.EditContactActivity
import com.smj.app.ui.contact.viewModel.ContactsViewModel
import com.smj.app.utils.response.BaseResponseFirebase


class ContactFragment : Fragment(), ContactAdapter.ContactAdapterCallback, ShareUsersAdapter.ShareUsersAdapterCallback {

    private lateinit var binding: FragmentContactBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var recyclerViewContact: RecyclerView
    private lateinit var databaseReference: DatabaseReference

    private var firebaseUser: FirebaseUser? = null
    private var usersRefrence: DatabaseReference? = null
    private val viewContactsModel by viewModels<ContactsViewModel>()

    private var contactsList: ArrayList<ContactList>? = null
    private var userList: ArrayList<Users>? = null
    private var shareUserList: ArrayList<Users>? = null

    lateinit var dialogView: View

    //start source
    private lateinit var selectedPosition: String
    private var selectedPositionIndex: Int = 0
    private val position = arrayOf(
        "All Employee",
        "ADMIN",
        "ADMIN HR",
        "ADMIN RM",
        "ADMIN MCC",
        "DIREKTUR",
        "GM Produksi",
        "Manager Produksi",
        "SPI Produksi",
        "Jr.SPI Produksi",
        "SPV Produksi",
        "Jr SPV Produksi",
        "Sr Foreman Produksi",
        "Jr Foreman Produksi",
        "Foreman Produksi",
        "MT MCC Produksi",
        "MCC Field",
        "Operator PC 1250",
        "Operator EC 395",
        "Operator PC 750",
        "Operator EC 480",
        "Operator PC 200",
        "Operator CAT 773E",
        "Operator CAT 773E & HD 465",
        "Operator HD 465",
        "Operator ADT",
        "Operator D8 T",
        "Operator DZ 375",
        "Operator DZ 85 SS",
        "Operator GD 705",
        "Operator GD 14 M",
        "Driver Hino 500",
        "Driver WT",
        "Driver Isuzu",
        "Jr. SPI Maintenance Road"
    )

    private val addContactResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
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
        binding = FragmentContactBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser

        FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val user: Users? = p0.getValue(Users::class.java)

                        if (context != null) {
                            if (user?.getPosition() == "ADMIN" || user?.getPosition() == "ADMIN HR" || user?.getPosition() == "root") {
                                binding.llAddContact.visibility = View.VISIBLE
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })


        binding.addContact.setOnClickListener {
            binding.llProgressBar.preload.visibility = View.VISIBLE
            val intent = Intent(requireContext(), AddContactActivity::class.java)
            intent.putExtra("fragmentToLoad", "ContactFragment")
            addContactResult.launch(intent)
        }

        binding.etSearch.queryHint = "Search"
        binding.etSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val filter = binding.tvFilter.text.toString()
                contactsListShow(query.toString(), filter)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filter = binding.tvFilter.text.toString()
                contactsListShow(newText.toString(), filter)
                return true
            }

        })

        setDefaultFilter()

        recyclerViewContact = binding.recyclerViewContactlist
        recyclerViewContact.setHasFixedSize(true)
        recyclerViewContact.layoutManager = LinearLayoutManager(context)

        contactsList = ArrayList()
        userList = ArrayList()
        shareUserList = ArrayList()

        viewContactsModel.fDbContactsResult?.observe(requireActivity()){
            when (it) {
                is BaseResponseFirebase.UserShowSuccess -> {
                    (contactsList as ArrayList).clear()
                    if(isAdded) {
                        if (it.value?.isNotEmpty() == true) {
                            binding.llProgressBar.preload.visibility = View.GONE
                            binding.shimmerViewContainer.visibility = View.GONE
                            recyclerViewContact.visibility = View.VISIBLE
                            binding.llEmpty.visibility = View.GONE
                            for (data in it.value) {
                                if(data.getPosition() != "root") {
                                    (contactsList as ArrayList).add(data)
                                }
                            }

                            val position = binding.tvFilter.text.toString()
                            val forUse = ""
                            checkUserCurrent(
                                requireContext(),
                                contactsList!!,
                                this@ContactFragment,
                                position,
                                forUse,
                                binding.llProgressBar
                            )

                        } else {
                            binding.shimmerViewContainer.visibility = View.GONE
                            recyclerViewContact.visibility = View.GONE
                            binding.llEmpty.visibility = View.VISIBLE
                        }
                    }
                }
                is BaseResponseFirebase.Failed -> {
                    if(isAdded) {
                        binding.shimmerViewContainer.visibility = View.GONE
                        recyclerViewContact.visibility = View.GONE
                        binding.llEmpty.visibility = View.VISIBLE
                    }
                }
                else -> {
                    if(isAdded) {
                        binding.shimmerViewContainer.visibility = View.GONE
                        recyclerViewContact.visibility = View.GONE
                        binding.llEmpty.visibility = View.VISIBLE
                    }
                }
            }
        }

        binding.tvFilter.setOnClickListener {
            getDataPosition()
        }

        binding.tvFilter.setOnFocusChangeListener { _, _ ->
//            Helper().showToast("tes", requireActivity())
        }

        return binding.root
    }



    private fun getDataPosition() {
        selectedPosition = position[selectedPositionIndex]
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select JOB")
            .setSingleChoiceItems(position, selectedPositionIndex) { _, which ->
                selectedPositionIndex = which
                selectedPosition = position[which]
            }
            .setPositiveButton("Choice") { _, _ ->
                val mapData = HashMap<String, Any>()
                mapData["source"] = "$selectedPosition"
                binding.tvFilter.text = "$selectedPosition"
                val filter = "$selectedPosition"
                val newText = binding.etSearch.query.toString()
                contactsListShow(newText, filter)
            }
            .show()
    }

    private fun setDefaultFilter() {
        binding.tvFilter.text = "All Employee"

        val filter = binding.tvFilter.text.toString()
        val newText = binding.etSearch.query.toString()
        contactsListShow(newText, filter)
//        val refFilter = FirebaseDatabase.getInstance().reference
//            .child("SettingUpContact")
//            .child(firebaseUser!!.uid)
//        refFilter.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if(snapshot.exists()) {
//                    binding.tvFilter.text = snapshot.child("filterBy").value.toString()
//
//                    val newText = binding.etSearch.query.toString()
//                    contactsListShow(newText)
//                }
//                else{
//                    binding.tvFilter.text = "All Worker"
//                    val newText = ""
//                    contactsListShow(newText)
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                binding.tvFilter.text = "All Contact"
//            }
//        })
    }

    private fun contactsListShow(search: String, filter: String) {
        if(search.isNotEmpty()){
            if (filter != "All Employee") {
                val refContact = FirebaseDatabase.getInstance().reference
                    .child("Users")
                    .orderByChild("position")
                    .equalTo(filter)
                viewContactsModel.search(refContact, search)
            }
            else{
                val refContact = FirebaseDatabase.getInstance().reference
                    .child("Users")
                viewContactsModel.search(refContact, search)
            }
        }
        else{
            if (filter != "All Employee") {
                val refContact = FirebaseDatabase.getInstance().reference
                    .child("Users")
                    .orderByChild("position")
                    .equalTo(filter)
                viewContactsModel.show(refContact)
            }
            else{
                val refContact = FirebaseDatabase.getInstance().reference
                    .child("Users")
                viewContactsModel.show(refContact)
            }
        }
    }

    companion object {}

    override fun OnDetail(bindingAdapterPosition: Int, mData: ArrayList<ContactList>) {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        val intent = Intent(requireContext(), EditContactActivity::class.java)
        intent.putExtra("fragmentToLoad", "ContactFragment")
        intent.putExtra("uid", mData[bindingAdapterPosition].getUid())
        intent.putExtra("contactId", mData[bindingAdapterPosition].getId())
        intent.putExtra("userKey", mData[bindingAdapterPosition].getUserKey())
        intent.putExtra("firebaseUserID", mData[bindingAdapterPosition].getUid())
        intent.putExtra("createBy", mData[bindingAdapterPosition].getCreateBy())
        intent.putExtra("createDate", mData[bindingAdapterPosition].getCreateDate().toString())
        addContactResult.launch(intent)
    }

    override fun WhatsApp(bindingAdapterPosition: Int, mData: ArrayList<ContactList>) {
        val phone = contactsList?.get(bindingAdapterPosition)?.getPhoneNumber()
        phone?.let { sendMessage(it) }
    }

    override fun Call(bindingAdapterPosition: Int, mData: ArrayList<ContactList>) {
        val phone = contactsList?.get(bindingAdapterPosition)?.getPhoneNumber()
        phone?.let { outGoingCall(it) }
    }

    override fun Email(bindingAdapterPosition: Int, mData: ArrayList<ContactList>) {
        val email = contactsList?.get(bindingAdapterPosition)?.getEmail()
        email?.let { sendEmail(it) }
    }

    override fun Authorized(bindingAdapterPosition: Int, mData: java.util.ArrayList<ContactList>) {
        val refUser = FirebaseDatabase.getInstance().reference
        val userKey = mData[bindingAdapterPosition].getUserKey()

        val hashMap = HashMap<String, Any>()
        hashMap["fullName"] = mData[bindingAdapterPosition].getFullName().toString()
        hashMap["idNumber"] = mData[bindingAdapterPosition].getIdNumber().toString()
        hashMap["email"] = mData[bindingAdapterPosition].getEmail().toString()
        hashMap["phoneNumber"] = mData[bindingAdapterPosition].getPhoneNumber().toString()
        hashMap["photo"] = "https://firebasestorage.googleapis.com/v0/b/smj-app-94dec.appspot.com/o/worker.png?alt=media&token=a5ea2480-6479-4f5b-a32e-93fdd0380afa"
        hashMap["status"] = "active"
        hashMap["position"] = mData[bindingAdapterPosition].getPosition().toString()
        hashMap["gender"] = ""
        hashMap["birthDay"] = ""
        hashMap["latitude"] = ""
        hashMap["longitude"] = ""
        hashMap["userKey"] = userKey!!
        hashMap["uid"] = mData[bindingAdapterPosition].getUid().toString()
        hashMap["createBy"] = mData[bindingAdapterPosition].getCreateBy().toString()
        hashMap["updateBy"] = firebaseAuth.currentUser?.uid.toString()
        hashMap["createDate"] =  mData[bindingAdapterPosition].getCreateDate()!!.toLong()
        hashMap["updateDate"] =  DateHelper().todayTime()

        databaseReference = refUser.child("Users").child(mData[bindingAdapterPosition].getUid().toString())
        databaseReference.updateChildren(hashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Helper().showToast("Update data is Successful!", requireActivity())
//                    binding.llProgressBar.preload.visibility = View.GONE
                    setDefaultFilter()
                }
                else{
//                    binding.llProgressBar.preload.visibility = View.GONE
                    Helper().showToast("Data Product is Something Wrong!!!", requireActivity())
                }
            }
    }

    override fun Unauthorized(
        bindingAdapterPosition: Int,
        mData: java.util.ArrayList<ContactList>
    ) {
        val refUser = FirebaseDatabase.getInstance().reference
        val userKey = mData[bindingAdapterPosition].getUserKey()

        val hashMap = HashMap<String, Any>()
        hashMap["fullName"] = mData[bindingAdapterPosition].getFullName().toString()
        hashMap["idNumber"] = mData[bindingAdapterPosition].getIdNumber().toString()
        hashMap["email"] = mData[bindingAdapterPosition].getEmail().toString()
        hashMap["phoneNumber"] = mData[bindingAdapterPosition].getPhoneNumber().toString()
        hashMap["photo"] = "https://firebasestorage.googleapis.com/v0/b/siapnikah-app.appspot.com/o/profile.png?alt=media&token=9dabafa6-50b0-4472-aa22-a5e06b673674"
        hashMap["status"] = "pending"
        hashMap["position"] = mData[bindingAdapterPosition].getPosition().toString()
        hashMap["gender"] = ""
        hashMap["birthDay"] = ""
        hashMap["latitude"] = ""
        hashMap["longitude"] = ""
        hashMap["userKey"] = userKey!!
        hashMap["uid"] = mData[bindingAdapterPosition].getUid().toString()
        hashMap["createBy"] = mData[bindingAdapterPosition].getCreateBy().toString()
        hashMap["updateBy"] = firebaseAuth.currentUser?.uid.toString()
        hashMap["createDate"] =  mData[bindingAdapterPosition].getCreateDate()!!.toLong()
        hashMap["updateDate"] =  DateHelper().todayTime()

        databaseReference = refUser.child("Users").child(mData[bindingAdapterPosition].getUid().toString())
        databaseReference.updateChildren(hashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if(isAdded){
                        Helper().showToast("Update data is Successful!", requireActivity())
//                        binding.llProgressBar.preload.visibility = View.GONE
                        setDefaultFilter()
                    }
                }
                else{
//                    binding.llProgressBar.preload.visibility = View.GONE
                    Helper().showToast("Data Product is Something Wrong!!!", requireActivity())
                }
            }
    }

    private fun sendEmail(email: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
        }
        startActivity(Intent.createChooser(emailIntent, "Send feedback"))
    }

    private fun outGoingCall(phone: String) {
        val dialIntent = Intent(Intent.ACTION_DIAL)
        dialIntent.data = Uri.parse("tel:$phone")
        startActivity(dialIntent)
    }

    private fun sendMessage(phone: String) {
        val uri = Uri.parse("smsto:$phone")
        val sendIntent = Intent(Intent.ACTION_SENDTO, uri)

        val shareIntent = Intent.createChooser(sendIntent, null)
        NavigationHelper().navigateToActivityCallback(requireActivity(), shareIntent)
    }

    private fun checkUserCurrent(
        requireContext: Context,
        contactsList: ArrayList<ContactList>,
        contactFragment: ContactFragment,
        position: String,
        forUse: String,
        llProgressBar: LayoutProgressBinding
    ) {
        if(firebaseAuth.currentUser?.uid == null){
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            NavigationHelper().navigateToActivityFlags(requireActivity(), intent)
        }
        else{
            firebaseUser = firebaseAuth.currentUser
            usersRefrence = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

            usersRefrence!!.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user: Users? = snapshot.getValue(Users::class.java)
                        val contactAdapter = ContactAdapter(
                            requireContext,
                            contactsList,
                            contactFragment,
                            position,
                            forUse,
                            llProgressBar,
                            user?.getPosition().toString(),
                            ""
                        )
                        recyclerViewContact.layoutManager = LinearLayoutManager(activity)
                        recyclerViewContact.adapter = contactAdapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
    }

//    private fun setUpDefaultContact(dialog: BottomSheetDialog, radioButton: RadioButton) {
//        if (radioButton.text.isNotEmpty()){
//            dialog.dismiss()
//            binding.llProgressBar.preload.visibility = View.VISIBLE
//
//            val refSettingUp = FirebaseDatabase.getInstance().reference
//            val settingUpContact = HashMap<String, Any>()
//            settingUpContact["uid"] = firebaseUser!!.uid
//            settingUpContact["filterBy"] = radioButton.text.toString()
//
//            databaseReference = refSettingUp.child("SettingUpContact").child(firebaseAuth.currentUser!!.uid)
//            databaseReference.updateChildren(settingUpContact)
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful)
//                    {
//                        Helper().showToast("OK!!!", requireActivity())
//                        binding.llProgressBar.preload.visibility = View.GONE
//                        binding.shimmerViewContainer.visibility = View.VISIBLE
//                        recyclerViewContact.visibility = View.GONE
//                        binding.llEmpty.visibility = View.GONE
//                        setDefaultFilter()
//                    }
//                    else{
//                        binding.llProgressBar.preload.visibility = View.GONE
//                        Helper().showToast("Something Wrong!!!", requireActivity())
//                    }
//                }
//        }
//    }

}