package net.azisaba.vanilife.island.wrack

import io.github.retrooper.packetevents.util.SpigotConversionUtil
import io.papermc.paper.registry.keys.SoundEventKeys
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kr.toxicity.model.api.BetterModel
import kr.toxicity.model.api.data.renderer.ModelRenderer
import net.azisaba.serialization.EnchantmentSerializer
import net.azisaba.vanilife.DynamicContents
import net.azisaba.vanilife.ItemStackProvider
import net.azisaba.vanilife.island.Island
import net.azisaba.vanilife.island.enchantment.EnchantmentAccessor
import net.azisaba.vanilife.island.leveling.IslandLevelPredicate
import net.kyori.adventure.sound.Sound
import org.bukkit.entity.Player
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@Serializable
sealed interface WrackType {
    val weight: Int

    val targetLevel: IslandLevelPredicate

    val modelName: String
        get() = "bottle"

    fun modelOrThrow(): ModelRenderer = BetterModel.model(modelName).orElseThrow()

    suspend fun drop(random: Random, player: Player, island: Island, entity: WrackEntity)

    companion object : DynamicContents<WrackType>("wrack_type", lazy { WrackType.serializer() }) {
        fun byLevel(level: Int): Set<WrackType> = filter {
            it.targetLevel.matches(level)
        }.toSet()

        fun roll(random: Random, level: Int, enchantments: EnchantmentAccessor): WrackType? {
            val entries = byLevel(level)
                .filter { it.weight > 0 }
                .filter { it !is Enchantment || !enchantments.has(it.enchantment) }
                .takeIf(List<WrackType>::isNotEmpty) ?: return null

            val totalWeight = entries.sumOf(WrackType::weight)
            var roll = random.nextInt(totalWeight)
            for (entry in entries) {
                roll -= entry.weight
                if (roll < 0) return entry
            }

            return entries.last()
        }
    }

    @Serializable
    @SerialName("Item")
    data class Item(
        val item: ItemStackProvider,
        override val weight: Int,
        override val targetLevel: IslandLevelPredicate,
    ) :
        WrackType {
        override suspend fun drop(random: Random, player: Player, island: Island, entity: WrackEntity) {
            val itemStack = item.sample(random)
            val dropLocation = entity.location
            dropLocation.world.dropItemNaturally(dropLocation, itemStack)

            player.playSound(Sound.sound(SoundEventKeys.ENTITY_ITEM_PICKUP, Sound.Source.PLAYER, 0.5f, 0.1f))
        }
    }

    @Serializable
    @SerialName("Enchantment")
    data class Enchantment(
        val enchantment: @Serializable(with = EnchantmentSerializer::class) org.bukkit.enchantments.Enchantment,
        override val weight: Int,
        override val targetLevel: IslandLevelPredicate,
    ) : WrackType {
        override suspend fun drop(random: Random, player: Player, island: Island, entity: WrackEntity) {
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
                delay(50L.milliseconds)
            }
            display.finish()

            if (!island.has(enchantment)) {
                island.addEnchantment(enchantment)
            }

            player.playSound(Sound.sound(SoundEventKeys.ENTITY_ITEM_PICKUP, Sound.Source.PLAYER, 0.6f, 1.4f))
        }
    }
}
