package com.smj.app.ui.task.model

class TaskGroupList {
    private var taskId: String? = null
    private var uid: String? = null
    private var date: String? = null
    private var shift: String? = null
    private var shiftId: String? = null
    private var shiftTime: String? = null
    private var pengawasId: String? = null
    private var pengawasName: String? = null
    private var operatorId: String? = null
    private var operatorName: String? = null
    private var exaCode: String? = null
    private var exaId: String? = null
    private var locationCode: String? = null
    private var locationName: String? = null
    private var galianCode: String? = null
    private var galianName: String? = null
    private var timbunanCode: String? = null
    private var timbunanName: String? = null
    private var plan: String? = null
    private var jarak: String? = null
    private var targetRit: String? = null
    private var status: String? = null
    private var createBy: String? = null
    private var createDate: Int? = null

    constructor()

    constructor(
        taskId: String?,
        uid: String?,
        date: String?,
        shift: String?,
        shiftId: String?,
        shiftTime: String?,
        pengawasId: String?,
        pengawasName: String?,
        operatorId: String?,
        operatorName: String?,
        exaCode: String?,
        exaId: String?,
        locationCode: String?,
        locationName: String?,
        galianCode: String?,
        galianName: String?,
        timbunanCode: String?,
        timbunanName: String?,
        plan: String?,
        jarak: String?,
        targetRit: String?,
        status: String?,
        createBy: String?,
        createDate: Int?
    ){
        this.taskId = taskId
        this.uid = uid
        this.date = date
        this.shift = shift
        this.shiftId = shiftId
        this.shiftTime = shiftTime
        this.pengawasId = pengawasId
        this.pengawasName = pengawasName
        this.operatorId = operatorId
        this.operatorName = operatorName
        this.exaCode = exaCode
        this.exaId = exaId
        this.locationCode = locationCode
        this.locationName = locationName
        this.galianCode = galianCode
        this.galianName = galianName
        this.timbunanCode = timbunanCode
        this.timbunanName = timbunanName
        this.plan = plan
        this.jarak = jarak
        this.targetRit = targetRit
        this.status = status
        this.createBy = createBy
        this.createDate = createDate
    }

    fun getTaskId(): String? {
        return taskId
    }

    fun getUid(): String? {
        return uid
    }

    fun getDate(): String? {
        return date
    }

    fun getShift(): String? {
        return shift
    }

    fun getShiftId(): String? {
        return shiftId
    }

    fun getShiftTime(): String? {
        return shiftTime
    }

    fun getPengawasId(): String? {
        return pengawasId
    }
    fun getPengawasName(): String? {
        return pengawasName
    }

    fun getOperatorId(): String? {
        return operatorId
    }

    fun getOperatorName(): String? {
        return operatorName
    }

    fun getExaCode(): String? {
        return exaCode
    }

    fun getExaId(): String? {
        return exaId
    }

    fun getLocationCode(): String? {
        return locationCode
    }

    fun getLocationName(): String? {
        return locationName
    }

    fun getGalianCode(): String? {
        return galianCode
    }

    fun getGalianName(): String? {
        return galianName
    }

    fun getTimbunanCode(): String? {
        return timbunanCode
    }

    fun getTimbunanName(): String? {
        return timbunanName
    }

    fun getPlan(): String? {
        return plan
    }

    fun getJarak(): String? {
        return jarak
    }

    fun getTargetRit(): String? {
        return targetRit
    }

    fun getStatus(): String? {
        return status
    }

    fun getCreateBy(): String? {
        return createBy
    }

    fun getCreateDate(): Int? {
        return createDate
    }
}