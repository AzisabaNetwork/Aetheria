package net.azisaba.vanilife

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import kotlinx.serialization.Serializable
import net.azisaba.serialization.IntProvider
import net.azisaba.serialization.KeySerializer
import net.azisaba.vanilife.item.ServerItem
import net.kyori.adventure.key.Key
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType
import kotlin.random.Random

@Serializable
data class ItemStackProvider(@Serializable(with = KeySerializer::class) val id: Key, val count: IntProvider) {
    fun sample(random: Random, registryAccess: RegistryAccess = RegistryAccess.registryAccess()): ItemStack {
        val count = count.sample(random)
        return when (val resolved = tryResolve(registryAccess)) {
            is ItemType -> resolved.createItemStack(count)
            is ServerItem -> ItemStack.of(resolved, count)
            else -> error("Cannot resolve id: $id")
        }
    }

    private fun tryResolve(registryAccess: RegistryAccess): Any? =
        tryGetAsItemType(registryAccess) ?: tryGetAsServerItem(registryAccess)

    private fun tryGetAsItemType(registryAccess: RegistryAccess): ItemType? =
        registryAccess.getRegistry(RegistryKey.ITEM).get(id)

    private fun tryGetAsServerItem(registryAccess: RegistryAccess): ServerItem? =
        registryAccess.getRegistry(RegistryKey.SERVER_ITEM).get(id)
}
