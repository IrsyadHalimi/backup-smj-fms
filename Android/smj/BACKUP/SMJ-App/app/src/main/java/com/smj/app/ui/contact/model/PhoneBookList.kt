package com.smj.app.ui.contact.model

class PhoneBookList {
    private var contactId: String? = ""
    private var displayName: String? = ""
    private var phoneNumber: String? = ""

    constructor()

    constructor(
        contactId: String,
        displayName: String,
        phoneNumber: String,
    ) {
        this.contactId = contactId
        this.displayName = displayName
        this.phoneNumber = phoneNumber
    }

    fun getContactId(): String? {
        return contactId
    }

    fun getDisplayName(): String? {
        return displayName
    }

    fun getPhoneNumber(): String? {
        return phoneNumber
    }

    companion object {
        private var counter = 0L
    }
}

