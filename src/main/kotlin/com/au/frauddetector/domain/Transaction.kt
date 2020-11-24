package com.au.frauddetector.domain

import java.math.BigDecimal

data class Transaction(
    val creditCardNumber: String,
    val time: String,
    val amount: BigDecimal
)