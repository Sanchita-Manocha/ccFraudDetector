package com.au.frauddetector.domain

import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset

data class Transaction(
    val creditCardNumber: String,
    val time: LocalDateTime,
    val amount: BigDecimal
){
    val unixTime
        get()=
    time.toEpochSecond(ZoneOffset.UTC)
}

