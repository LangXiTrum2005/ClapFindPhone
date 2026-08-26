package com.example.findphonebyclap.data.model

enum class FlashlightMode(val delayOnMs: Long, val delayOffMs: Long) {
    OFF(0L, 0L),
    DEFAULT(500L, 500L),
    FAST(200L, 200L),
    SOS(100L, 100L)
}
