package com.smj.app.ui.settings.model

class ShiftList {
    private var id: Long = 0L
    private var uid: String? = ""
    private var shiftId: String? = ""
    private var shiftName: String? = ""
    private var shiftTimeStart: String? = ""
    private var shiftTimeEnd: String? = ""
    private var createDate: Long? = 0

    constructor()

    constructor(
        id: Long,
        uid: String,
        shiftId: String,
        shiftName: String,
        shiftTimeStart: String,
        shiftTimeEnd: String,
        createDate: Long
    ){
        this.id = id
        this.uid = uid
        this.shiftId = shiftId
        this.shiftName = shiftName
        this.shiftTimeStart = shiftTimeStart
        this.shiftTimeEnd = shiftTimeEnd
        this.createDate = createDate
    }

    fun getId(): Long {
        return id
    }

    fun getUid(): String? {
        return uid
    }

    fun getShiftId(): String? {
        return shiftId
    }

    fun getShiftName(): String? {
        return shiftName
    }

    fun getShiftTimeStart(): String? {
        return shiftTimeStart
    }

    fun getshiftTimeEnd(): String? {
        return shiftTimeEnd
    }

    fun getCreateDate(): Long? {
        return createDate
    }
}