package net.azisaba.vanilife.enchanting.tables

import com.github.shynixn.mccoroutine.folia.regionDispatcher
import kotlinx.coroutines.withContext
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.concurrent.ConcurrentHashMap

internal object EnchantingTableBehaviour {
    private val instanceMap: MutableMap<Block, EnchantingTableInstance> = ConcurrentHashMap()

    suspend fun tick(time: Long, plugin: Plugin) {
        val iterator = instanceMap.entries.iterator()
        while (iterator.hasNext()) {
            val (table, instance) = iterator.next()

            val tickResult = withContext(plugin.regionDispatcher(table.location)) {
                instance.tick(time)
            }

            if (!tickResult) {
                iterator.remove()
            }
        }
    }

    fun handleRightInteract(player: Player, enchantingTable: Block, heldItem: ItemStack?) {
        require(enchantingTable.type == Material.ENCHANTING_TABLE) {
            "Block must be an enchanting table"
        }

        val instance = instanceMap.getOrPut(enchantingTable) {
            EnchantingTableInstance(enchantingTable)
        }

        if (player.isSneaking) {
            instance.pickup(player)
            return
        }

        if (heldItem == null) {
            return
        }

        instance.place(player, heldItem)
    }

    fun handleLeftInteract(player: Player, enchantingTable: Block) {
        require(enchantingTable.type == Material.ENCHANTING_TABLE) {
            "Block must be an enchanting table"
        }

        instanceMap[enchantingTable]?.pickup(player)
    }
}
