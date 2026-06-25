package com.smj.app.utils.response

import com.smj.app.ui.auth.model.Users
import com.smj.app.ui.contact.model.ContactList
import com.smj.app.ui.fleet.model.UnitList
import com.smj.app.ui.task.model.TaskGroupList

sealed class BaseResponseFirebase<out T> {

    //Users
    data class Success<out T>(val value: Users?) : BaseResponseFirebase<T>()
    data class Failed<out T>(val nothing: String?) : BaseResponseFirebase<T>()

    //Login
    data class LoginSuccess<out T>(val value: String?) : BaseResponseFirebase<T>()
    data class LoginFailure<out T>(val value: String?) : BaseResponseFirebase<T>()

    //contact
    data class ContactShowSuccess<out T>(val value: ArrayList<ContactList>?) : BaseResponseFirebase<T>()

    //contact
    data class UserShowSuccess<out T>(val value: ArrayList<ContactList>?) : BaseResponseFirebase<T>()

    //contact
    data class ProductShowSuccess<out T>(val value: ArrayList<UnitList>?) : BaseResponseFirebase<T>()

    //contact
    data class TaskShowSuccess<out T>(val value: ArrayList<TaskGroupList>?) : BaseResponseFirebase<T>()

}