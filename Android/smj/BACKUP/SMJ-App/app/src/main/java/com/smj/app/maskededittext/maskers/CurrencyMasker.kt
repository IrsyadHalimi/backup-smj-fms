package com.smj.app.maskededittext.maskers

import android.text.InputType
import com.smj.app.maskededittext.Mask
import java.text.NumberFormat
import java.util.*

class CurrencyMasker(
    override val mask: Mask,
    override val onTextMaskedListener: (String) -> Unit,
    private val currencyDecimalLimit: Int
) : BaseMasker {

    override val inputType: Int
        get() = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED

    override fun onTextChanged(charSequence: CharSequence?, start: Int, count: Int, before: Int) {
        if (charSequence == null || charSequence.isBlank()) {
            return
        }

        var numberStr = charSequence.toString().replace(",", "").replace(mask.maskPattern, "")
        if (numberStr.isBlank()) {
            numberStr = "0"
        }
        val numList = numberStr.split(".").toMutableList()
        if (numList.first().isEmpty()) {
            numList[0] = "0"
        }

        val formattedTextBuilder = StringBuilder(mask.maskPattern).apply {
            append(NumberFormat.getNumberInstance(Locale.US).format(numList.first().toFloat()))
            if (numList.size > 1) {
                append(POINT)
                if (currencyDecimalLimit == NO_DECIMAL_LIMIT) {
                    append(numList[1])
                } else {
                    append(getLimitedDecimalString(numList[1]))
                }
            }
        }
        onTextMaskedListener(formattedTextBuilder.toString())
    }

    override fun getTextWithReturnPattern(): String? {
        return null
    }

    private fun getLimitedDecimalString(decimalString: String): String {
        return with(decimalString) {
            substring(0, if (length <= currencyDecimalLimit) length else currencyDecimalLimit)
        }
    }

    companion object {
        const val DEFAULT_DECIMAL_LIMIT = 2
        const val NO_DECIMAL_LIMIT = -1
        private const val POINT = "."

        fun checkIfLimitSafe(currencyDecimalLimit: Int) {
            if (currencyDecimalLimit < NO_DECIMAL_LIMIT) {
                throw IllegalArgumentException("currencyDecimalLimit must be equal or bigger than -1")
            }
        }
    }
}