package com.smj.app.ui.pengawas.model

class Ritase {
    private var id: Long = 0L
    private var taskId: String? =null
    private var ritaseId: String? = null
    private var timeStamp: String? = null
    private var status: String? = null
    private var unitId: String? = null
    private var unitCode: String? = null
    private var supirId: String? = null
    private var supir: String? = null
    private var uid: String? = null

    constructor()

    constructor(
        id: Long,
        taskId: String,
        ritaseId: String,
        timeStamp: String?,
        status: String?,
        unitId: String?,
        unitCode: String?,
        supirId: String?,
        supir: String?,
        uid: String?
    ){
        this.id = id
        this.taskId = taskId
        this.ritaseId = ritaseId
        this.timeStamp = timeStamp
        this.status = status
        this.unitId = unitId
        this.unitCode = unitCode
        this.supirId = supirId
        this.supir = supir
        this.uid = uid
    }

    fun getId(): Long {
        return id
    }

    fun getTaskId(): String? {
        return taskId
    }

    fun getRitaseId(): String? {
        return ritaseId
    }

    fun getTimeStamp(): String? {
        return timeStamp
    }

    fun getStatus(): String? {
        return status
    }

    fun getUnitId(): String? {
        return unitId
    }

    fun getUnitCode(): String? {
        return unitCode
    }

    fun getSupirId(): String? {
        return supirId
    }

    fun getSupir(): String? {
        return supir
    }

    fun getUid(): String? {
        return uid
    }
}