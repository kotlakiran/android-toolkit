package com.kotlakiran.toolkit.money

import kotlin.math.abs
import kotlin.math.roundToLong

/** Paise-based INR money helpers. All amounts are Long paise to avoid float drift. */
object Money {

    fun rupeesToPaise(rupees: Double): Long = (rupees * 100).roundToLong()

    /** Indian digit grouping: 18,420,000 paise -> "₹1,84,200". */
    fun paiseToRupees(paise: Long, symbol: String = "₹"): String {
        val neg = paise < 0
        val digits = (abs(paise) / 100).toString()
        if (digits.length <= 3) return (if (neg) "-" else "") + symbol + digits
        val last3 = digits.takeLast(3)
        val rest = digits.dropLast(3)
        val groups = ArrayList<String>()
        var i = rest.length
        while (i > 0) {
            val j = maxOf(0, i - 2)
            groups.add(rest.substring(j, i))
            i = j
        }
        val grouped = groups.asReversed().joinToString(",") + "," + last3
        return (if (neg) "-" else "") + symbol + grouped
    }

    /** Compact form: 29,000,000 paise -> "₹2.9L", 1,200,000,000 -> "₹1.2Cr". */
    fun paiseToRupeesCompact(paise: Long, symbol: String = "₹"): String {
        val neg = paise < 0
        val rupees = abs(paise) / 100.0
        val out = when {
            rupees >= 1e7 -> trim1(rupees / 1e7) + "Cr"
            rupees >= 1e5 -> trim1(rupees / 1e5) + "L"
            else -> return paiseToRupees(paise, symbol)
        }
        return (if (neg) "-" else "") + symbol + out
    }

    private fun trim1(v: Double): String {
        val s = String.format("%.1f", v)
        return if (s.endsWith(".0")) s.dropLast(2) else s
    }
}
