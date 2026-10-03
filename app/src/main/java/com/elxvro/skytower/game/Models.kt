package com.elxvro.skytower.game

data class Block(
    var x: Float,
    var y: Float,
    var width: Float,
    val height: Float,
    val paletteIndex: Int,
)

sealed interface PlacementResult {
    data object Miss : PlacementResult

    data class Success(
        val placedBlock: Block,
        val cutFragment: Block?,
        val perfect: Boolean,
    ) : PlacementResult
}

enum class RunMode {
    MENU,
    RUNNING,
    PAUSED,
    GAME_OVER,
}
