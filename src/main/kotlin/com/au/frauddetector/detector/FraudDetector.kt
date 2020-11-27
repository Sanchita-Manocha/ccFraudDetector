package com.au.frauddetector.detector


import com.au.frauddetector.config.Config
import com.au.frauddetector.domain.Transaction
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL

class FraudDetector(private val config: Config) {
    val logger = LoggerFactory.getLogger(this::class.java)
    fun getFraudCards() {
        val validTxn = readTxFile(this::class.java.classLoader.getResource(config.sourceFile))
    }

    private fun readTxFile(filePath: URL?): List<Transaction> {
        return File(filePath?.file).useLines { it.toList() }
            .map { it.split(",") }
            .filter { it.size == 3 }
            .map {
                Transaction(
                    creditCardNumber = it[0],
                    time = it[1],
                    amount = it[2].replace("\\s".toRegex(), "").toBigDecimal()
                )
            }.also {
                logger.info("Reading transactions from File - '${config.sourceFile}'")
            }
    }
}



