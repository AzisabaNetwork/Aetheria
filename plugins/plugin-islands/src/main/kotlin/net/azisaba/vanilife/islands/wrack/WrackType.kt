package net.azisaba.vanilife.islands.wrack

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.keys.SoundEventKeys
import io.papermc.paper.registry.TypedKey
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kr.toxicity.model.api.BetterModel
import kr.toxicity.model.api.data.renderer.ModelRenderer
import net.azisaba.serialization.KeySerializer
import net.azisaba.vanilife.ItemStackProvider
import net.azisaba.vanilife.islands.Island
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.entity.Player
import kotlin.random.Random

@Serializable
sealed interface WrackType {
    val modelName: String
        get() = "bottle"

    fun modelOrThrow(): ModelRenderer = BetterModel.model(modelName).orElseThrow()

    suspend fun drop(random: Random, player: Player, island: Island, entity: WrackEntity)

    @Serializable
    @SerialName("Item")
    data class Item(val item: ItemStackProvider) : WrackType {
        override suspend fun drop(random: Random, player: Player, island: Island, entity: WrackEntity) {
            val itemStack = item.sample(random)
            val dropLocation = entity.location
            dropLocation.world.dropItemNaturally(dropLocation, itemStack)

            player.playSound(Sound.sound(SoundEventKeys.ENTITY_ITEM_PICKUP, Sound.Source.PLAYER, 0.5f, 0.1f))
        }
    }

    @Serializable
    @SerialName("Enchantment")
    data class Enchantment(val id: @Serializable(with = KeySerializer::class) Key) : WrackType {
        override suspend fun drop(random: Random, player: Player, island: Island, entity: WrackEntity) {
            RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT)
                .getOrThrow(id)

            val enchantmentKey: TypedKey<org.bukkit.enchantments.Enchantment> = RegistryKey.ENCHANTMENT.typedKey(id)
            val displayLocation = entity.location.clone().add(0.0, 0.35, 0.0)
            val display = WrapperWrackEnchantmentDisplay(displayLocation)
            display.spawn(SpigotConversionUtil.fromBukkitLocation(displayLocation))

            island.audiences()
                .filterIsInstance<Player>()
                .ifEmpty { listOf(player) }
                .forEach { viewer ->
                    display.addViewer(viewer.uniqueId)
                }

            repeat(20) { tick ->
                display.tick(tick, 20)
                delay(50L)
            }
            display.finish()

            if (enchantmentKey !in island.enchantments) {
                island.addEnchantment(enchantmentKey)
            }

            player.playSound(Sound.sound(SoundEventKeys.ENTITY_ITEM_PICKUP, Sound.Source.PLAYER, 0.6f, 1.4f))
        }
    }
}
