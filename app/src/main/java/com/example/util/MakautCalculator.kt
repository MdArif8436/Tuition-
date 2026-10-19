package com.example.util

import java.util.Locale

object MakautCalculator {
    /**
     * Statutory MAKAUT (WBUT) formula for converting CGPA or SGPA to Percentage:
     * Percentage = (CGPA - 0.75) * 10
     */
    fun cgpaToPercentage(cgpa: Double): Double {
        if (cgpa <= 0.75) return 0.0
        val percentage = (cgpa - 0.75) * 10.0
        return (percentage * 100.0).toLong() / 100.0 // 2 decimal precision
    }

    /**
     * Inverse calculation: CGPA = (Percentage / 10) + 0.75
     */
    fun percentageToCgpa(percentage: Double): Double {
        if (percentage <= 0.0) return 0.0
        val cgpa = (percentage / 10.0) + 0.75
        return (cgpa * 100.0).toLong() / 100.0
    }

    /**
     * Official MAKAUT 10-point letter grading scale:
     * 9.00 - 10.00: Outstanding (O)
     * 8.00 - 8.99: Excellent (E)
     * 7.00 - 7.99: Very Good (A)
     * 6.00 - 6.99: Good (B)
     * 5.00 - 5.99: Average (C)
     * 4.00 - 4.99: Pass (D)
     * Below 4.00: Fail (F)
     */
    fun getGradeLetter(cgpa: Double): Pair<String, String> {
        return when {
            cgpa >= 9.0 -> "O" to "Outstanding"
            cgpa >= 8.0 -> "E" to "Excellent"
            cgpa >= 7.0 -> "A" to "Very Good"
            cgpa >= 6.0 -> "B" to "Good"
            cgpa >= 5.0 -> "C" to "Average"
            cgpa >= 4.0 -> "D" to "Pass"
            else -> "F" to "Fail"
        }
    }

    /**
     * Placement & Academic Standing remarks:
     * 6.75 CGPA is equivalent to 60.00% (The standard corporate campus recruitment threshold)
     */
    fun getPlacementEligibility(cgpa: Double): String {
        val pct = cgpaToPercentage(cgpa)
        return when {
            cgpa >= 8.5 -> "Top Tier: First Class with Distinction (Eligible for all Tier 1 tech placements)"
            cgpa >= 6.75 -> "Eligible: Meets standard corporate campus recruitment cutoff (>=60.00%)"
            cgpa >= 6.0 -> "Near Cutoff: Current ${String.format(Locale.US, "%.1f", pct)}%. Requires ${String.format(Locale.US, "%.2f", 6.75 - cgpa)} more CGPA for 60% placement cutoff"
            else -> "Remedial Attention: Below 6.0 CGPA. HOD guided improvement recommended"
        }
    }

    fun formatCurrency(amount: Double): String {
        return "₹" + String.format(Locale.US, "%,.0f", amount)
    }
}
