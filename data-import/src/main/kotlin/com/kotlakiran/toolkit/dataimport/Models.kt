package com.kotlakiran.toolkit.dataimport

enum class Direction { CREDIT, DEBIT }

enum class TxTag { SALARY, EMI, RENT, CARD, UPI, TRANSFER, OTHER }

data class ParsedTransaction(
    val description: String,
    val amountPaise: Long,
    val direction: Direction,
    val epochDay: Long? = null,
    val tag: TxTag = TxTag.OTHER,
)

data class ParsedStatement(
    val bankName: String,
    val periodLabel: String?,
    val transactions: List<ParsedTransaction>,
) {
    val salaryCredits: List<ParsedTransaction> get() = transactions.filter { it.tag == TxTag.SALARY }
    val emiDebits: List<ParsedTransaction> get() = transactions.filter { it.tag == TxTag.EMI }
    val totalInPaise: Long get() = transactions.filter { it.direction == Direction.CREDIT }.sumOf { it.amountPaise }
    val totalOutPaise: Long get() = transactions.filter { it.direction == Direction.DEBIT }.sumOf { it.amountPaise }
}
