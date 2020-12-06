package com.au.frauddetector.detector


import com.au.frauddetector.config.Config
import com.au.frauddetector.domain.Transaction
import com.au.frauddetector.parser.readCSVFileToTransactions
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import java.math.BigDecimal

private val logger = LoggerFactory.getLogger(object {}::class.java.`package`.name)

class FraudDetector(private val config: Config){

    fun detectFraud(): Set<String> {
        val fileURL = this::class.java.classLoader.getResource(config.sourceFile)
            ?: throw FileNotFoundException().also {
                logger.error("${config.sourceFile} does not exist") }
        return getFraudCards(
            config.priceThreshold,
            config.slidingTimeWindowInHours,
            readCSVFileToTransactions(File(fileURL.file))
        )
    }


}

fun getFraudCards(
    priceThreshold: BigDecimal,
    slidingTimeWindowInHours: Int,
    transactions: List<Transaction>
): Set<String> {
    if (transactions.isEmpty())
        return emptySet()
    return transactions.groupBy { it.creditCardNumber }
        .filter {
            isCardFraud(priceThreshold, slidingTimeWindowInHours, it.value)
        }.keys
}

fun isCardFraud(priceThreshold: BigDecimal, timeWindowInHours: Int, transactions: List<Transaction>): Boolean {
    transactions.forEachIndexed { index, txn ->
        val totalAmount = getTotalAmountWithInTimeWindow(txn.time, timeWindowInHours, transactions.subList(0, index + 1))
        if (totalAmount >= priceThreshold)
            return true
    }
    return false
}

fun getTotalAmountWithInTimeWindow(
    transactionTime: Long,
    timeWindowInHours: Int,
    transactions: List<Transaction>
): BigDecimal {
    return transactions.filter { transactionTime - it.time <= timeWindowInHours * 60 * 60 }.sumOf { it.amount }
}