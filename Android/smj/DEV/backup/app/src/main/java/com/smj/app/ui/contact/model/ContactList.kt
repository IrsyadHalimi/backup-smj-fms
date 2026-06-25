package com.smj.app.ui.contact.model

class ContactList {
    private var id: Long = counter++
    private var uid: String? = ""
    private var birthDay: String? = ""
    private var email: String? = ""
    private var phoneNumber: String? = ""
    private var fullName: String? = ""
    private var gender: String? = ""
    private var idNumber: String? = ""
    private var latitude: String? = ""
    private var longitude: String? = ""
    private var photo: String? = ""
    private var position: String? = ""
    private var status: String? = ""
    private var userKey: String? = ""
    private var createBy: String? = ""
    private var createDate: Long? = 0

    constructor()

    constructor(
        id: Long,
        uid: String,
        birthDay: String,
        email: String,
        phoneNumber: String,
        fullName: String,
        gender: String,
        idNumber: String,
        latitude: String,
        longitude: String,
        photo: String,
        position: String,
        status: String,
        userKey: String,
        createBy: String,
        createDate: Long,
    ){
        this.id = id
        this.uid = uid
        this.birthDay = birthDay
        this.email = email
        this.phoneNumber = phoneNumber
        this.fullName = fullName
        this.gender = gender
        this.idNumber = idNumber
        this.latitude = latitude
        this.longitude = longitude
        this.photo = photo
        this.status = status
        this.position = position
        this.userKey = userKey
        this.createBy = createBy
        this.createDate = createDate
    }

    fun getId(): Long {
        return id
    }

    fun getBirthDay(): String? {
        return birthDay
    }

    fun getEmail(): String? {
        return email
    }

    fun getPhoneNumber(): String? {
        return phoneNumber
    }

    fun getFullName(): String? {
        return fullName
    }

    fun getGender(): String? {
        return gender
    }

    fun getIdNumber(): String? {
        return idNumber
    }

    fun getLatitude(): String? {
        return latitude
    }

    fun getLongitude(): String? {
        return longitude
    }

    fun getPhoto(): String? {
        return photo
    }

    fun getStatus(): String? {
        return status
    }

    fun getPosition(): String? {
        return position
    }

    fun getUid(): String? {
        return uid
    }

    fun getUserKey(): String? {
        return userKey
    }
    fun getCreateBy(): String? {
        return createBy
    }

    fun getCreateDate(): Long? {
        return createDate
    }

    companion object {
        private var counter = 0L
    }
}