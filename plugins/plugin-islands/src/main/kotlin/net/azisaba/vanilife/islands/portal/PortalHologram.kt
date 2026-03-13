package net.azisaba.vanilife.islands.portal

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.protocol.world.Location
import me.tofaa.entitylib.container.EntityContainer
import me.tofaa.entitylib.meta.display.TextDisplayMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import net.kyori.adventure.text.Component

internal class PortalHologram(private val text: Component) : WrapperEntity(EntityTypes.TEXT_DISPLAY) {
    override fun spawn(location: Location, parent: EntityContainer): Boolean {
        if (!super.spawn(location, parent)) return false

        consumeEntityMeta(TextDisplayMeta::class.java) { meta ->
            meta.text = text
            meta.backgroundColor = 0
            meta.isInvisible = false
            meta.isSeeThrough = true
            meta.transformationInterpolationDuration = 5
        }
        refresh()
        return true
    }
}
