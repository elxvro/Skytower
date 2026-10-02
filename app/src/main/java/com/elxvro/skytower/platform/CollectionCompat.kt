package com.elxvro.skytower.platform

/** Compatibility helper for keeping the most recent bounded token window from persisted sets. */
internal fun <T> Set<T>.takeLast(count: Int): List<T> = toList().takeLast(count.coerceAtLeast(0))
