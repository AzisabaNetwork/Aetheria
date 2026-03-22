package net.azisaba.vanilife.islands.enchantment

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

interface EnchantmentAccessor {
    val enchantments: Set<TypedKey<Enchantment>>

    suspend fun addEnchantment(enchantment: TypedKey<Enchantment>)

    suspend fun removeEnchantment(enchantment: TypedKey<Enchantment>)

    suspend fun clearEnchantments()

    fun rollEnchantments(itemStack: ItemStack, random: Random = Random.Default, maxCount: Int = 3): List<TypedKey<Enchantment>> = buildList {
        if (maxCount <= 0) return@buildList

        val itemKey = RegistryKey.ITEM.typedKey(itemStack.type.key)
        val weighted = enchantments
            .mapNotNull { key ->
                RegistryAccess.registryAccess()
                    .getRegistry(RegistryKey.ENCHANTMENT)[key]
                    ?.takeIf { enchantment ->
                        enchantment.getPrimaryItems()?.contains(itemKey) ?: enchantment.canEnchantItem(itemStack)
                    }
                    ?.weight
                    ?.takeIf { it > 0 }
                    ?.let { key to it }
            }
            .toMutableList()

        repeat(minOf(maxCount, weighted.size)) {
            val totalWeight = weighted.sumOf { it.second }
            if (totalWeight <= 0) return@repeat

            var roll = random.nextInt(totalWeight)
            val index = weighted.indexOfFirst { (_, weight) ->
                roll -= weight
                roll < 0
            }

            if (index >= 0) {
                add(weighted.removeAt(index).first)
            }
        }
    }
}
