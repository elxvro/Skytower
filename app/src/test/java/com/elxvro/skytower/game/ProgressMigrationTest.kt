package com.elxvro.skytower.game

import com.elxvro.skytower.platform.LegacyProgressSnapshot
import com.elxvro.skytower.platform.ProgressMigration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressMigrationTest {
    @Test fun v04ValuesArePreservedWithoutRepayingLegacyMissions() {
        val legacy = LegacyProgressSnapshot(
            bestScore = 88,
            coins = 345,
            themeId = 4,
            unlockedThemeMask = 0b11111,
            soundEnabled = false,
            vibrationEnabled = true,
            tutorialSeen = true,
            legacyPlacements = 27,
            legacyPerfects = 9,
            legacyMissionClaimedMask = 0b111,
        )

        val migrated = ProgressMigration.migrate(legacy, currentDate = "2026-10-01")

        assertEquals(88, migrated.bestScore)
        assertEquals(345, migrated.coins)
        assertEquals(4, migrated.themeId)
        assertEquals(0b11111, migrated.unlockedThemeMask)
        assertFalse(migrated.soundEnabled)
        assertTrue(migrated.vibrationEnabled)
        assertTrue(migrated.tutorialSeen)
        assertEquals(27, migrated.totalPlacements)
        assertEquals(9, migrated.totalPerfects)
        assertEquals(0, migrated.totalRuns)
        assertEquals("2026-10-01", migrated.dailyState.dateKey)
        assertEquals(0, migrated.dailyState.placements)
        assertEquals(0, migrated.dailyState.claimedMask)
    }

    @Test fun classicAndCrystalAreOwnedAndClassicSelectedOnFirstMigration() {
        val migrated = ProgressMigration.migrate(LegacyProgressSnapshot(), "2026-10-01")
        assertTrue(SkinRules.isOwned(BlockSkin.CLASSIC, migrated.unlockedSkinMask))
        assertTrue(SkinRules.isOwned(BlockSkin.CRYSTAL, migrated.unlockedSkinMask))
        assertEquals(BlockSkin.CLASSIC, migrated.selectedSkin)
    }

    @Test fun negativeAndInvalidStoredValuesAreClampedToSafeDefaults() {
        val migrated = ProgressMigration.migrate(
            LegacyProgressSnapshot(
                bestScore = -5,
                coins = -10,
                themeId = 99,
                unlockedThemeMask = 0,
                legacyPlacements = -8,
                legacyPerfects = -3,
            ),
            currentDate = "2026-10-01",
        )
        assertEquals(0, migrated.bestScore)
        assertEquals(0, migrated.coins)
        assertEquals(0, migrated.themeId)
        assertEquals(0b111, migrated.unlockedThemeMask)
        assertEquals(0, migrated.totalPlacements)
        assertEquals(0, migrated.totalPerfects)
    }

    @Test fun existingV05TotalsWinWhenTheyAreGreaterThanLegacyCounters() {
        val existing = ProgressMigration.migrate(LegacyProgressSnapshot(), "2026-09-30").copy(
            totalPlacements = 90,
            totalPerfects = 25,
            totalRuns = 12,
            totalXp = 2200,
            selectedSkin = BlockSkin.CRYSTAL,
        )
        val migrated = ProgressMigration.migrate(
            LegacyProgressSnapshot(legacyPlacements = 10, legacyPerfects = 3),
            currentDate = "2026-10-01",
            existing = existing,
        )
        assertEquals(90, migrated.totalPlacements)
        assertEquals(25, migrated.totalPerfects)
        assertEquals(12, migrated.totalRuns)
        assertEquals(2200, migrated.totalXp)
        assertEquals(BlockSkin.CRYSTAL, migrated.selectedSkin)
    }
}
