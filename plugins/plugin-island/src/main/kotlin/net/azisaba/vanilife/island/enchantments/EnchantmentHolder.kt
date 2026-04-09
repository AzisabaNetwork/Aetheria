package net.azisaba.vanilife.island.enchantments

import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import org.bukkit.enchantments.Enchantment

interface EnchantmentHolder {
    val enchantments: Set<TypedKey<Enchantment>>

    fun has(enchantment: TypedKey<Enchantment>): Boolean = enchantment in enchantments

    fun has(enchantment: Enchantment): Boolean = has(RegistryKey.ENCHANTMENT.typedKey(enchantment.key()))

    suspend fun addEnchantment(enchantment: TypedKey<Enchantment>): Boolean

    suspend fun addEnchantment(enchantment: Enchantment): Boolean = addEnchantment(
        RegistryKey.ENCHANTMENT.typedKey(enchantment.key())
    )

    suspend fun removeEnchantment(enchantment: TypedKey<Enchantment>): Boolean

    suspend fun removeEnchantment(enchantment: Enchantment): Boolean = removeEnchantment(
        RegistryKey.ENCHANTMENT.typedKey(enchantment.key())
    )

    suspend fun clearEnchantments()
}
