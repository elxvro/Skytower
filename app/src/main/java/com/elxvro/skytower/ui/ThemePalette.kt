package com.elxvro.skytower.ui

data class SkyTheme(
    val name: String,
    val skyTop: Int,
    val skyBottom: Int,
    val cloud: Int,
    val islandRock: Int,
    val islandGrass: Int,
    val blocks: IntArray,
)

object ThemePalette {
    val themes = listOf(
        SkyTheme(
            name = "Gündüz",
            skyTop = 0xFF168BFF.toInt(),
            skyBottom = 0xFFBDEBFF.toInt(),
            cloud = 0xEFFFFFFF.toInt(),
            islandRock = 0xFF716B85.toInt(),
            islandGrass = 0xFF56C96B.toInt(),
            blocks = intArrayOf(
                0xFF24A7FF.toInt(), 0xFFFF4FA3.toInt(), 0xFFFFC928.toInt(),
                0xFF7F5CFF.toInt(), 0xFF43CF6E.toInt(), 0xFFFF7A32.toInt(),
            ),
        ),
        SkyTheme(
            name = "Gün Batımı",
            skyTop = 0xFF6A47E8.toInt(),
            skyBottom = 0xFFFFB66B.toInt(),
            cloud = 0xEFFFF2E8.toInt(),
            islandRock = 0xFF6B5871.toInt(),
            islandGrass = 0xFF6ED56D.toInt(),
            blocks = intArrayOf(
                0xFF44C2FF.toInt(), 0xFFFF5B8F.toInt(), 0xFFFFD43B.toInt(),
                0xFF9B70FF.toInt(), 0xFF54D48A.toInt(), 0xFFFF8546.toInt(),
            ),
        ),
        SkyTheme(
            name = "Gece",
            skyTop = 0xFF101A4A.toInt(),
            skyBottom = 0xFF394A99.toInt(),
            cloud = 0xBFDDE7FF.toInt(),
            islandRock = 0xFF424666.toInt(),
            islandGrass = 0xFF40B878.toInt(),
            blocks = intArrayOf(
                0xFF2CD4FF.toInt(), 0xFFFF4FB8.toInt(), 0xFFFFD55A.toInt(),
                0xFF8B78FF.toInt(), 0xFF4EE29A.toInt(), 0xFFFF785A.toInt(),
            ),
        ),
    )

    fun get(themeId: Int): SkyTheme = themes[Math.floorMod(themeId, themes.size)]
}
