package net.azisaba.vanilife.island.waves

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.protocol.particle.Particle
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes
import com.github.retrooper.packetevents.protocol.world.Location
import com.github.retrooper.packetevents.util.Vector3f
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle
import me.tofaa.entitylib.container.EntityContainer
import me.tofaa.entitylib.meta.display.TextDisplayMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import net.azisaba.vanilife.island.IslandFonts
import net.azisaba.vanilife.world.IslandsWorld
import net.kyori.adventure.text.Component
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

internal class WrapperWave(val position: WavePosition) : WrapperEntity(EntityTypes.TEXT_DISPLAY) {
    private val random: Random = Random(position.computeSeed())
    private var cycleRandom: CycleRandom = CycleRandom.roll(random)
    private val ticksOffset: Long = random.nextLong(0L, CYCLE_TICKS)
    private var currentFrame: Char = IslandFonts.Waves.FRAME0

    override fun spawn(location: Location, parent: EntityContainer): Boolean {
        if (!super.spawn(location, parent)) return false
        consumeEntityMeta(TextDisplayMeta::class.java) { meta ->
            meta.text = Component.text(IslandFonts.Waves.FRAME0).font(IslandFonts.WAVES)
            meta.backgroundColor = 0
            meta.brightnessOverride = 0x00f000f0
            meta.leftRotation = position.coastSide.rotation
            meta.textOpacity = cycleRandom.textOpacity
            meta.transformationInterpolationDuration = 5
        }
        refresh()
        return true
    }

    override fun tick(time: Long) {
        val progress = progressAt(time)
        frameTick(progress)
        if ((time + ticksOffset) % CYCLE_TICKS == 0L) {
            startCycleTick()
        } else if (progress < cycleRandom.movementProgressEnd) {
            movementTick(time, progress)
        } else if (!entityMeta.isInvisible) {
            movementEndTick()
        }
    }

    private fun frameTick(progress: Double) {
        val frameIndex = (progress.coerceIn(0.0, 0.999999) * FRAMES.size).toInt()
        currentFrame = FRAMES[frameIndex]
    }

    private fun startCycleTick() {
        cycleRandom = CycleRandom.roll(random)
        consumeEntityMeta(TextDisplayMeta::class.java) { meta ->
            meta.text = Component.text(currentFrame).font(IslandFonts.WAVES)
            meta.translation = Vector3f()
            meta.scale = Vector3f(cycleRandom.scale, cycleRandom.scale, cycleRandom.scale)
            meta.textOpacity = cycleRandom.textOpacity
            meta.isInvisible = false
        }
        refresh()
    }

    private fun movementTick(time: Long, computedProgress: Double) {
        val target = computeLocation(computedProgress)
        val dx = target.x - x
        val dy = target.y - y
        val dz = target.z - z

        val radians = Math.toRadians(target.yaw.toDouble())
        val cos = cos(radians)
        val sin = sin(radians)
        val lx = dx * cos + dz * sin
        val lz = -dx * sin + dz * cos
        val translation = Vector3f(lx.toFloat(), dy.toFloat(), lz.toFloat())

        consumeEntityMeta(TextDisplayMeta::class.java) { meta ->
            meta.text = Component.text(currentFrame).font(IslandFonts.WAVES)
            meta.translation = translation
        }
        refresh()
    }

    private fun movementEndTick() {
        val particlePacket = WrapperPlayServerParticle(
            Particle(ParticleTypes.POOF),
            false,
            position.computeForward(computeLocation(cycleRandom.movementProgressEnd), 4.5).position,
            Vector3f(0.9f, 0f, 0.9f),
            0.01f,
            6,
            false
        )
        sendPacketsToViewers(particlePacket)

        consumeEntityMeta(TextDisplayMeta::class.java) { meta ->
            meta.text = Component.text(currentFrame).font(IslandFonts.WAVES)
            meta.isInvisible = true
        }
        refresh()
    }

    private fun progressAt(ticks: Long): Double {
        val raw = (ticks + ticksOffset).toDouble() / CYCLE_TICKS.toDouble()
        val wrapped = raw % 1.0
        return if (wrapped < 0.0) wrapped + 1.0 else wrapped
    }

    private fun computeLocation(progress: Double): Location {
        val coastSize =
            if (position.coastSide.axisX) IslandsWorld.ISLAND_SIZE_X_BLOCKS else IslandsWorld.ISLAND_SIZE_Z_BLOCKS

        val forwardEnd = (coastSize * 0.18 - 20.0).coerceIn(12.0, 30.0)
        val forwardSpin = (coastSize * 0.078).coerceIn(10.0, 28.0)
        val forwardStart = forwardEnd + forwardSpin

        val lateralInset = 37.0
        val lateralStart = if (position.coastSide.axisX) position.islandPosition.minBlockZ() else position.islandPosition.minBlockX()
        val lateralEnd = if (position.coastSide.axisX) position.islandPosition.maxBlockZ() else position.islandPosition.maxBlockX()
        val lateralStep = (lateralEnd - lateralStart) / (WavePosition.WAVES_PER_COAST_SIDE - 1).toDouble()
        val lateralMin = (lateralStart + lateralInset).coerceAtMost(lateralEnd - lateralInset)
        val lateralMax = (lateralEnd - lateralInset).coerceAtLeast(lateralMin)
        val lateralRaw = lateralStart + lateralStep * position.index + cycleRandom.lateralOffset
        val lateral = lateralRaw.coerceIn(lateralMin, lateralMax)

        val t = (progress / cycleRandom.movementProgressEnd).coerceIn(0.0, 1.0)
        val eased = 1.0 - (1.0 - t) * (1.0 - t)
        val blended = t * (1.0 - 0.25) + eased * 0.25
        val active = (blended * 0.96).coerceIn(0.0, 1.0)

        val forwardRaw = forwardStart - forwardSpin * active + cycleRandom.forwardOffset
        val forward = forwardRaw.coerceIn(forwardEnd, forwardStart)
        val fixed = position.edgeCoord() + position.coastSide.coastNormalSign * forward

        val bob = sin((progress * 0.07) + ((ticksOffset.toDouble() / CYCLE_TICKS.toDouble()) * (PI * 2.0))) * 0.12
        val x = if (position.coastSide.axisX) fixed + bob else lateral
        val z = if (position.coastSide.axisX) lateral else fixed + bob

        return Location(x, IslandsWorld.SEA_LEVEL + 0.88, z, position.coastSide.yaw, 0f)
    }

    companion object {
        const val CYCLE_TICKS: Long = 20L * 5

        private val FRAMES: List<Char> = listOf(
            IslandFonts.Waves.FRAME0,
            IslandFonts.Waves.FRAME1,
            IslandFonts.Waves.FRAME2,
            IslandFonts.Waves.FRAME3,
            IslandFonts.Waves.FRAME4,
            IslandFonts.Waves.FRAME5,
            IslandFonts.Waves.FRAME6,
            IslandFonts.Waves.FRAME7,
            IslandFonts.Waves.FRAME8,
            IslandFonts.Waves.FRAME9,
            IslandFonts.Waves.FRAME10,
        )
    }

    private data class CycleRandom(
        val scale: Float,
        val forwardOffset: Double,
        val lateralOffset: Double,
        val movementProgressEnd: Double,
        val textOpacity: Byte,
    ) {
        companion object {
            fun roll(random: Random): CycleRandom {
                val scale = 1f * random.nextDouble(0.72, 1.38).toFloat()
                val forwardOffset = random.nextDouble(-5.5, 5.5)
                val lateralOffset = random.nextDouble(-8.0, 8.0)
                val movementProgressEnd = random.nextDouble(0.66, 0.80)
                val textOpacity = random.nextInt(96, 256).toByte()
                return CycleRandom(scale, forwardOffset, lateralOffset, movementProgressEnd, textOpacity)
            }
        }
    }
}
