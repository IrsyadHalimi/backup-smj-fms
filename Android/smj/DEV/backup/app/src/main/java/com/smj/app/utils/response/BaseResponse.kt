package com.smj.app.utils.response

import okhttp3.ResponseBody

/**
 * Created by MuhamadRiyadi on 01/01/2023
 * Phone: 08174100212
 * Website: www.prokonco.com
 */
sealed class BaseResponse<out T> {
    data class Success<out T>(val data: T? = null) : BaseResponse<T>()
    data class Denied(val data: ResponseBody? = null) : BaseResponse<Nothing>()
    data class Loading(val nothing: Nothing?=null) : BaseResponse<Nothing>()
    data class Error(val msg: String?) : BaseResponse<Nothing>()
    data class Notified(val msg: String?) : BaseResponse<Nothing>()
}