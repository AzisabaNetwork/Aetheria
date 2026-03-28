package net.azisaba.vanilife

import net.kyori.adventure.key.Key

interface Contents<T> {
    fun byKey(key: Key): T?

    fun byKeyOrThrow(key: Key): T = requireNotNull(byKey(key)) { "Content '$key' not found" }

    fun all(): Set<T>
}
