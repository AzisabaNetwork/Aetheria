package net.azisaba.vanilife.island.event

import io.papermc.paper.registry.TypedKey
import net.azisaba.vanilife.island.Island
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

data class IslandEnchantmentRemoveEvent(
    val island: Island,
    val enchantment: TypedKey<Enchantment>,
    private var cancelled: Boolean = false,
) : Event(), Cancellable {
    override fun getHandlers(): HandlerList = HANDLER_LIST

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    companion object {
        private val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLER_LIST
    }
}
