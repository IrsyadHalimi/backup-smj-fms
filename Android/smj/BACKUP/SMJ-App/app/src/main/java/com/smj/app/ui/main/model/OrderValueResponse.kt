package com.smj.app.ui.main.model

data class OrderValueResponse(
    var month: Double,
    val monthName: String,
    val sum: Int,
    val year: Double,
    val yearName: String
)