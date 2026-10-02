package com.kotlakiran.toolkit.money

import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyTest {

    @Test
    fun indianGrouping() {
        assertEquals("₹1,84,200", Money.paiseToRupees(18_420_000))
        assertEquals("₹36,20,000", Money.paiseToRupees(362_000_000))
        assertEquals("₹500", Money.paiseToRupees(50_000))
    }

    @Test
    fun lakhCompact() {
        assertEquals("₹2.9L", Money.paiseToRupeesCompact(29_000_000))
        assertEquals("₹39L", Money.paiseToRupeesCompact(390_000_000))
    }

    @Test
    fun croreCompact() {
        assertEquals("₹1.2Cr", Money.paiseToRupeesCompact(1_200_000_000))
    }

    @Test
    fun plainCompact() {
        assertEquals("₹46,880", Money.paiseToRupeesCompact(4_688_000))
    }

    @Test
    fun negative() {
        assertEquals("-₹1,500", Money.paiseToRupees(-150_000))
        assertEquals("-₹2.5L", Money.paiseToRupeesCompact(-25_000_000))
    }
}
