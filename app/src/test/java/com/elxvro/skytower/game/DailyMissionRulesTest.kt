package com.elxvro.skytower.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyMissionRulesTest {
    @Test fun blocksMissionCompletesAtTenAndPays20Once() {
        val state = DailyMissionRules.update(DailyMissionState(), "2026-10-01", placements = 10, perfects = 0, score = 10)
        val first = DailyMissionRules.claimMission(state, "2026-10-01", DailyMission.BLOCKS)
        val second = DailyMissionRules.claimMission(first.state, "2026-10-01", DailyMission.BLOCKS)
        assertEquals(20, first.coinsAwarded)
        assertEquals(0, second.coinsAwarded)
    }

    @Test fun perfectMissionCompletesAtThreeAndPays25Once() {
        val state = DailyMissionRules.update(DailyMissionState(), "2026-10-01", placements = 3, perfects = 3, score = 3)
        val first = DailyMissionRules.claimMission(state, "2026-10-01", DailyMission.PERFECTS)
        val second = DailyMissionRules.claimMission(first.state, "2026-10-01", DailyMission.PERFECTS)
        assertEquals(25, first.coinsAwarded)
        assertEquals(0, second.coinsAwarded)
    }

    @Test fun scoreMissionCompletesAt50AndPays40Once() {
        val state = DailyMissionRules.update(DailyMissionState(), "2026-10-01", placements = 8, perfects = 1, score = 50)
        val first = DailyMissionRules.claimMission(state, "2026-10-01", DailyMission.SCORE)
        val second = DailyMissionRules.claimMission(first.state, "2026-10-01", DailyMission.SCORE)
        assertEquals(40, first.coinsAwarded)
        assertEquals(0, second.coinsAwarded)
    }

    @Test fun allCompletePays100OnceAfterAllThreeClaims() {
        var state = DailyMissionRules.update(DailyMissionState(), "2026-10-01", placements = 10, perfects = 3, score = 50)
        for (mission in DailyMission.entries) state = DailyMissionRules.claimMission(state, "2026-10-01", mission).state
        val first = DailyMissionRules.claimAllComplete(state, "2026-10-01")
        val second = DailyMissionRules.claimAllComplete(first.state, "2026-10-01")
        assertEquals(100, first.coinsAwarded)
        assertEquals(0, second.coinsAwarded)
    }

    @Test fun dayFiveStreakPays150Once() {
        var state = DailyMissionState()
        for (day in 1..5) {
            val date = "2026-10-0$day"
            state = DailyMissionRules.update(state, date, placements = 10, perfects = 0, score = 10)
            val claim = DailyMissionRules.claimMission(state, date, DailyMission.BLOCKS)
            state = claim.state
            if (day < 5) assertEquals(0, claim.streakBonusAwarded) else assertEquals(150, claim.streakBonusAwarded)
        }
        val repeated = DailyMissionRules.claimMission(state, "2026-10-05", DailyMission.BLOCKS)
        assertEquals(0, repeated.streakBonusAwarded)
    }

    @Test fun sameDateClaimsAdvanceStreakOnlyOnce() {
        var state = DailyMissionRules.update(DailyMissionState(), "2026-10-01", placements = 10, perfects = 3, score = 50)
        state = DailyMissionRules.claimMission(state, "2026-10-01", DailyMission.BLOCKS).state
        val second = DailyMissionRules.claimMission(state, "2026-10-01", DailyMission.PERFECTS)
        assertEquals(1, second.state.streakCount)
    }

    @Test fun missingDayResetsStreakToOne() {
        var state = DailyMissionRules.update(DailyMissionState(), "2026-10-01", placements = 10, perfects = 0, score = 10)
        state = DailyMissionRules.claimMission(state, "2026-10-01", DailyMission.BLOCKS).state
        state = DailyMissionRules.update(state, "2026-10-03", placements = 10, perfects = 0, score = 10)
        state = DailyMissionRules.claimMission(state, "2026-10-03", DailyMission.BLOCKS).state
        assertEquals(1, state.streakCount)
    }

    @Test fun newDateStartsFreshDailyProgress() {
        var state = DailyMissionRules.update(DailyMissionState(), "2026-10-01", placements = 7, perfects = 2, score = 22)
        state = DailyMissionRules.update(state, "2026-10-02", placements = 1, perfects = 0, score = 1)
        assertEquals(1, state.placements)
        assertEquals(0, state.perfects)
        assertEquals(1, state.bestScore)
        assertEquals(0, state.claimedMask)
        assertFalse(state.allCompleteClaimed)
    }

    @Test fun backwardDateDoesNotRepaySeenDate() {
        var state = DailyMissionRules.update(DailyMissionState(), "2026-10-02", placements = 10, perfects = 0, score = 10)
        state = DailyMissionRules.claimMission(state, "2026-10-02", DailyMission.BLOCKS).state
        val backwards = DailyMissionRules.update(state, "2026-10-01", placements = 10, perfects = 0, score = 10)
        val claim = DailyMissionRules.claimMission(backwards, "2026-10-01", DailyMission.BLOCKS)
        assertEquals("2026-10-02", claim.state.dateKey)
        assertEquals(0, claim.coinsAwarded)
        assertTrue(claim.state.claimedMask != 0)
    }
}
