package com.kotlakiran.toolkit.dataimport

import kotlin.math.roundToLong

/**
 * Minimal CSV importer. Expected columns: date,description,amount[,dr|cr].
 * Amount in rupees; comma-grouped amounts are handled. Naive split, so
 * descriptions containing commas are truncated — fine for bank CSV exports.
 */
object CsvImporter {

    fun parse(csv: String, sourceName: String = "CSV import"): ParsedStatement {
        val txs = ArrayList<ParsedTransaction>()
        for (raw in csv.lines()) {
            val line = raw.trim()
            if (line.isEmpty()) continue
            val cols = line.split(",")
            if (cols.size < 3) continue
            val marker = if (cols.size >= 4) cols[cols.size - 1].trim().trim('"').uppercase() else ""
            val amount = cols[2].trim().trim('"').replace(",", "").toDoubleOrNull() ?: continue // skips a header row
            val desc = cols[1].trim().trim('"')
            val direction = when {
                marker == "DR" -> Direction.DEBIT
                marker == "CR" -> Direction.CREDIT
                amount < 0 -> Direction.DEBIT
                else -> Direction.CREDIT
            }
            txs += ParsedTransaction(
                description = desc,
                amountPaise = (kotlin.math.abs(amount) * 100).roundToLong(),
                direction = direction,
            )
        }
        return ParsedStatement(bankName = sourceName, periodLabel = null, transactions = txs)
    }
}
