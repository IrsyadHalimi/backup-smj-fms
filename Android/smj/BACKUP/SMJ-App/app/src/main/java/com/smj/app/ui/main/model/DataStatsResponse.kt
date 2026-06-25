package com.smj.app.ui.main.model

data class DataStatsResponse(
    val inventoryValue: Int,
    val orderValue: List<OrderValueResponse>
)