package com.au.frauddetector.detector

import com.au.frauddetector.config.Config
import com.au.frauddetector.domain.Transaction
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.FileNotFoundException
import java.math.BigDecimal


internal class FraudDetectorTest{

    @Test
    fun `exception when file does not exist`() {
        val testConfig = Config.defaultConfig().let{it.copy(sourceFile = "test.csv")}
        assertThrows<FileNotFoundException>{ FraudDetector(testConfig).detectFraud()}
    }

    @Test
    fun `correct total amount for given time window`(){
        val transactions = listOf(
            Transaction("card1", 1583882100,10.toBigDecimal()),
            Transaction("card1", 1583883800,13.toBigDecimal()),
            Transaction("card1", 1583884800,19.toBigDecimal()),
            Transaction("card1", 1583888400,16.toBigDecimal())
        )
        assertEquals(35.toBigDecimal(),
            getTotalAmountWithInTimeWindow(1583888400, 1, transactions))

    }

    @Test
    fun `no transaction within  given time window`() {
        val transactions = listOf(
            Transaction("card1", 1583882100, 10.toBigDecimal()),
            Transaction("card1", 1583883800, 13.toBigDecimal()),
            Transaction("card1", 1583884800, 19.toBigDecimal()),
            Transaction("card1", 1583888400, 16.toBigDecimal())
        )
        assertEquals(
            BigDecimal.ZERO,
            getTotalAmountWithInTimeWindow(1583898400, 1, transactions)
        )
    }

    @Test
    fun `when total amount is less than price threshold within time window`() {
        val transactions = listOf(
            Transaction("card1", 1583882100,10.toBigDecimal()),
            Transaction("card1", 1583883800,13.toBigDecimal()),
            Transaction("card1", 1583884800,19.toBigDecimal()),
            Transaction("card1", 1583888400,16.toBigDecimal())
        )
        assertFalse(isCardFraud(50.toBigDecimal(), 1, transactions))
    }

    @Test
    fun `when total amount is greater than price threshold within time window`() {
        val transactions = listOf(
            Transaction("card1", 1583882100,10.toBigDecimal()),
            Transaction("card1", 1583883800,13.toBigDecimal()),
            Transaction("card1", 1583884800,19.toBigDecimal()),
            Transaction("card1", 1583888400,36.toBigDecimal())
        )
        assertTrue(isCardFraud(50.toBigDecimal(), 1, transactions))
    }

    @Test
    fun `when amount of single transaction within is greater than price threshold`() {
        val transactions = listOf(
            Transaction("card1", 1583182100,10.toBigDecimal()),
            Transaction("card1", 1583883800,63.toBigDecimal()),
            Transaction("card1", 1583984800,19.toBigDecimal()),
            Transaction("card1", 1584888400,6.toBigDecimal())
        )
        assertTrue(isCardFraud(50.toBigDecimal(), 1, transactions))
    }

    @Test
    fun `no fraud cards when empty list of transactions`() {
        assertTrue(getFraudCards(BigDecimal.TEN, 1, emptyList())
            .isEmpty())
    }

    @Test
    fun `list of fraud cards exceeded price threshold within time window`() {
        val transactions = listOf(
            Transaction("card1", 1583880000,10.toBigDecimal()),
            Transaction("card2", 1583881200,12.toBigDecimal()),
            Transaction("card3", 1583881200,6.toBigDecimal()),
            Transaction("card1", 1583883800,14.toBigDecimal()),
            Transaction("card2", 1583884800,19.toBigDecimal()),
            Transaction("card3", 1583884800,13.toBigDecimal()),
            Transaction("card4", 1583884900,25.toBigDecimal())
        )
        val fraudCards = getFraudCards(20.toBigDecimal(), 1, transactions)
        assertTrue(fraudCards.containsAll(listOf("card2", "card4")))
    }

}