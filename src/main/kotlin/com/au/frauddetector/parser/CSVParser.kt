package com.au.frauddetector.parser

import com.au.frauddetector.domain.Transaction
import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val logger = LoggerFactory.getLogger(object {}::class.java.`package`.name)

fun readCSVFileToTransactions(file: File): List<Transaction> {
    if (!file.canRead())
        throw AccessDeniedException(file).also {
            logger.error("${file.name} is not readable")
        }
    logger.info("Reading transactions from File - '${file.name}'\"")
    return file.useLines {
        it.mapIndexed { index, row -> convertToTransaction(index, row) }
            .filterNotNull()
            .toList()
    }
}

fun convertToTransaction(index: Int, row: String): Transaction? {
    var txn: Transaction? = null
    try {
        val fields = row.split(",")
        if (fields.size != 3)
            logInvalidTxn(index, row)
        else {
            txn = Transaction(
                creditCardNumber = fields[0],
                time = stringDateToUnixTime(fields[1].removeWhiteSpaces()),
                amount = fields[2].removeWhiteSpaces().toBigDecimal()
            )
        }
    } catch (e: Exception) {
        logInvalidTxn(index, row)
    }
    return txn
}

private fun logInvalidTxn(index: Int, row: String) {
    logger.warn(
        "skipped INVALID_TRANSACTION at row $index" +
                "\n$row"
    )
}

private fun stringDateToUnixTime(time: String): Long {
    return LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
        .toEpochSecond(ZoneOffset.UTC)
}

fun String.removeWhiteSpaces() = this.replace("\\s".toRegex(), "")
