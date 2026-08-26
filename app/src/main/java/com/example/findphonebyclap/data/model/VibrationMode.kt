package com.example.findphonebyclap.data.model

enum class VibrationMode(val pattern: LongArray) {
    OFF(longArrayOf(0)),
    DEFAULT(longArrayOf(0, 500, 500)),
    STRONG(longArrayOf(0, 1000, 300)),
    HEARTBEAT(longArrayOf(0, 200, 150, 200, 600)),
    SOS(longArrayOf(0, 200, 100, 200, 100, 200, 400, 600, 100, 600, 100, 600, 400, 200, 100, 200, 100, 200, 800))
}
