package net.azisaba.vanilife.npc.wrapper

import net.kyori.adventure.text.Component
import org.bukkit.Nameable
import org.bukkit.entity.Chicken

interface NpcNameAccessor : Nameable {
    @Deprecated("Use customName() instead")
    override fun getCustomName(): String? {
        throw UnsupportedOperationException("Legacy message API not supported")
    }

    @Deprecated("Use customName(Component) instead")
    override fun setCustomName(name: String?) {
        throw UnsupportedOperationException("Legacy message API not supported")
    }
}

internal class NpcNameAccessorImpl(private val delegate: Chicken) : NpcNameAccessor {
    override fun customName(): Component? = delegate.customName()

    override fun customName(customName: Component?) {
        delegate.customName(customName)
    }
}
