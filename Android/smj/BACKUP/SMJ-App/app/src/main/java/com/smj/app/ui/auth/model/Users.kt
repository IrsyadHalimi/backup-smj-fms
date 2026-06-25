package com.smj.app.ui.auth.model

class Users {
    private var id: Long = 0L
    private var fullName: String? = ""
    private var email: String? = ""
    private var phoneNumber: String? = ""
    private var idNumber: String? = ""
    private var photo: String? = ""
    private var status: String? = ""
    private var gender: String? = ""
    private var birthDay: String? = ""
    private var latitude: String? = ""
    private var longitude: String? = ""
    private var uid: String? = ""
    private var position: String? = ""
    private var createDate: Long? = 0

    constructor()

    constructor(id: Long, fullName: String, email: String, phoneNumber: String, idNumber: String, photo: String, status: String, gender: String, birthDay: String, latitude: String, longitude: String, uid: String, position: String, createDate: Long){
        this.id = id
        this.fullName = fullName
        this.email = email
        this.phoneNumber = phoneNumber
        this.idNumber = idNumber
        this.photo = photo
        this.status = status
        this.gender = gender
        this.birthDay = birthDay
        this.latitude = latitude
        this.longitude = longitude
        this.uid = uid
        this.position = position
        this.createDate = createDate
    }

    fun getId(): Long {
        return id
    }
    fun getFullName(): String?{
        return fullName
    }
    fun getIdNumber(): String?{
        return idNumber
    }
    fun getEmail(): String?{
        return email
    }
    fun getPhoneNumber(): String?{
        return phoneNumber
    }
    fun getPhoto(): String?{
        return photo
    }
    fun getStatus(): String?{
        return status
    }
    fun getGender(): String?{
        return gender
    }
    fun getBirthDay(): String?{
        return birthDay
    }
    fun getUid(): String?{
        return uid
    }
    fun getCreateDate(): Long?{
        return createDate
    }
    fun getLatitude(): String?{
        return latitude
    }
    fun getLongitude(): String?{
        return longitude
    }
    fun getPosition(): String?{
        return position
    }
}