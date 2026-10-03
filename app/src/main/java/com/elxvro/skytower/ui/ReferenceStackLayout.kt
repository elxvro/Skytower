package com.elxvro.skytower.ui

data class ReferenceBlockSpec(
    val color: Int,
    val width: Float,
)

data class ReferenceStackSpec(
    val blocks: List<ReferenceBlockSpec>,
    val platformGrassHeight: Float,
    val platformRockDepth: Float,
)

object ReferenceStackLayout {
    private val classicColors = intArrayOf(
        0xFF2D9EF2.toInt(),
        0xFF92562E.toInt(),
        0xFF58C936.toInt(),
        0xFFFFC62D.toInt(),
        0xFFF24C70.toInt(),
        0xFFF2EFE8.toInt(),
    )

    fun home(): ReferenceStackSpec = stackFor(classicColors)

    fun preview(palette: IntArray): ReferenceStackSpec {
        val safePalette = if (palette.isEmpty()) classicColors else palette
        val colors = IntArray(6) { index -> safePalette[index % safePalette.size] }
        return stackFor(colors)
    }

    private fun stackFor(colors: IntArray): ReferenceStackSpec {
        val widths = floatArrayOf(1.00f, 0.985f, 0.97f, 0.955f, 0.94f, 0.925f)
        return ReferenceStackSpec(
            blocks = List(6) { index -> ReferenceBlockSpec(colors[index], widths[index]) },
            platformGrassHeight = 0.12f,
            platformRockDepth = 0.27f,
        )
    }
}
