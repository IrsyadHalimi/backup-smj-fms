package com.smj.app.ui.contact.model

class CompanyList {
    private var id: Long = counter++
    private var companyId: String? = ""
    private var companyName: String? = ""
    private var companyPhone: String? = ""
    private var companyAddress: String? = ""
    private var companyState: String? = ""
    private var companyCity: String? = ""
    private var postalCode: String? = ""
    private var latitude: String? = ""
    private var longitude: String? = ""

    constructor()

    constructor(
        id: Long,
        companyId: String,
        companyName: String,
        companyPhone: String,
        companyAddress: String,
        companyState: String,
        companyCity: String,
        postalCode: String,
        latitude: String,
        longitude: String
    ) {
        this.id = id
        this.companyId = companyId
        this.companyName = companyName
        this.companyPhone = companyPhone
        this.companyAddress = companyAddress
        this.companyState = companyState
        this.companyCity = companyCity
        this.postalCode = postalCode
        this.latitude = latitude
        this.longitude = longitude
    }

    fun getId(): Long {
        return id
    }

    fun getCompanyId(): String? {
        return companyId
    }

    fun getCompanyName(): String? {
        return companyName
    }

    fun getCompanyPhone(): String? {
        return companyPhone
    }

    fun getCompanyAddress(): String? {
        return companyAddress
    }

    fun getCompanyState(): String? {
        return companyState
    }

    fun getCompanyCity(): String? {
        return companyCity
    }

    fun getPostalCode(): String? {
        return postalCode
    }

    fun getLatitude(): String? {
        return latitude
    }

    fun getLongitude(): String? {
        return longitude
    }

    companion object {
        private var counter = 0L
    }
}