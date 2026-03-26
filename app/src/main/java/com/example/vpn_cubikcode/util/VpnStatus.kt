package com.example.vpn_cubikcode.util

enum class VpnStatus(val displayName: String) {
    DISCONNECTED("Отключено"),
    CONNECTING("Подключение..."),
    CONNECTED("Подключено"),
    ERROR("Ошибка")
}
