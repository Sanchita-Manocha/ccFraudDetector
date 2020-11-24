package com.au.frauddetector.config

import java.math.BigDecimal

data class Config(
    val priceThreshold: BigDecimal,
    val slidingTimeWindowInHours: Int,
    val sourceFile: String,
) {
    companion object {
        fun defaultConfig() = Config(
            env("PRICE_THRESHOLD", "100.00").toBigDecimal(),
            env("TIME_WINDOW", "24").toInt(),
            env("SOURCE_FILE", "transaction.csv"),
        )
    }
}

private fun env(name: String, default: String): String = System.getenv(name) ?: default