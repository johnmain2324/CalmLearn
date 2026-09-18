package com.education.calmlearn.data.progress

import org.junit.Assert.assertEquals
import org.junit.Test

class StreakCalculatorTest {

    @Test
    fun `first ever activity starts streak at 1`() {
        assertEquals(1, StreakCalculator.computeStreak("2026-09-17", null, 0))
    }

    @Test
    fun `repeating an activity the same day does not change the streak`() {
        assertEquals(5, StreakCalculator.computeStreak("2026-09-17", "2026-09-17", 5))
    }

    @Test
    fun `activity on the very next calendar day extends the streak by one`() {
        assertEquals(6, StreakCalculator.computeStreak("2026-09-18", "2026-09-17", 5))
    }

    @Test
    fun `a gap of more than one day resets the streak to 1`() {
        assertEquals(1, StreakCalculator.computeStreak("2026-09-19", "2026-09-17", 5))
        assertEquals(1, StreakCalculator.computeStreak("2027-01-01", "2026-09-17", 40))
    }

    @Test
    fun `streak extends correctly across a month boundary`() {
        assertEquals(4, StreakCalculator.computeStreak("2026-02-01", "2026-01-31", 3))
    }

    @Test
    fun `streak extends correctly across a year boundary`() {
        assertEquals(11, StreakCalculator.computeStreak("2027-01-01", "2026-12-31", 10))
    }

    @Test
    fun `streak extends correctly across the leap day of a leap year`() {
        // 2028 is a leap year: Feb 29 exists, so Feb 28 -> Feb 29 is a consecutive day.
        assertEquals(2, StreakCalculator.computeStreak("2028-02-29", "2028-02-28", 1))
        // ...and Feb 29 -> Mar 1 is also consecutive.
        assertEquals(3, StreakCalculator.computeStreak("2028-03-01", "2028-02-29", 2))
    }

    @Test
    fun `a gap spanning the leap day still resets the streak`() {
        // 2027 is NOT a leap year, so Feb 28 -> Mar 1 (skipping the nonexistent Feb 29) is a gap.
        assertEquals(1, StreakCalculator.computeStreak("2027-03-01", "2027-02-27", 5))
    }

    @Test
    fun `previousDateString computes the calendar day before`() {
        assertEquals("2026-09-16", StreakCalculator.previousDateString("2026-09-17"))
        assertEquals("2026-01-31", StreakCalculator.previousDateString("2026-02-01"))
        assertEquals("2025-12-31", StreakCalculator.previousDateString("2026-01-01"))
        assertEquals("2028-02-29", StreakCalculator.previousDateString("2028-03-01"))
    }
}
