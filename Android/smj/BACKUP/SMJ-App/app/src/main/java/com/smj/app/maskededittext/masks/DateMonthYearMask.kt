package com.smj.app.maskededittext.masks

import com.smj.app.maskededittext.Mask

class DateMonthYearMask : Mask() {
    override val maskPattern: String
        get() = "## / ####"
    override val returnPattern: String
        get() = "##-####"

    override fun getParsedText(maskedText: String): String? {
        return filterMaskedText(maskedText).takeIf { isValidToParse(maskedText) }
    }

    override fun isValidToParse(maskedText: String): Boolean {
        return maskedText.length == maskPattern.length
    }

    override fun filterMaskedText(maskedText: String): String {
        return maskedText.filter { it.isDigit() }
    }
}
