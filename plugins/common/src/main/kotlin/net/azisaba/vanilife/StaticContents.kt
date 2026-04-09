package net.azisaba.vanilife

import net.kyori.adventure.key.Key
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

abstract class StaticContents<T : Any> : Contents<T> {
    private val byKey: ConcurrentMap<Key, T> = ConcurrentHashMap()

    override fun byKey(key: Key): T? = byKey[key]

    override fun iterator(): Iterator<T> = byKey.values.toSet().iterator()

    protected fun <S : T> bind(key: Key, value: S): S {
        val previous = byKey.putIfAbsent(key, value)
        check(previous == null) {
            "Key $key is already defined"
        }
        return value
    }
}
