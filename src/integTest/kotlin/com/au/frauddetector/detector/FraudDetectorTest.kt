package com.au.frauddetector.detector

import com.au.frauddetector.config.Config
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.FileNotFoundException

internal class FraudDetectorTest {
    @Test
    fun `throws exception when csv not found`() {
        val testConfig = Config.defaultConfig().let { it.copy(sourceFile = "not_existing.csv") }
        assertThrows<FileNotFoundException> { FraudDetector(testConfig).detectFraud() }
    }

    @Test
    fun `ignores invalid transaction records`() {
        val testConfig = Config.defaultConfig().let {
            it.copy(sourceFile = "sample_file_with_few_invalid_txns.csv") }
        val fraudCards = FraudDetector(testConfig).detectFraud()
        assertEquals(1, fraudCards.size)
        assertEquals("10d7ce2f43e45fa57d1bbf8b1e2", fraudCards.single())

    }

    @Test
    fun `single tx exceeds threshold with in sliding window`() {
        val testConfig = Config.defaultConfig().let {
            it.copy(sourceFile = "sample_file_with_single_txn_exceed_threshold.csv") }
        val fraudCards = FraudDetector(testConfig).detectFraud()
        assertEquals(1, fraudCards.size)
        assertEquals("10d7ce2f43e45fa57d1bbf8b1e2", fraudCards.single())
    }

    @Test
    fun `multiple txs from a card exceeds threshold with in sliding window`() {
        val testConfig = Config.defaultConfig().let {
            it.copy(sourceFile = "sample_file_with_multiple_txn_total_amt_exceed_threshold.csv") }
        val fraudCards = FraudDetector(testConfig).detectFraud()
        assertEquals(1, fraudCards.size)
        assertEquals("10d7ce2f43e45fa57d1bbf8b1e1", fraudCards.single())
    }

    @Test
    fun `multiple cards exceed threshold with in sliding window`() {
        val testConfig = Config.defaultConfig().let {
            it.copy(sourceFile = "sample_file_with_multiple_fraud_cards.csv") }
        val fraudCards = FraudDetector(testConfig).detectFraud()
        assertEquals(3, fraudCards.size)
        assertTrue(fraudCards.containsAll(listOf("10d7ce2f43e45fa57d1bbf8b1e2", "10d7ce2f43e45fa57d1bbf8b1e3", "10d7ce2f43e45fa57d1bbf8b1e4")))
    }
}