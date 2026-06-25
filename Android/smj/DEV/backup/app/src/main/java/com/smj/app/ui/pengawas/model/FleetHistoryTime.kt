package com.smj.app.ui.pengawas.model

class FleetHistoryTime {
    private var id: Long = 0L
    private var createBy: String? =null
    private var createDate: Int? =0
    private var remark: String? = "..."
    private var taskId: String? =null
    private var timestamp: String? =null
    private var updateBy: String? =null
    private var updateDate: String? =null

    constructor()

    constructor(
        id: Long,
        createBy: String,
        createDate: Int,
        remark: String,
        taskId: String,
        timestamp: String,
        updateBy: String,
        updateDate: String
    ) {
        this.id = id
        this.createBy = createBy
        this.createDate = createDate
        this.remark = remark
        this.taskId = taskId
        this.timestamp = timestamp
        this.updateBy = updateBy
        this.updateDate = updateDate
    }

    fun getId(): Long {
        return id
    }

    fun getCreateBy(): String? {
        return createBy
    }

    fun getCreateDate(): Int? {
        return createDate
    }

    fun getRemark(): String? {
        return remark
    }

    fun getTaskId(): String? {
        return taskId
    }

    fun getTimestamp(): String? {
        return timestamp
    }

    fun getUpdateBy(): String? {
        return updateBy
    }

    fun getUpdateDate(): String? {
        return updateDate
    }
}