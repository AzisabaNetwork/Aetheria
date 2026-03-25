package net.azisaba.vanilife.npc.trading

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.azisaba.serialization.KeySerializer
import net.azisaba.vanilife.item.ServerItem
import net.kyori.adventure.key.Key
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType
import kotlin.random.Random

@Serializable
data class ItemStackProvider(
    @Serializable(with = KeySerializer::class) val type: Key,
    val amount: ItemStackAmountProvider,
) {
    fun provide(random: Random, registryAccess: RegistryAccess = RegistryAccess.registryAccess()): ItemStack {
        val amount = amount.sample(random)
        return when (val resolved = resolveType(registryAccess)) {
            is ItemType -> resolved.createItemStack(amount)
            is ServerItem -> ItemStack.of(resolved, amount)
            else -> error("Cannot resolve item type: $type")
        }
    }

    private fun resolveType(registryAccess: RegistryAccess): Any? =
        tryGetAsItemType(registryAccess) ?: tryGetAsServerItem(registryAccess)

    private fun tryGetAsItemType(registryAccess: RegistryAccess): ItemType? =
        registryAccess.getRegistry(RegistryKey.ITEM).get(type)

    private fun tryGetAsServerItem(registryAccess: RegistryAccess): ServerItem? =
        registryAccess.getRegistry(RegistryKey.SERVER_ITEM).get(type)
}

@Serializable
sealed interface ItemStackAmountProvider {
    fun sample(random: Random): Int

    @Serializable
    @SerialName("Constant")
    data class Constant(val value: Int) : ItemStackAmountProvider {
        override fun sample(random: Random): Int = value
    }

    @Serializable
    @SerialName("Uniform")
    data class Uniform(val min: Int, val max: Int) : ItemStackAmountProvider {
        override fun sample(random: Random): Int = random.nextInt(min, max + 1)
    }
}
