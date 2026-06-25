package com.smj.app.maskededittext

interface MaskCreator {
    fun create(maskPattern: String? = null, returnPattern: String? = null): Mask
}