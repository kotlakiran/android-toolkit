package com.kotlakiran.toolkit.money

import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToLong

enum class RateType { FIXED, FLOATING, HYBRID }

data class LoanInput(
    val principalPaise: Long,
    val annualRatePct: Double,
    val tenureMonths: Int,
    val rateType: RateType = RateType.FLOATING,
    /** HYBRID only: months at annualRatePct before switching to hybridLaterRatePct. */
    val hybridFixedMonths: Int = 0,
    val hybridLaterRatePct: Double = 0.0,
)

data class YearBreakdown(val year: Int, val principalPaise: Long, val interestPaise: Long, val balancePaise: Long)

data class LoanSummary(
    val emiPaise: Long,
    val totalPayablePaise: Long,
    val totalInterestPaise: Long,
    val payoffMonths: Int,
    val yearly: List<YearBreakdown>,
)

object Emi {

    fun monthlyEmiPaise(principalPaise: Long, annualRatePct: Double, months: Int): Long {
        require(principalPaise >= 0) { "principal must be >= 0" }
        require(months > 0) { "tenure must be > 0" }
        if (principalPaise == 0L) return 0
        val r = annualRatePct / 1200.0
        if (r == 0.0) return (principalPaise.toDouble() / months).roundToLong()
        val f = (1.0 + r).pow(months)
        return (principalPaise * r * f / (f - 1.0)).roundToLong()
    }

    /** Month-by-month amortization; handles FIXED, FLOATING and HYBRID rate switches. */
    fun summarize(input: LoanInput): LoanSummary {
        val emi = monthlyEmiPaise(input.principalPaise, input.annualRatePct, input.tenureMonths)
        var balance = input.principalPaise
        var totalInterest = 0L
        var month = 0
        val yearly = ArrayList<YearBreakdown>()
        var yPrincipal = 0L
        var yInterest = 0L
        var year = 1
        while (balance > 0 && month < input.tenureMonths + 600) {
            val ratePct = if (input.rateType == RateType.HYBRID && input.hybridLaterRatePct > 0 && month >= input.hybridFixedMonths) {
                input.hybridLaterRatePct
            } else {
                input.annualRatePct
            }
            val r = ratePct / 1200.0
            val interest = (balance * r).roundToLong()
            var principal = emi - interest
            if (principal <= 0) break // EMI does not cover interest
            if (principal > balance) principal = balance
            balance -= principal
            totalInterest += interest
            yPrincipal += principal
            yInterest += interest
            month++
            if (month % 12 == 0 || balance == 0L) {
                yearly += YearBreakdown(year, yPrincipal, yInterest, balance)
                year++
                yPrincipal = 0
                yInterest = 0
            }
        }
        return LoanSummary(emi, emi * month, totalInterest, month, yearly)
    }

    /** Months needed to repay at a given EMI. Null if the EMI does not cover interest. */
    fun monthsForEmi(principalPaise: Long, annualRatePct: Double, emiPaise: Long): Int? {
        val r = annualRatePct / 1200.0
        if (r <= 0.0) return ceil(principalPaise.toDouble() / emiPaise).toInt()
        val interestCover = principalPaise * r
        if (emiPaise <= interestCover) return null
        val n = ln(emiPaise / (emiPaise - interestCover)) / ln(1.0 + r)
        return ceil(n).toInt()
    }

    data class RateShock(
        val newRatePct: Double,
        val newEmiPaise: Long,
        val emiDeltaPaise: Long,
        /** New total tenure if the EMI is kept fixed instead. Null = never repays. */
        val tenureMonthsIfEmiFixed: Int?,
    )

    fun rateShock(input: LoanInput, deltaPct: Double): RateShock {
        val base = monthlyEmiPaise(input.principalPaise, input.annualRatePct, input.tenureMonths)
        val newRate = input.annualRatePct + deltaPct
        val newEmi = monthlyEmiPaise(input.principalPaise, newRate, input.tenureMonths)
        return RateShock(
            newRatePct = newRate,
            newEmiPaise = newEmi,
            emiDeltaPaise = newEmi - base,
            tenureMonthsIfEmiFixed = monthsForEmi(input.principalPaise, newRate, base),
        )
    }

    data class PrepayResult(
        val newTenureMonths: Int,
        val monthsSaved: Int,
        val interestSavedPaise: Long,
        val newTotalInterestPaise: Long,
    )

    /** Extra amount paid every month on top of the EMI; tenure shrinks, EMI stays. */
    fun prepayByExtraMonthly(input: LoanInput, extraMonthlyPaise: Long): PrepayResult {
        require(extraMonthlyPaise > 0) { "extra must be > 0" }
        val base = summarize(input)
        val boosted = base.emiPaise + extraMonthlyPaise
        val months = monthsForEmi(input.principalPaise, input.annualRatePct, boosted) ?: input.tenureMonths
        val newInterest = boosted * months - input.principalPaise
        return PrepayResult(
            newTenureMonths = months,
            monthsSaved = (input.tenureMonths - months).coerceAtLeast(0),
            interestSavedPaise = base.totalInterestPaise - newInterest,
            newTotalInterestPaise = newInterest,
        )
    }

    enum class LumpSumMode { REDUCE_TENURE, REDUCE_EMI }

    data class LumpSumResult(val newEmiPaise: Long?, val newTenureMonths: Int?, val interestSavedPaise: Long)

    fun prepayByLumpSum(input: LoanInput, lumpSumPaise: Long, mode: LumpSumMode): LumpSumResult {
        val base = summarize(input)
        val newPrincipal = input.principalPaise - lumpSumPaise
        require(newPrincipal > 0) { "lump sum exceeds principal" }
        return when (mode) {
            LumpSumMode.REDUCE_TENURE -> {
                val months = monthsForEmi(newPrincipal, input.annualRatePct, base.emiPaise) ?: input.tenureMonths
                val newInterest = base.emiPaise * months - newPrincipal
                LumpSumResult(null, months, base.totalInterestPaise - newInterest)
            }
            LumpSumMode.REDUCE_EMI -> {
                val newEmi = monthlyEmiPaise(newPrincipal, input.annualRatePct, input.tenureMonths)
                val newInterest = newEmi * input.tenureMonths - newPrincipal
                LumpSumResult(newEmi, null, base.totalInterestPaise - newInterest)
            }
        }
    }

    data class RefinanceQuote(
        val newEmiPaise: Long,
        val monthlySavingPaise: Long,
        val totalSavingPaise: Long,
        /** Months for the monthly saving to cover switchCostPaise. Int.MAX_VALUE if never. */
        val breakEvenMonths: Int,
    )

    fun refinance(input: LoanInput, newRatePct: Double, switchCostPaise: Long): RefinanceQuote {
        val oldEmi = monthlyEmiPaise(input.principalPaise, input.annualRatePct, input.tenureMonths)
        val newEmi = monthlyEmiPaise(input.principalPaise, newRatePct, input.tenureMonths)
        val monthlySaving = oldEmi - newEmi
        val oldInterest = oldEmi * input.tenureMonths - input.principalPaise
        val newInterest = newEmi * input.tenureMonths - input.principalPaise
        val breakEven = if (monthlySaving > 0) ceil(switchCostPaise.toDouble() / monthlySaving).toInt() else Int.MAX_VALUE
        return RefinanceQuote(
            newEmiPaise = newEmi,
            monthlySavingPaise = monthlySaving,
            totalSavingPaise = oldInterest - newInterest - switchCostPaise,
            breakEvenMonths = breakEven,
        )
    }

    /** Comfort band: EMIs should stay within [lowPct, highPct] of take-home pay. */
    fun safeEmiBandPaise(takeHomeMonthlyPaise: Long, lowPct: Double = 0.35, highPct: Double = 0.40): Pair<Long, Long> =
        (takeHomeMonthlyPaise * lowPct).roundToLong() to (takeHomeMonthlyPaise * highPct).roundToLong()
}
