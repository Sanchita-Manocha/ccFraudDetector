package com.au.frauddetector.detector


import com.au.frauddetector.config.Config
import com.au.frauddetector.domain.Transaction
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class FraudDetector(private val config: Config) {
    val logger = LoggerFactory.getLogger(this::class.java)

    fun getFraudCards() {
        val fileRows = readTxFileToList()
        val fraudCards = fraudCards(validTxns(fileRows))
    }

    private fun fraudCards(transactions: List<Transaction?>): List<String> {
        var fraudCards = mutableListOf<String>()
        if (transactions.size > 0) {
            transactions?.fold(mutableMapOf<String, List<Transaction>>()) { results, txn ->
                if (!results.containsKey(txn!!.creditCardNumber)) {
                    mutableMapOf(txn.creditCardNumber to mutableListOf(txn!!))
                } else {
                    results[txn.creditCardNumber]?.forEach {
                        if ((txn.time - it.time) <= config.slidingTimeWindowInHours &&
                            it.amount + txn.amount >= config.priceThreshold
                        ) {
                            fraudCards.add(txn.creditCardNumber)
                        }
                    }
                    results[txn.creditCardNumber] = results[txn.creditCardNumber]!!.plus(txn)
                    results
                }
            }
        }
        return fraudCards
    }

    private fun readTxFileToList(): List<String> {
        val filePath = this::class.java.classLoader.getResource(config.sourceFile)
        filePath ?: throw FileNotFoundException("${config.sourceFile} File not found")
        return File(filePath.file).useLines { it.toList() }.also {
            logger.info("Reading transactions from File - '${config.sourceFile}'")
        }
    }

    private fun validTxns(fileRows: List<String>): List<Transaction?> {
        return fileRows
            .map { convertToTransaction(it) }
            .filter { (it != null) }
            .toList()
    }
}

private fun stringDateToUnixTime(time: String): Long {
    return LocalDate.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
        .atStartOfDay(ZoneId.of("UTC"))
        .toInstant()
        .epochSecond
}

private fun convertToTransaction(row: String): Transaction? {
    var txn : Transaction? = null
    try {
        val fields = row.split(",")
        if (fields.size == 3) {
            txn = Transaction(
                creditCardNumber = fields[0],
                time = stringDateToUnixTime(fields[1].removeWhiteSpaces()),
                amount = fields[2].removeWhiteSpaces().toBigDecimal()
            )
        }
    }
    catch (e : Exception){
        e.printStackTrace()
    }
    return txn
}

fun String.removeWhiteSpaces () = this.replace("\\s".toRegex(), "")
