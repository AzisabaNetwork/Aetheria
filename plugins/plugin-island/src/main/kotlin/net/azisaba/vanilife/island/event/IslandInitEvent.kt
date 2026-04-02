package net.azisaba.vanilife.island.event

import net.azisaba.vanilife.island.Island
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

data class IslandInitEvent(val island: Island) : Event() {
    override fun getHandlers(): HandlerList = HANDLER_LIST

    companion object {
        private val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLER_LIST
    }
}
