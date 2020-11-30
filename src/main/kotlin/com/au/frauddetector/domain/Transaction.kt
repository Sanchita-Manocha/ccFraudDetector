package com.au.frauddetector.domain

import java.math.BigDecimal

data class Transaction(
    val creditCardNumber: String,
    val time: Long,
    val amount: BigDecimal
)