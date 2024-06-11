package com.halidodat.neuralnetworkvisualizer

import com.halidodat.neuralnetworkvisualizer.models.SampleRealm
import uniffi.mobile.WeightImage

@OptIn(ExperimentalUnsignedTypes::class)
class Grid(val width: Int, val height: Int) {
    private var data: UByteArray = UByteArray(width * height)

    constructor(image: WeightImage) : this(image.width.toInt(), image.height.toInt()) {
        data = image.image.toUByteArray()
    }

    constructor(image: SampleRealm) : this(image.width, image.height) {
        data = image.values.toUByteArray()
    }

    fun get(x: Int, y: Int): UByte {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            val index = y * width + x
            return data[index]
        }
        return 0U;

    }

    fun set(x: Int, y: Int, value: UByte) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            val index = y * width + x
            data[index] = value
        }
    }

    fun add(x: Int, y: Int, value: UByte) {
        val previous = get(x, y)
        if (previous.toUInt() + value.toUInt() > 255U) {
            set(x, y, 255U)
            return
        }
        set(x, y, (previous + value).toUByte())
    }

    fun clear() {
        data.fill(0U)
    }

    fun bytes(): ByteArray {
        return data.toByteArray()
    }
}