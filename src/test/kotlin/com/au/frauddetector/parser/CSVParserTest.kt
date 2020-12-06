package com.au.frauddetector.parser

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

internal class CSVParserTest {

    @Test
    fun `access denied exception when csv file is not readable`() {
        val file = File("test.csv")
        assertThrows<AccessDeniedException> {  readCSVFileToTransactions(file)}
    }

    @Test
    fun `returns only valid transactions from csv file`() {
        val file = File(
            this::class.java.classLoader.getResource("sample_txns.csv").file
        )
        val txnList = readCSVFileToTransactions(file)
        assertEquals(2, txnList.size)
        txnList.map { it?.creditCardNumber }
            .containsAll(listOf("xxxxxCARD1", "xxxxxCARD4"))

   }

    @Test
    fun `null transaction for the csv record with more than 3 fields`() {
        val csvRecord = "10d7ce2f43e35fa57d1bbf8b1e2, 2014-04-29T13:15:54, 10.00, 10.00"
        assertNull(convertToTransaction(0, csvRecord))
   }

    @Test
    fun `null transaction for the csv record with invalid time`() {
        val csvRecord = "10d7ce2f43e35fa57d1bbf8b1e2, 2014-04-29T13:1:54, 10.00"
        assertNull(convertToTransaction(0, csvRecord))
   }

    @Test
    fun `null transaction for the csv records with invalid amount`() {
        val csvRecord = "10d7ce2f43e35fa57d1bbf8b1e2, 2014-04-29T13:14:54, 10.00x"
        assertNull(convertToTransaction(0, csvRecord))
   }

    @Test
    fun `transaction for csv records with white spaces`() {
        val csvRecord = "10d7ce2f43e35fa57d1bbf8b1e2,          2014-04-29T13:14:54,        10.00"

        val transaction = convertToTransaction(0, csvRecord)
        assertEquals("10d7ce2f43e35fa57d1bbf8b1e2", transaction!!.creditCardNumber)
   }

}