package com.smj.app.ui.fleet.model

class UnitList {
    private var id: Long = 0L
    private var uid: String? = ""
    private var unitId: String? = ""
    private var unitCode: String? = ""
    private var unitType: String? = ""
    private var merk: String? = ""
    private var yom: String? = ""
    private var pit: String? = ""
    private var status: String? = ""
    private var createDate: Long? = 0

    constructor()

    constructor(
        id: Long,
        uid: String,
        unitId: String,
        unitCode: String,
        unitType: String,
        merk: String,
        yom: String,
        pit: String,
        status: String
    ){
        this.id = id
        this.uid = uid
        this.unitId = unitId
        this.unitCode = unitCode
        this.unitType = unitType
        this.merk = merk
        this.yom = yom
        this.pit = pit
        this.status = status
        this.createDate = createDate
    }

//    fun getId(): Long {
//        return id
//    }

    fun getUid(): String? {
        return uid
    }

    fun getUnitId(): String? {
        return unitId
    }

    fun getUnitCode(): String? {
        return unitCode
    }

    fun getUnitType(): String? {
        return unitType
    }

    fun getMerk(): String? {
        return merk
    }

    fun getYom(): String? {
        return yom
    }

    fun getPit(): String? {
        return pit
    }

    fun getStatus(): String? {
        return status
    }

    fun getCreateDate(): Long? {
        return createDate
    }
}