package com.au.frauddetector.detector

import com.au.frauddetector.config.Config
import com.au.frauddetector.domain.Transaction
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.FileNotFoundException
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


internal class FraudDetectorTest {

    @Test
    fun `exception when file does not exist`() {
        val testConfig = Config.defaultConfig().let { it.copy(sourceFile = "test.csv") }
        assertThrows<FileNotFoundException> { FraudDetector(testConfig).detectFraud() }
    }

    @Test
    fun `correct total amount for given time window`() {
        val transactions = listOf(
            Transaction("card1", "2020-03-10T23:15:00".toLocalDateTime(), 10.toBigDecimal()),
            Transaction("card1", "2020-03-10T23:43:20".toLocalDateTime(), 13.toBigDecimal()),
            Transaction("card1", "2020-03-11T00:00:00".toLocalDateTime(), 19.toBigDecimal()),
            Transaction("card1", "2020-03-11T01:00:00".toLocalDateTime(), 16.toBigDecimal())
        )
        assertEquals(
            35.toBigDecimal(),
            getTotalAmountWithInTimeWindow(
                "2020-03-11T01:00:00".toLocalDateTime().toEpochSecond(ZoneOffset.UTC),
                1,
                transactions
            )
        )

    }

    @Test
    fun `no transaction within  given time window`() {
        val transactions = listOf(
            Transaction("card1", "2020-03-10T23:15:00".toLocalDateTime(), 10.toBigDecimal()),
            Transaction("card1", "2020-03-10T23:43:20".toLocalDateTime(), 13.toBigDecimal()),
            Transaction("card1", "2020-03-11T00:00:00".toLocalDateTime(), 19.toBigDecimal()),
            Transaction("card1", "2020-03-11T01:00:00".toLocalDateTime(), 16.toBigDecimal())
        )
        assertEquals(
            BigDecimal.ZERO,
            getTotalAmountWithInTimeWindow(
                "2020-03-11T03:00:00".toLocalDateTime().toEpochSecond(ZoneOffset.UTC),
                1,
                transactions
            )
        )
    }

    @Test
    fun `when total amount is less than price threshold within time window`() {
        val transactions = listOf(
            Transaction("card1", "2020-03-10T23:15:00".toLocalDateTime(), 10.toBigDecimal()),
            Transaction("card1", "2020-03-10T23:43:20".toLocalDateTime(), 13.toBigDecimal()),
            Transaction("card1", "2020-03-11T00:00:00".toLocalDateTime(), 19.toBigDecimal()),
            Transaction("card1", "2020-03-11T01:00:00".toLocalDateTime(), 16.toBigDecimal())
        )
        assertFalse(isCardFraud(50.toBigDecimal(), 1, transactions))
    }

    @Test
    fun `when total amount is greater than price threshold within time window`() {
        val transactions = listOf(
            Transaction("card1", "2020-03-10T23:15:00".toLocalDateTime(), 10.toBigDecimal()),
            Transaction("card1", "2020-03-10T23:43:20".toLocalDateTime(), 13.toBigDecimal()),
            Transaction("card1", "2020-03-11T00:00:00".toLocalDateTime(), 19.toBigDecimal()),
            Transaction("card1", "2020-03-11T01:00:00".toLocalDateTime(), 36.toBigDecimal())
        )
        assertTrue(isCardFraud(50.toBigDecimal(), 1, transactions))
    }

    @Test
    fun `when amount of single transaction within is greater than price threshold`() {
        val transactions = listOf(
            Transaction("card1", "2020-03-02T20:48:20".toLocalDateTime(), 10.toBigDecimal()),
            Transaction("card1", "2020-03-10T23:43:20".toLocalDateTime(), 63.toBigDecimal()),
            Transaction("card1", "2020-03-12T03:46:40".toLocalDateTime(), 19.toBigDecimal()),
            Transaction("card1", "2020-03-22T01:46:40".toLocalDateTime(), 6.toBigDecimal())
        )
        assertTrue(isCardFraud(50.toBigDecimal(), 1, transactions))
    }

    @Test
    fun `no fraud cards when empty list of transactions`() {
        assertTrue(
            getFraudCards(BigDecimal.TEN, 1, emptyList())
                .isEmpty()
        )
    }

    @Test
    fun `list of fraud cards exceeded price threshold within time window`() {
        val transactions = listOf(
            Transaction("card1", "2020-03-10T22:40:00".toLocalDateTime(), 10.toBigDecimal()),
            Transaction("card2", "2020-03-10T23:00:00".toLocalDateTime(), 12.toBigDecimal()),
            Transaction("card3", "2020-03-10T23:00:00".toLocalDateTime(), 6.toBigDecimal()),
            Transaction("card1", "2020-03-10T23:43:20".toLocalDateTime(), 14.toBigDecimal()),
            Transaction("card2", "2020-03-11T00:00:00".toLocalDateTime(), 19.toBigDecimal()),
            Transaction("card3", "2020-03-11T00:00:00".toLocalDateTime(), 13.toBigDecimal()),
            Transaction("card4", "2020-03-10T00:01:40".toLocalDateTime(), 25.toBigDecimal())
        )
        val fraudCards = getFraudCards(20.toBigDecimal(), 1, transactions)
        assertTrue(fraudCards.containsAll(listOf("card2", "card4")))
    }
}

fun String.toLocalDateTime() =
    LocalDateTime.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))