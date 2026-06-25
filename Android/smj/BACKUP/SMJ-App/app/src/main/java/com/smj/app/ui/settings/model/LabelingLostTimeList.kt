package com.smj.app.ui.settings.model

class LabelingLostTimeList {
    private var id: Long = 0L
    private var uid: String? = ""
    private var labelingLostId: String? = ""
    private var labelingLostName: String? = ""
    private var createDate: Long? = 0

    constructor()

    constructor(
        id: Long,
        uid: String,
        labelingLostId: String,
        labelingLostName: String,
        createDate: Long
    ){
        this.id = id
        this.uid = uid
        this.labelingLostId = labelingLostId
        this.labelingLostName = labelingLostName
        this.createDate = createDate
    }

    fun getId(): Long {
        return id
    }

    fun getUid(): String? {
        return uid
    }

    fun getLabelingLostId(): String? {
        return labelingLostId
    }

    fun getLabelingLostName(): String? {
        return labelingLostName
    }

    fun getCreateDate(): Long? {
        return createDate
    }
}