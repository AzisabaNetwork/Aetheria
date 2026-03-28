package net.azisaba.vanilife.island.wrack

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.protocol.world.Location
import com.github.retrooper.packetevents.util.Quaternion4f
import com.github.retrooper.packetevents.util.Vector3f
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import me.tofaa.entitylib.container.EntityContainer
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta
import me.tofaa.entitylib.meta.display.ItemDisplayMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.inventory.ItemStack
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal class WrapperWrackEnchantmentDisplay(
    private val origin: org.bukkit.Location,
) : WrapperEntity(EntityTypes.ITEM_DISPLAY) {
    override fun spawn(location: Location, parent: EntityContainer): Boolean {
        if (!super.spawn(location, parent)) return false
        consumeEntityMeta(ItemDisplayMeta::class.java) { meta ->
            meta.item = SpigotConversionUtil.fromBukkitItemStack(ItemStack(Material.ENCHANTED_BOOK))
            meta.displayType = ItemDisplayMeta.DisplayType.GROUND
            meta.billboardConstraints = AbstractDisplayMeta.BillboardConstraints.FIXED
            meta.interpolationDelay = 0
            meta.transformationInterpolationDuration = 0
            meta.scale = Vector3f(0.2f, 0.2f, 0.2f)
            meta.translation = Vector3f(0f, 0f, 0f)
            meta.rightRotation = axisAngle(1f, 0f, 0f, 20f)
            meta.isGlowing = true
            meta.glowColorOverride = 0x7A5BC2
        }
        refresh()
        return true
    }

    fun tick(animationTick: Int, totalTicks: Int) {
        val progress = (animationTick.toFloat() / totalTicks.toFloat()).coerceIn(0f, 1f)
        val eased = 1f - (1f - progress) * (1f - progress)
        val rotation = (PI.toFloat() * 3f) * progress
        val scale = 0.2f + eased * 0.95f
        val verticalOffset = eased * 0.45f

        consumeEntityMeta(ItemDisplayMeta::class.java) { meta ->
            meta.transformationInterpolationDuration = 1
            meta.scale = Vector3f(scale, scale, scale)
            meta.translation = Vector3f(0f, verticalOffset, 0f)
            meta.leftRotation = yawRotation(rotation)
        }
        refresh()

        origin.world.spawnParticle(
            Particle.ENCHANT,
            origin.x,
            origin.y + 0.2 + verticalOffset,
            origin.z,
            6,
            0.15 + eased * 0.1,
            0.18,
            0.15 + eased * 0.1,
            0.02,
        )
    }

    fun finish() {
        origin.world.spawnParticle(
            Particle.FIREWORK,
            origin.x,
            origin.y + 0.7,
            origin.z,
            24,
            0.22,
            0.25,
            0.22,
            0.02,
        )
        remove()
    }

    private fun yawRotation(radians: Float): Quaternion4f {
        val half = radians / 2f
        return Quaternion4f(0f, sin(half), 0f, cos(half))
    }

    private fun axisAngle(ax: Float, ay: Float, az: Float, degrees: Float): Quaternion4f {
        val len = sqrt((ax * ax + ay * ay + az * az).toDouble()).toFloat()
        val nx = ax / len
        val ny = ay / len
        val nz = az / len
        val half = Math.toRadians((degrees / 2f).toDouble())
        val s = sin(half).toFloat()
        val c = cos(half).toFloat()
        return Quaternion4f(nx * s, ny * s, nz * s, c)
    }
}
