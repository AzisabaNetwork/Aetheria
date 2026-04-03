package net.azisaba.vanilife.enchanting.tables

import io.github.retrooper.packetevents.util.SpigotConversionUtil
import io.papermc.paper.registry.keys.SoundEventKeys
import net.kyori.adventure.sound.Sound
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

internal class EnchantingTableInstance(private val enchantingTable: Block) {
    private var itemStack: ItemStack? = null

    private var itemDisplay: WrapperEnchantingItemDisplay? = null
    private var textDisplay: WrapperEnchantingTextDisplay? = null

    fun hasItem(): Boolean = itemStack != null

    fun place(player: Player, sourceItemStack: ItemStack): Boolean {
        val placed = sourceItemStack.clone().apply { amount = 1 }
        if (!player.gameMode.isInvulnerable) {
            sourceItemStack.subtract()
        }

        itemStack?.let(::dropItemStack)
        itemDisplay?.remove()

        val spawnedItemDisplay = spawnItemDisplay(placed)
        val spawnedTextDisplay = ensureTextDisplay()

        forEachViewerId(spawnedItemDisplay::addViewer)

        itemStack = placed
        itemDisplay = spawnedItemDisplay
        textDisplay = spawnedTextDisplay

        playPlacementEffects()
        return true
    }

    fun pickup(player: Player): Boolean {
        val itemStack = this@EnchantingTableInstance.itemStack ?: return false
        dropItemStack(itemStack)
        resetState()
        player.playSound(Sound.sound(SoundEventKeys.ENTITY_ITEM_PICKUP, Sound.Source.PLAYER, 0.5f, 0.1f))
        return true
    }

    fun tick(time: Long): Boolean {
        if (enchantingTable.type != Material.ENCHANTING_TABLE) {
            itemStack?.let(::dropItemStack)
            resetState()
            return false
        }

        val item = itemStack
        if (item != null) {
            val nearest = findNearestPlayer()
            if (nearest == null) {
                dropItemStack(item)
                resetState()
                return false
            }

            itemDisplay?.tick(time, nearest)
        }

        return hasItem() || textDisplay != null
    }

    private inline fun forEachViewerId(action: (UUID) -> Unit) {
        enchantingTable.chunk.playersSeeingChunk
            .asSequence()
            .map(Player::getUniqueId)
            .forEach(action)
    }

    private fun spawnItemDisplay(itemStack: ItemStack): WrapperEnchantingItemDisplay =
        WrapperEnchantingItemDisplay(enchantingTable, itemStack).apply {
            spawn(
                SpigotConversionUtil.fromBukkitLocation(
                    enchantingTable.location.toCenterLocation().add(0.0, 0.95, 0.0)
                )
            )
        }

    private fun ensureTextDisplay(): WrapperEnchantingTextDisplay =
        textDisplay ?: WrapperEnchantingTextDisplay().also { display ->
            display.spawn(SpigotConversionUtil.fromBukkitLocation(enchantingTable.location.toCenterLocation()))
            forEachViewerId(display::addViewer)
            textDisplay = display
        }

    private fun resetState() {
        itemStack = null

        itemDisplay?.remove()
        itemDisplay = null

        textDisplay?.remove()
        textDisplay = null
    }

    private fun dropItemStack(itemStack: ItemStack) {
        val dropLocation = enchantingTable.location.toCenterLocation().add(0.0, 1.0, 0.0)
        dropLocation.world.dropItemNaturally(dropLocation, itemStack.clone())
    }

    private fun findNearestPlayer(): Player? {
        val centerLocation = enchantingTable.location.toCenterLocation()
        return centerLocation.world.getNearbyPlayers(centerLocation, 3.0)
            .minByOrNull { player -> player.location.distanceSquared(centerLocation) }
    }

    private fun playPlacementEffects() {
        enchantingTable.world.spawnParticle(
            Particle.CLOUD,
            enchantingTable.location.add(0.5, 0.5, 0.5),
            6,
            0.12,
            0.08,
            0.12,
            0.01,
        )
        enchantingTable.world.playSound(
            Sound.sound(SoundEventKeys.ENTITY_ITEM_FRAME_ADD_ITEM, Sound.Source.BLOCK, 0.7f, 0.85f),
            enchantingTable.x + 0.5,
            enchantingTable.y + 0.5,
            enchantingTable.z + 0.5,
        )
    }
}
