package com.example.vpn_cubikcode.vpn

import android.content.Context
import com.example.vpn_cubikcode.util.VpnEventBus
import java.io.File
import java.util.concurrent.Executors

class XrayManager(private val context: Context) {

    private var process: Process? = null
    private val ioExecutor = Executors.newFixedThreadPool(2)

    fun start(configJson: String): Result<Unit> {
        stop()

        return runCatching {
            val configFile = File(context.filesDir, "xray_config.json")
            configFile.writeText(configJson)
            VpnEventBus.appendLog("Конфиг сохранен: ${configFile.absolutePath}")

            val binaryFile = findXrayBinary()
                ?: throw IllegalStateException(
                    "xray-core бинарник не найден. Положите исполняемый файл 'xray' в /data/data/${context.packageName}/files/xray или добавьте в nativeLibraryDir."
                )

            if (!binaryFile.canExecute()) {
                binaryFile.setExecutable(true)
            }

            val command = listOf(binaryFile.absolutePath, "run", "-config", configFile.absolutePath)
            VpnEventBus.appendLog("Запуск xray: ${command.joinToString(" ")}")

            process = ProcessBuilder(command)
                .redirectErrorStream(false)
                .start()

            streamOutput(process ?: throw IllegalStateException("Процесс не создан"))
        }
    }

    fun stop() {
        process?.let {
            VpnEventBus.appendLog("Остановка xray процесса")
            it.destroy()
            try {
                it.waitFor()
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
            if (it.isAlive) {
                it.destroyForcibly()
            }
        }
        process = null
    }

    private fun streamOutput(proc: Process) {
        ioExecutor.execute {
            proc.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { VpnEventBus.appendLog("xray stdout: $it") }
            }
        }

        ioExecutor.execute {
            proc.errorStream.bufferedReader().useLines { lines ->
                lines.forEach { VpnEventBus.appendLog("xray stderr: $it") }
            }
        }
    }

    private fun findXrayBinary(): File? {
        // Основной путь интеграции: положите исполняемый xray-core бинарник в filesDir с именем "xray".
        // Пример на устройстве: /data/data/<package>/files/xray
        val candidateInFiles = File(context.filesDir, "xray")
        if (candidateInFiles.exists()) return candidateInFiles

        val nativeDir = context.applicationInfo.nativeLibraryDir ?: return null
        val candidates = listOf(
            File(nativeDir, "xray"),
            File(nativeDir, "libxray.so")
        )

        return candidates.firstOrNull { it.exists() }
    }
}
