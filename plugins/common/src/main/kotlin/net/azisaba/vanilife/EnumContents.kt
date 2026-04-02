package net.azisaba.vanilife

import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import kotlin.reflect.KClass

abstract class EnumContents<T>(kClass: KClass<T>) : Contents<T> where T : Enum<T>, T : Keyed {
    private val byKey: Map<Key, T> = kClass.java.enumConstants.associateBy { it.key() }

    override fun byKey(key: Key): T? = byKey[key]

    override fun iterator(): Iterator<T> = byKey.values.iterator()
}
