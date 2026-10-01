package com.elxvro.skytower.ui

import com.elxvro.skytower.game.DailyMission
import com.elxvro.skytower.game.DailyMissionRules
import com.elxvro.skytower.game.DailyMissionState
import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenInteractionStateTest {
    @Test fun themeStateDistinguishesSelectedOwnedAffordableAndLocked() {
        assertEquals(ItemActionState.SELECTED, ScreenInteractionState.theme(1, 1, 0b11, 0, 0))
        assertEquals(ItemActionState.SELECT, ScreenInteractionState.theme(0, 1, 0b11, 0, 0))
        assertEquals(ItemActionState.BUY, ScreenInteractionState.theme(3, 0, 0b111, 100, 60))
        assertEquals(ItemActionState.LOCKED, ScreenInteractionState.theme(5, 0, 0b111, 20, 200))
    }

    @Test fun dailyStateBecomesClaimableOnlyWhenCompleteAndUnclaimed() {
        val progress = DailyMissionState(dateKey = "2026-10-01", placements = 10)
        assertEquals(ItemActionState.CLAIM, ScreenInteractionState.daily(progress, DailyMission.BLOCKS))
        val claimed = DailyMissionRules.claimMission(progress, "2026-10-01", DailyMission.BLOCKS).state
        assertEquals(ItemActionState.CLAIMED, ScreenInteractionState.daily(claimed, DailyMission.BLOCKS))
        assertEquals(ItemActionState.LOCKED, ScreenInteractionState.daily(progress, DailyMission.PERFECTS))
    }

    @Test fun levelRewardIsClaimableOnlyWhenReachedAndUnclaimed() {
        assertEquals(ItemActionState.CLAIM, ScreenInteractionState.level(currentLevel = 5, rewardLevel = 3, claimed = false))
        assertEquals(ItemActionState.CLAIMED, ScreenInteractionState.level(currentLevel = 5, rewardLevel = 3, claimed = true))
        assertEquals(ItemActionState.LOCKED, ScreenInteractionState.level(currentLevel = 2, rewardLevel = 3, claimed = false))
    }
}
