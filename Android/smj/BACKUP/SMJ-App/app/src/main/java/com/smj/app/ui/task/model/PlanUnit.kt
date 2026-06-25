package com.smj.app.ui.task.model

class PlanUnit {
    private var id: Long = 0L
    private var taskId: String? = null
    private var unitId: String? = null
    private var taskUnitId: String? = null
    private var sopirId: String? = null
    private var sopir: String? = null
    private var unitCode: String? = null
    private var uid: String? = null
    private var status: String? = null

    constructor()

    constructor(
        id: Long,
        taskId: String?,
        taskUnitId: String?,
        sopirId: String?,
        sopir: String?,
        unitCode: String?,
        uid: String?,
        status: String?
    ){
        this.id = id
        this.taskId = taskId
        this.taskUnitId = taskUnitId
        this.sopirId = sopirId
        this.sopir = sopir
        this.unitCode = unitCode
        this.uid = uid
        this.status = status
    }

    fun getId(): Long {
        return id
    }

    fun getTaskId(): String? {
        return taskId
    }

    fun getTaskUnitId(): String? {
        return taskUnitId
    }
    //
    fun getSopirId(): String? {
        return sopirId
    }

    fun getSopir(): String? {
        return sopir
    }

    fun getUnitCode(): String? {
        return unitCode
    }

    fun getUid(): String? {
        return uid
    }

    fun getStatus(): String? {
        return status
    }
}