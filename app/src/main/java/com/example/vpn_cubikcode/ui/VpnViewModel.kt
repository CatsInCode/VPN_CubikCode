package com.example.vpn_cubikcode.ui

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vpn_cubikcode.data.ConfigRepository
import com.example.vpn_cubikcode.util.JsonValidator
import com.example.vpn_cubikcode.util.SampleConfigs
import com.example.vpn_cubikcode.util.VpnEventBus
import com.example.vpn_cubikcode.util.VpnStatus
import com.example.vpn_cubikcode.vpn.MyVpnService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class VpnUiState(
    val configText: String = "",
    val status: VpnStatus = VpnStatus.DISCONNECTED,
    val logs: String = ""
)

class VpnViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ConfigRepository(application)
    private val isDebugBuild: Boolean =
        (application.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0

    private val _configText = MutableStateFlow("")
    val configText: StateFlow<String> = _configText.asStateFlow()

    private val _uiState = MutableStateFlow(VpnUiState())
    val uiState: StateFlow<VpnUiState> = _uiState.asStateFlow()

    init {
        val saved = repository.getConfig()
        _configText.value = when {
            saved.isNotBlank() -> saved
            isDebugBuild -> SampleConfigs.XRAY_VLESS_REALITY
            else -> ""
        }

        viewModelScope.launch {
            combine(_configText, VpnEventBus.status, VpnEventBus.logs) { config, status, logs ->
                VpnUiState(
                    configText = config,
                    status = status,
                    logs = logs.joinToString("\n")
                )
            }.collect { _uiState.value = it }
        }
    }

    fun onConfigChanged(newText: String) {
        _configText.value = newText
    }

    fun onConnectClick(onNeedPermission: () -> Unit, onValidationError: (String) -> Unit) {
        val validation = JsonValidator.validateConfig(_configText.value)
        if (validation.isFailure) {
            onValidationError(validation.exceptionOrNull()?.message ?: "Невалидный JSON")
            return
        }

        repository.saveConfig(_configText.value)
        onNeedPermission()
    }

    fun startVpnService() {
        val context = getApplication<Application>()
        val intent = Intent(context, MyVpnService::class.java).apply {
            action = MyVpnService.ACTION_START
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun stopVpnService() {
        val context = getApplication<Application>()
        val intent = Intent(context, MyVpnService::class.java).apply {
            action = MyVpnService.ACTION_STOP
        }
        ContextCompat.startForegroundService(context, intent)
    }
}
