package com.smj.app.ui.auth.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class TasksDTO(
    var nickname: String? = null,
    var email: String? = null,
    var phoneNumber: String? = null,
    var photo: String? = null,
    var status: String? = null,
    var gender: String? = null,
    var birthday: String? = null,
    var latitude: String? = null,
    var longitude: String? = null,
    var uid: String? = null,
    var createDate: String? = null
) {
    constructor() : this("", "", "", "", "", "", "", "", "", "", "") {}

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "nickname" to nickname,
            "email" to email,
            "phoneNumber" to phoneNumber,
            "photo" to photo,
            "status" to status,
            "gender" to gender,
            "birthday" to birthday,
            "latitude" to latitude,
            "longitude" to longitude,
            "uid" to uid,
            "createDate" to createDate
        )
    }
}