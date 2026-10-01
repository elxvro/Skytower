package com.elxvro.skytower.game

enum class PowerUpType {
    SECOND_CHANCE,
    SLOW_TIME,
    WIDE_PERFECT,
    COIN_MULTIPLIER,
}

data class PowerUpInventory(
    val secondChance: Int = 0,
    val slowTime: Int = 0,
    val widePerfect: Int = 0,
    val coinMultiplier: Int = 0,
) {
    fun count(type: PowerUpType): Int = when (type) {
        PowerUpType.SECOND_CHANCE -> secondChance
        PowerUpType.SLOW_TIME -> slowTime
        PowerUpType.WIDE_PERFECT -> widePerfect
        PowerUpType.COIN_MULTIPLIER -> coinMultiplier
    }.coerceAtLeast(0)

    fun withCount(type: PowerUpType, count: Int): PowerUpInventory {
        val safe = count.coerceAtLeast(0)
        return when (type) {
            PowerUpType.SECOND_CHANCE -> copy(secondChance = safe)
            PowerUpType.SLOW_TIME -> copy(slowTime = safe)
            PowerUpType.WIDE_PERFECT -> copy(widePerfect = safe)
            PowerUpType.COIN_MULTIPLIER -> copy(coinMultiplier = safe)
        }
    }
}

data class PowerUpShopState(
    val coins: Int = 0,
    val inventory: PowerUpInventory = PowerUpInventory(),
    val equipped: Set<PowerUpType> = emptySet(),
    val processedPurchaseTokens: Set<String> = emptySet(),
)

data class PowerUpPurchaseResult(val state: PowerUpShopState, val purchased: Boolean)
data class PowerUpConsumeResult(val state: PowerUpShopState, val consumed: Boolean)
data class PowerUpEquipResult(val state: PowerUpShopState, val changed: Boolean)

object PowerUpRules {
    const val MAX_EQUIPPED = 3

    fun price(type: PowerUpType): Int = when (type) {
        PowerUpType.SECOND_CHANCE -> 300
        PowerUpType.SLOW_TIME -> 250
        PowerUpType.WIDE_PERFECT -> 350
        PowerUpType.COIN_MULTIPLIER -> 400
    }

    fun purchase(state: PowerUpShopState, type: PowerUpType, requestToken: String): PowerUpPurchaseResult {
        if (requestToken.isNotBlank() && requestToken in state.processedPurchaseTokens) {
            return PowerUpPurchaseResult(state, false)
        }
        val safeCoins = state.coins.coerceAtLeast(0)
        val cost = price(type)
        if (safeCoins < cost) return PowerUpPurchaseResult(state.copy(coins = safeCoins), false)

        val inventory = state.inventory.withCount(type, state.inventory.count(type) + 1)
        val tokens = if (requestToken.isBlank()) state.processedPurchaseTokens else state.processedPurchaseTokens + requestToken
        return PowerUpPurchaseResult(
            state.copy(coins = safeCoins - cost, inventory = inventory, processedPurchaseTokens = tokens),
            true,
        )
    }

    fun consume(state: PowerUpShopState, type: PowerUpType): PowerUpConsumeResult {
        val count = state.inventory.count(type)
        if (count <= 0) return PowerUpConsumeResult(state.copy(coins = state.coins.coerceAtLeast(0)), false)
        val inventory = state.inventory.withCount(type, count - 1)
        val equipped = if (inventory.count(type) == 0) state.equipped - type else state.equipped
        return PowerUpConsumeResult(state.copy(inventory = inventory, equipped = equipped), true)
    }

    fun toggleEquip(state: PowerUpShopState, type: PowerUpType): PowerUpEquipResult {
        if (type in state.equipped) {
            return PowerUpEquipResult(state.copy(equipped = state.equipped - type), true)
        }
        if (state.inventory.count(type) <= 0 || state.equipped.size >= MAX_EQUIPPED) {
            return PowerUpEquipResult(state, false)
        }
        return PowerUpEquipResult(state.copy(equipped = state.equipped + type), true)
    }
}
