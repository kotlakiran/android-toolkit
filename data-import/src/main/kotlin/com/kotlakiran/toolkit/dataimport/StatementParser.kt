package com.kotlakiran.toolkit.dataimport

import kotlin.math.roundToLong

fun interface StatementParser {
    fun parse(text: String): ParsedStatement
}

/**
 * Best-effort parser for Indian bank statement text extracted from PDFs.
 * Recognises lines shaped like "12/09/26 UPI/... 1,250.00 Dr" and tags
 * salary / EMI / rent / card / UPI transactions by keyword. Statement layouts
 * vary; treat results as a draft the user can correct in the app.
 */
class GenericBankStatementParser(private val defaultBank: String = "Bank") : StatementParser {

    override fun parse(text: String): ParsedStatement {
        val bank = detectBank(text) ?: defaultBank
        val txs = ArrayList<ParsedTransaction>()
        for (raw in text.lines()) {
            val line = raw.trim()
            if (line.length < 8) continue
            val m = TX_LINE.find(line) ?: continue
            val desc = m.groupValues[2].trim()
            val amountPaise = parseAmountPaise(m.groupValues[3]) ?: continue
            if (amountPaise <= 0) continue
            val marker = m.groupValues[4].uppercase()
            val tag = tagFor(desc)
            val direction = directionFor(marker, desc, tag) ?: continue
            txs += ParsedTransaction(description = desc, amountPaise = amountPaise, direction = direction, tag = tag)
        }
        return ParsedStatement(bankName = bank, periodLabel = detectPeriod(text), transactions = txs)
    }

    private fun directionFor(marker: String, desc: String, tag: TxTag): Direction? = when {
        marker == "DR" -> Direction.DEBIT
        marker == "CR" -> Direction.CREDIT
        tag == TxTag.SALARY -> Direction.CREDIT
        CREDIT_HINT.containsMatchIn(desc) -> Direction.CREDIT
        DEBIT_HINT.containsMatchIn(desc) -> Direction.DEBIT
        else -> null // unknown direction — skip rather than guess
    }

    private fun tagFor(desc: String): TxTag {
        val d = desc.uppercase()
        return when {
            d.contains("SALARY") || d.contains("SAL ") -> TxTag.SALARY
            d.contains("EMI") || d.contains("NACH") || d.contains("ACH DR") || d.contains("ECS") -> TxTag.EMI
            d.contains("RENT") -> TxTag.RENT
            d.contains("UPI") -> TxTag.UPI
            d.contains("CARD") || d.contains("CC PMT") || d.contains("VISA") || d.contains("MASTER") -> TxTag.CARD
            d.contains("NEFT") || d.contains("IMPS") || d.contains("RTGS") -> TxTag.TRANSFER
            else -> TxTag.OTHER
        }
    }

    private fun parseAmountPaise(raw: String): Long? =
        raw.replace(",", "").toDoubleOrNull()?.let { (it * 100).roundToLong() }

    private fun detectBank(text: String): String? {
        val head = text.take(2000).uppercase()
        return when {
            "HDFC" in head -> "HDFC Bank"
            "ICICI" in head -> "ICICI Bank"
            "STATE BANK" in head || "SBI " in head -> "State Bank of India"
            "AXIS" in head -> "Axis Bank"
            "KOTAK" in head -> "Kotak Mahindra"
            "IDFC" in head -> "IDFC First"
            "INDUSIND" in head -> "IndusInd"
            "YES BANK" in head -> "Yes Bank"
            "FEDERAL BANK" in head -> "Federal Bank"
            "PUNJAB NATIONAL" in head || "PNB " in head -> "Punjab National Bank"
            else -> null
        }
    }

    private fun detectPeriod(text: String): String? =
        PERIOD.find(text)?.groupValues?.get(1)

    private companion object {
        // date (optional) + description + amount + optional Dr/Cr marker
        val TX_LINE = Regex(
            """^(?:(\d{2}[/-]\d{2}[/-]\d{2,4})\s+)?(.+?)\s+([\d,]+\.\d{2})\s*(Dr|Cr|DR|CR)?\s*$"""
        )
        val CREDIT_HINT = Regex("""(?i)received|credited|credit|refund|cashback|deposit""")
        val DEBIT_HINT = Regex("""(?i)paid|debit|withdrawn|purchase|payment""")
        val PERIOD = Regex("""(?i)statement\s+(?:period|from)?\s*[:\-]?\s*([^\n]{6,40})""")
    }
}

object StatementParsers {
    val defaults: List<StatementParser> = listOf(GenericBankStatementParser())

    /** First parser yielding at least 3 transactions wins; otherwise the richest result. */
    fun parseBest(text: String, parsers: List<StatementParser> = defaults): ParsedStatement {
        var best: ParsedStatement? = null
        for (p in parsers) {
            val r = p.parse(text)
            if (r.transactions.size >= 3) return r
            if (best == null || r.transactions.size > best!!.transactions.size) best = r
        }
        return best ?: ParsedStatement("Bank", null, emptyList())
    }
}
