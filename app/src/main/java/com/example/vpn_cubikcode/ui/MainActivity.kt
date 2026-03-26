package com.example.vpn_cubikcode.ui

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.vpn_cubikcode.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: VpnViewModel by viewModels()

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.startVpnService()
        } else {
            Toast.makeText(this, "VPN permission не выдан", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListeners()
        observeState()
    }

    private fun initListeners() {
        binding.configInput.doAfterTextChanged {
            viewModel.onConfigChanged(it?.toString().orEmpty())
        }

        binding.connectButton.setOnClickListener {
            viewModel.onConnectClick(
                onNeedPermission = { requestVpnPermissionAndConnect() },
                onValidationError = { showError(it) }
            )
        }

        binding.disconnectButton.setOnClickListener {
            viewModel.stopVpnService()
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (binding.configInput.text.toString() != state.configText) {
                        binding.configInput.setText(state.configText)
                        binding.configInput.setSelection(state.configText.length)
                    }
                    binding.statusText.text = state.status.displayName
                    binding.logText.text = state.logs
                }
            }
        }
    }

    private fun requestVpnPermissionAndConnect() {
        val prepareIntent = VpnService.prepare(this)
        if (prepareIntent != null) {
            vpnPermissionLauncher.launch(prepareIntent)
        } else {
            viewModel.startVpnService()
        }
    }

    private fun showError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    }
}
