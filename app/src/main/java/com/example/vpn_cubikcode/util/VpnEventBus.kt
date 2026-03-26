package com.example.vpn_cubikcode.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object VpnEventBus {
    private const val MAX_LOG_LINES = 150

    private val _status = MutableStateFlow(VpnStatus.DISCONNECTED)
    val status: StateFlow<VpnStatus> = _status.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    fun setStatus(status: VpnStatus) {
        _status.value = status
    }

    fun appendLog(message: String) {
        val newItem = "${System.currentTimeMillis()} | $message"
        val newLogs = (_logs.value + newItem).takeLast(MAX_LOG_LINES)
        _logs.value = newLogs
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }
}
