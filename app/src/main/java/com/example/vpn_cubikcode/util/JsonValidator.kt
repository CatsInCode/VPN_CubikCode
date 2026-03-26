package com.example.vpn_cubikcode.util

import org.json.JSONObject

object JsonValidator {
    fun validateConfig(json: String): Result<Unit> {
        if (json.isBlank()) {
            return Result.failure(IllegalArgumentException("JSON-конфиг пустой"))
        }

        return try {
            JSONObject(json)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Невалидный JSON: ${e.message}"))
        }
    }
}
