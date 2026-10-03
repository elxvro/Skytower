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
            name = "Klasik",
            skyTop = ReferenceDesignTokens.SKY_TOP,
            skyBottom = ReferenceDesignTokens.SKY_BOTTOM,
            cloud = 0xF7FFFFFF.toInt(),
            islandRock = 0xFF6E748A.toInt(),
            islandGrass = 0xFF55D468.toInt(),
            blocks = intArrayOf(
                0xFF2D9EF2.toInt(),
                0xFF92562E.toInt(),
                0xFF58C936.toInt(),
                0xFFFFC62D.toInt(),
                0xFFF24C70.toInt(),
                0xFFF2EFE8.toInt(),
            ),
        ),
        SkyTheme(
            name = "Gün Batımı",
            skyTop = 0xFF6A47E8.toInt(),
            skyBottom = 0xFFFFB66B.toInt(),
            cloud = 0xF5FFF2E8.toInt(),
            islandRock = 0xFF6B5871.toInt(),
            islandGrass = 0xFF6ED56D.toInt(),
            blocks = intArrayOf(
                0xFF44C2FF.toInt(), 0xFF7B5CC4.toInt(), 0xFF54D48A.toInt(),
                0xFFFFD43B.toInt(), 0xFFFF5B8F.toInt(), 0xFFFFE8C7.toInt(),
            ),
        ),
        SkyTheme(
            name = "Gece",
            skyTop = 0xFF101A4A.toInt(),
            skyBottom = 0xFF394A99.toInt(),
            cloud = 0xE8DDE7FF.toInt(),
            islandRock = 0xFF424666.toInt(),
            islandGrass = 0xFF40B878.toInt(),
            blocks = intArrayOf(
                0xFF2CD4FF.toInt(), 0xFF6A58B5.toInt(), 0xFF4EE29A.toInt(),
                0xFFFFD55A.toInt(), 0xFFFF4FB8.toInt(), 0xFFD6DCF7.toInt(),
            ),
        ),
        SkyTheme(
            name = "Neon",
            skyTop = 0xFF190B3D.toInt(),
            skyBottom = 0xFF522282.toInt(),
            cloud = 0xDFAEEBFF.toInt(),
            islandRock = 0xFF3F315E.toInt(),
            islandGrass = 0xFF2FE0C0.toInt(),
            blocks = intArrayOf(
                0xFF00E5FF.toInt(), 0xFF58365F.toInt(), 0xFF37F4A2.toInt(),
                0xFFFFB51F.toInt(), 0xFFFF2BC2.toInt(), 0xFFD9FAFF.toInt(),
            ),
        ),
        SkyTheme(
            name = "Uzay",
            skyTop = 0xFF050818.toInt(),
            skyBottom = 0xFF1C285D.toInt(),
            cloud = 0xD5C7D8FF.toInt(),
            islandRock = 0xFF30364F.toInt(),
            islandGrass = 0xFF6A78A8.toInt(),
            blocks = intArrayOf(
                0xFF5FE3FF.toInt(), 0xFF7568A8.toInt(), 0xFF65F2B2.toInt(),
                0xFFFFD86A.toInt(), 0xFFFF6BB5.toInt(), 0xFFDDE8FF.toInt(),
            ),
        ),
        SkyTheme(
            name = "Aurora",
            skyTop = 0xFF092E3C.toInt(),
            skyBottom = 0xFF30245F.toInt(),
            cloud = 0xE5E4FFF6.toInt(),
            islandRock = 0xFF3F5362.toInt(),
            islandGrass = 0xFF62E4B0.toInt(),
            blocks = intArrayOf(
                0xFF58FFD0.toInt(), 0xFF6D6B99.toInt(), 0xFF7AB8FF.toInt(),
                0xFFFFE676.toInt(), 0xFFFF82CE.toInt(), 0xFFE7FFF8.toInt(),
            ),
        ),
    )

    fun get(themeId: Int): SkyTheme = themes[Math.floorMod(themeId, themes.size)]
}
