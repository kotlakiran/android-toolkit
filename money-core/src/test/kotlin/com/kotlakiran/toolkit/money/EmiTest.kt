package com.kotlakiran.toolkit.money

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EmiTest {

    // Home loan used across the UI mock: 36.2L outstanding, 8.5% floating, 190 months left.
    private val home = LoanInput(principalPaise = 362_000_000, annualRatePct = 8.5, tenureMonths = 190)

    @Test
    fun emiMatchesKnownValue() {
        val emi = Emi.monthlyEmiPaise(home.principalPaise, home.annualRatePct, home.tenureMonths)
        assertTrue(emi in 3_472_300..3_472_500, "emi=$emi")
    }

    @Test
    fun zeroRateIsLinear() {
        assertEquals(1_905_263, Emi.monthlyEmiPaise(362_000_000, 0.0, 190))
    }

    @Test
    fun rateShockHalfPercent() {
        val s = Emi.rateShock(home, 0.5)
        assertTrue(s.newEmiPaise in 3_580_700..3_580_900, "newEmi=${s.newEmiPaise}")
        assertTrue(s.tenureMonthsIfEmiFixed!! in 203..205, "tenure=${s.tenureMonthsIfEmiFixed}")
    }

    @Test
    fun rateShockTwoPercent() {
        val s = Emi.rateShock(home, 2.0)
        assertTrue(s.emiDeltaPaise in 443_000..446_000, "delta=${s.emiDeltaPaise}")
    }

    @Test
    fun prepayFiveThousandMonthly() {
        val p = Emi.prepayByExtraMonthly(home, 500_000) // ₹5,000/month extra
        assertTrue(p.monthsSaved in 42..44, "monthsSaved=${p.monthsSaved}")
        assertTrue(p.interestSavedPaise in 74_000_000..78_000_000, "saved=${p.interestSavedPaise}")
    }

    @Test
    fun lumpSumReduceEmiKeepsTenure() {
        val r = Emi.prepayByLumpSum(home, 10_000_000, Emi.LumpSumMode.REDUCE_EMI) // ₹1L
        assertTrue(r.newEmiPaise!! < 3_471_400)
        assertTrue(r.interestSavedPaise > 0)
    }

    @Test
    fun lumpSumReduceTenureKeepsEmi() {
        val r = Emi.prepayByLumpSum(home, 10_000_000, Emi.LumpSumMode.REDUCE_TENURE)
        assertTrue(r.newTenureMonths!! < 190)
        assertTrue(r.interestSavedPaise > 0)
    }

    @Test
    fun refinanceBreakEven() {
        val q = Emi.refinance(home, newRatePct = 7.9, switchCostPaise = 1_200_000) // ₹12,000 cost
        assertTrue(q.monthlySavingPaise in 127_000..128_500, "monthly=${q.monthlySavingPaise}")
        assertEquals(10, q.breakEvenMonths)
    }

    @Test
    fun hybridRateRiseCostsMoreThanStayingLow() {
        // 36 months fixed at 8.25%, then floating at 8.5% for the rest.
        // EMI stays sized for 8.25%, so tenure extends past the nominal schedule.
        val hybrid = Emi.summarize(home.copy(annualRatePct = 8.25, rateType = RateType.HYBRID, hybridFixedMonths = 36, hybridLaterRatePct = 8.5))
        val allLow = Emi.summarize(home.copy(annualRatePct = 8.25))
        assertTrue(hybrid.totalInterestPaise > allLow.totalInterestPaise, "hybrid=${hybrid.totalInterestPaise} low=${allLow.totalInterestPaise}")
        assertTrue(hybrid.payoffMonths > 190, "payoffMonths=${hybrid.payoffMonths}")
    }

    @Test
    fun emiBelowInterestNeverRepays() {
        assertNull(Emi.monthsForEmi(362_000_000, 40.0, 1_000))
    }

    @Test
    fun safeBandDefaults() {
        val (lo, hi) = Emi.safeEmiBandPaise(14_500_000) // ₹1.45L take-home
        assertEquals(5_075_000, lo)
        assertEquals(5_800_000, hi)
    }
}
