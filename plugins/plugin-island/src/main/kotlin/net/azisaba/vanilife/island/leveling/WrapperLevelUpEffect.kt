package net.azisaba.vanilife.island.leveling

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.protocol.world.Location
import com.github.retrooper.packetevents.util.Vector3f
import me.tofaa.entitylib.container.EntityContainer
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta
import me.tofaa.entitylib.meta.display.TextDisplayMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import net.azisaba.vanilife.island.IslandFonts
import net.azisaba.vanilife.island.IslandTranslations
import net.kyori.adventure.text.Component
import kotlin.math.PI
import kotlin.math.sin

internal class WrapperLevelUpEffect(private val level: Int) : WrapperEntity(EntityTypes.TEXT_DISPLAY) {
    override fun spawn(location: Location, parent: EntityContainer): Boolean {
        if (!super.spawn(location, parent)) return false
        consumeEntityMeta(TextDisplayMeta::class.java) { meta ->
            meta.text = Component.text()
                .append(Component.text(IslandFonts.IslandLevelIcons.levelUpOf(level)).font(IslandFonts.ISLAND_LEVEL_ICONS))
                .appendSpace()
                .append(Component.translatable(IslandTranslations.ISLAND_LEVEL_UP))
                .build()
            meta.billboardConstraints = AbstractDisplayMeta.BillboardConstraints.CENTER
            meta.backgroundColor = 0
            meta.applyAnimation(0L)
        }
        return true
    }

    override fun tick(time: Long) {
        consumeEntityMeta(TextDisplayMeta::class.java) { meta ->
            meta.transformationInterpolationDuration = 4
            meta.applyAnimation(time)
        }
        refresh()
    }

    private fun TextDisplayMeta.applyAnimation(time: Long) {
        val progress = (time % BOUNCE_PERIOD_TICKS).toFloat() / BOUNCE_PERIOD_TICKS
        val bounce = sin(progress * PI).toFloat()

        val bounceScale = BASE_SCALE + (bounce * SCALE_AMPLITUDE)
        val y = BASE_TRANSLATION_Y + (bounce * TRANSLATION_AMPLITUDE)

        scale = Vector3f(bounceScale, bounceScale, bounceScale)
        translation = Vector3f(0f, y, 0f)
    }

    private companion object {
        const val BASE_SCALE: Float = 0.6f
        const val SCALE_AMPLITUDE: Float = 0.18f
        const val BASE_TRANSLATION_Y: Float = 0.15f
        const val TRANSLATION_AMPLITUDE: Float = 0.45f
        const val BOUNCE_PERIOD_TICKS: Int = 16
    }
}
