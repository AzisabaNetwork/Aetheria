package net.azisaba.vanilife.enchanting.tables

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.protocol.world.Location
import com.github.retrooper.packetevents.util.Vector3f
import me.tofaa.entitylib.container.EntityContainer
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta
import me.tofaa.entitylib.meta.display.TextDisplayMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import net.azisaba.vanilife.enchanting.EnchantingTranslations
import net.azisaba.vanilife.island.IslandFonts
import net.kyori.adventure.text.Component

internal class WrapperEnchantingTextDisplay : WrapperEntity(EntityTypes.TEXT_DISPLAY) {
    override fun spawn(location: Location, parent: EntityContainer): Boolean {
        if (!super.spawn(location, parent)) return false

        consumeEntityMeta(TextDisplayMeta::class.java) { meta ->
            meta.text = TEXT
            meta.billboardConstraints = AbstractDisplayMeta.BillboardConstraints.CENTER
            meta.scale = Vector3f(0.75f, 0.75f, 0.75f)
            meta.translation = Vector3f(0f, 0.55f, 0f)
        }
        refresh()
        return true
    }

    private companion object {
        val TEXT: Component = Component.text()
            .append(Component.text(IslandFonts.Enchants.ENCHANTING_TABLE).font(IslandFonts.ENCHANTS))
            .appendSpace()
            .append(Component.translatable(EnchantingTranslations.BLOCK_ENCHANTING_TABLE_RIGHT_CLICK_TO_OPEN))
            .build()
    }
}
