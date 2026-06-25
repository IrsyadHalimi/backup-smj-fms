package com.smj.app.ui.pengawas.model

class RitaseRemark {
    private var remark: String? = null

    constructor()

    constructor(
        remark: String?
    ){
        this.remark = remark
    }

    fun getRemark(): String? {
        return remark
    }
}