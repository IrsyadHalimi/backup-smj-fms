package com.smj.app.ui.pengawas.model

class LoadingTime {
    private var id: Long = 0L
    private var loadingId: String? =null
    private var operator: String? = null
    private var operatorId: String? = null
    private var status: String? = null
    private var taskId: String? = null
    private var timeStamp: String? = null
    private var unitId: String? = null
    private var unitCode: String? = null
    private var uid: String? = null

    constructor()

    constructor(
        id: Long,
        taskId: String,
        loadingId: String,
        timeStamp: String?,
        status: String?,
        unitId: String?,
        unitCode: String?,
        operatorId: String?,
        operator: String?,
        uid: String?
    ){
        this.id = id
        this.taskId = taskId
        this.loadingId = loadingId
        this.timeStamp = timeStamp
        this.status = status
        this.unitId = unitId
        this.unitCode = unitCode
        this.operatorId = operatorId
        this.operator = operator
        this.uid = uid
    }

    fun getId(): Long {
        return id
    }

    fun getTaskId(): String? {
        return taskId
    }

    fun getLoadingId(): String? {
        return loadingId
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

    fun getOperatorId(): String? {
        return operatorId
    }

    fun getOperator(): String? {
        return operator
    }

    fun getUid(): String? {
        return uid
    }
}