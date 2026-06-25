package com.smj.app.ui.task.model

class TaskList {
    private var id: Long = 0L
    private var taskId: String? = null
    private var category: String? = null
    private var contactId: String? = null
    private var productId: String? = null
    private var contact: String? = null
    private var dueDate: String? = null
    private var note: String? = null
    private var status: String? = null
    private var priority: Long? = 0

    constructor()

    constructor(
        id: Long,
        taskId: String,
        category: String,
        contactId: String,
        productId: String,
        contact: String,
        dueDate: String,
        note: String,
        status: String,
        priority: Long,
    ) {
        this.id = id
        this.taskId = taskId
        this.category = category
        this.contactId = contactId
        this.productId = productId
        this.contact = contact
        this.dueDate = dueDate
        this.note = note
        this.status = status
        this.priority = priority
    }

    fun getId(): Long {
        return id
    }

    fun getTaskId(): String? {
        return taskId
    }

    fun getCategory(): String? {
        return category
    }

    fun getContactId(): String? {
        return contactId
    }

    fun getProductId(): String? {
        return productId
    }

    fun getContact(): String? {
        return contact
    }

    fun getDueDate(): String? {
        return dueDate
    }

    fun getNote(): String? {
        return note
    }

    fun getStatus(): String? {
        return status
    }

    fun getPriority(): Long? {
        return priority
    }
}