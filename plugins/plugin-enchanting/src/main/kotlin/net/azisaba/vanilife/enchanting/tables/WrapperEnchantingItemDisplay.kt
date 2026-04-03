package net.azisaba.vanilife.enchanting.tables

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.protocol.world.Location
import com.github.retrooper.packetevents.util.Quaternion4f
import com.github.retrooper.packetevents.util.Vector3f
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import me.tofaa.entitylib.container.EntityContainer
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta
import me.tofaa.entitylib.meta.display.ItemDisplayMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal class WrapperEnchantingItemDisplay(
    private val enchantingTable: Block,
    private val itemStack: ItemStack,
) : WrapperEntity(EntityTypes.ITEM_DISPLAY) {
    override fun spawn(location: Location, parent: EntityContainer): Boolean {
        if (!super.spawn(location, parent)) return false

        consumeEntityMeta(ItemDisplayMeta::class.java) { meta ->
            meta.item = SpigotConversionUtil.fromBukkitItemStack(itemStack)
            meta.displayType = ItemDisplayMeta.DisplayType.GROUND
            meta.billboardConstraints = AbstractDisplayMeta.BillboardConstraints.FIXED
            meta.interpolationDelay = 0
            meta.transformationInterpolationDuration = 0
            meta.isGlowing = true
            applyAnimation(meta, 0L, 0f)
        }
        refresh()
        return true
    }

    fun tick(time: Long, player: Player) {
        val rotation = rotationFor(player)
        consumeEntityMeta(ItemDisplayMeta::class.java) { meta ->
            meta.transformationInterpolationDuration = 2
            applyAnimation(meta, time, rotation)
        }
        refresh()

        if (time % 4L == 0L) {
            spawnEnchantParticles(rotation)
        }
    }

    private fun applyAnimation(meta: ItemDisplayMeta, time: Long, rotation: Float) {
        meta.scale = animatedScale(time)
        meta.translation = translationFor(time, rotation)
        meta.leftRotation = yawRotation(-(rotation + (Math.PI / 2.0).toFloat()))
        meta.rightRotation = tiltRotation()
        meta.glowColorOverride = enchantAuraColor(time)
    }

    private fun spawnEnchantParticles(rotation: Float) {
        val tableCenter = enchantingTable.location.toCenterLocation()
        val x = tableCenter.x + cos(rotation) * 0.18
        val z = tableCenter.z + sin(rotation) * 0.18
        tableCenter.world.spawnParticle(
            Particle.ENCHANT,
            x,
            tableCenter.y + 1.2,
            z,
            3,
            0.18,
            0.12,
            0.18,
            0.02,
        )
    }

    private fun translationFor(time: Long, rotation: Float): Vector3f {
        val offsetX = cos(rotation) * 0.18
        val offsetZ = sin(rotation) * 0.18
        return Vector3f(offsetX.toFloat(), verticalOffset(time), offsetZ.toFloat())
    }

    private fun animatedScale(time: Long): Vector3f {
        val scale = 0.85f + sin(time.toDouble() * 0.08).toFloat() * 0.04f
        return Vector3f(scale, scale, scale)
    }

    private fun verticalOffset(time: Long): Float =
        -0.27f + sin(time.toDouble() * 0.1).toFloat() * 0.03f

    private fun enchantAuraColor(time: Long): Int {
        val blend = ((sin(time.toDouble() * 0.08) + 1.0) * 0.5).toFloat()
        return lerpRgb(0x2B174F, 0x7A5BC2, blend)
    }

    private fun tiltRotation(): Quaternion4f = axisAngle(1f, 0f, 0f, 45f)

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

    private fun lerpRgb(from: Int, to: Int, progress: Float): Int {
        val clamped = progress.coerceIn(0f, 1f)
        val fromR = (from shr 16) and 0xFF
        val fromG = (from shr 8) and 0xFF
        val fromB = from and 0xFF
        val toR = (to shr 16) and 0xFF
        val toG = (to shr 8) and 0xFF
        val toB = to and 0xFF

        val r = (fromR + ((toR - fromR) * clamped)).toInt().coerceIn(0, 255)
        val g = (fromG + ((toG - fromG) * clamped)).toInt().coerceIn(0, 255)
        val b = (fromB + ((toB - fromB) * clamped)).toInt().coerceIn(0, 255)
        return (r shl 16) or (g shl 8) or b
    }

    private fun rotationFor(player: Player): Float {
        val tableCenter = enchantingTable.location.toCenterLocation()
        return atan2(player.z - tableCenter.z, player.x - tableCenter.x).toFloat()
    }
}
