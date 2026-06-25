package com.smj.app.helper

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class FormatNumber {
    private val nilai = 0

    fun simpleNumber(nilai: Int?): String? {
        val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))
        return formatter.format(nilai)
    }

    fun simpleCurrency(nilai: Any): String? {
        val formatter = DecimalFormat("###-####-####")
        return formatter.format(nilai)
    }
}