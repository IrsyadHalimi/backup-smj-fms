package com.smj.app.maskededittext.maskers

import com.smj.app.maskededittext.Mask

interface BaseMasker {
    fun onTextChanged(charSequence: CharSequence?, start: Int, count: Int, before: Int)
    fun getTextWithReturnPattern(): String?
    val onTextMaskedListener: (String) -> Unit
    val mask: Mask
    val inputType: Int
}