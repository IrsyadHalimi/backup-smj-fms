package com.smj.app.ui.settings.model

class LocationList {
    private var id: Long = 0L
    private var uid: String? = ""
    private var locationId: String? = ""
    private var locationName: String? = ""
    private var createDate: Long? = 0

    constructor()

    constructor(
        id: Long,
        uid: String,
        locationId: String,
        locationName: String,
        createDate: Long
    ){
        this.id = id
        this.uid = uid
        this.locationId = locationId
        this.locationName = locationName
        this.createDate = createDate
    }

    fun getId(): Long {
        return id
    }

    fun getUid(): String? {
        return uid
    }

    fun getLocationId(): String? {
        return locationId
    }

    fun getLocationName(): String? {
        return locationName
    }

    fun getCreateDate(): Long? {
        return createDate
    }
}