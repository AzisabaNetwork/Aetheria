package net.azisaba.vanilife.islands.enchantment

import io.github.retrooper.packetevents.util.SpigotConversionUtil
import io.papermc.paper.registry.keys.SoundEventKeys
import net.kyori.adventure.sound.Sound
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

internal open class EnchantingTableBehaviour {
    private val instanceMap: MutableMap<Block, Instance> = ConcurrentHashMap()

    fun use(player: Player, enchantingTable: Block,  itemStack: ItemStack) {
        require(enchantingTable.type == Material.ENCHANTING_TABLE) { "Block must be an enchanting table" }

        if (enchantingTable in instanceMap) return

        val placedItemStack = itemStack.clone().apply { amount = 1 }
        if (!player.gameMode.isInvulnerable) {
            itemStack.subtract()
        }

        val wrapperItemDisplay = WrapperEnchantingTableItemDisplay(enchantingTable, placedItemStack).apply {
            spawn(
                SpigotConversionUtil.fromBukkitLocation(
                    enchantingTable.location.toCenterLocation().add(0.0, 0.95, 0.0)
                )
            )
        }
        val wrapperTextDisplay = WrapperEnchantingTableTextDisplay().apply {
            spawn(
                SpigotConversionUtil.fromBukkitLocation(enchantingTable.location.toCenterLocation())
            )
        }
        enchantingTable.chunk.playersSeeingChunk.map(Player::getUniqueId).forEach { viewer ->
            wrapperItemDisplay.addViewer(viewer)
            wrapperTextDisplay.addViewer(viewer)
        }

        instanceMap[enchantingTable] = Instance(placedItemStack, wrapperItemDisplay, wrapperTextDisplay)

        playPlacementEffects(enchantingTable)
    }

    fun pickup(player: Player, enchantingTable: Block) {
        require(enchantingTable.type == Material.ENCHANTING_TABLE) { "Block must be an enchanting table" }

        val instance = instanceMap.remove(enchantingTable) ?: return
        instance.dropItemStack(enchantingTable)
        instance.dispose()

        player.playSound(Sound.sound(SoundEventKeys.ENTITY_ITEM_PICKUP, Sound.Source.PLAYER, 0.5f, 0.1f))
    }

    fun tick(time: Long) {
        val iterator = instanceMap.entries.iterator()
        while (iterator.hasNext()) {
            val (enchantingTable, instance) = iterator.next()
            val nearestPlayer = findNearestPlayer(enchantingTable)
            if (nearestPlayer == null) {
                instance.dropItemStack(enchantingTable)
                instance.dispose()
                iterator.remove()
                continue
            }
            instance.tick(time, nearestPlayer)
        }
    }

    fun hasItem(enchantingTable: Block): Boolean = enchantingTable in instanceMap

    private fun playPlacementEffects(enchantingTable: Block) {
        enchantingTable.world.spawnParticle(
            Particle.CLOUD,
            enchantingTable.location.add(0.5, 0.5, 0.5),
            6, 0.12, 0.08, 0.12, 0.01,
        )
        enchantingTable.world.playSound(
            Sound.sound(SoundEventKeys.ENTITY_ITEM_FRAME_ADD_ITEM, Sound.Source.BLOCK, 0.7f, 0.85f),
            enchantingTable.x + 0.5, enchantingTable.y + 0.5, enchantingTable.z + 0.5,
        )
    }

    private fun findNearestPlayer(enchantingTable: Block): Player? {
        val tableCenter = enchantingTable.location.toCenterLocation()
        return tableCenter.world.getNearbyPlayers(tableCenter, 3.0)
            .minByOrNull { player -> player.location.distanceSquared(tableCenter) }
    }

    companion object Default : EnchantingTableBehaviour()

    private data class Instance(
        val itemStack: ItemStack,
        val wrapperItemDisplay: WrapperEnchantingTableItemDisplay,
        val wrapperTextDisplay: WrapperEnchantingTableTextDisplay,
    ) {
        fun tick(time: Long, nearestPlayer: Player) {
            wrapperItemDisplay.tick(time, nearestPlayer)
        }

        fun dispose() {
            wrapperItemDisplay.remove()
            wrapperTextDisplay.remove()
        }

        fun dropItemStack(enchantingTable: Block) {
            val dropLocation = enchantingTable.location.toCenterLocation().add(0.0, 1.0, 0.0)
            dropLocation.world.dropItemNaturally(dropLocation, itemStack.clone())
        }
    }
}
