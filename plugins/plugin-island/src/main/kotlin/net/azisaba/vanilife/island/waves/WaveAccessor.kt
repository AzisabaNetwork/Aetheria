package net.azisaba.vanilife.island.waves

import me.tofaa.entitylib.container.EntityContainer
import net.azisaba.vanilife.world.IslandPosition
import java.util.*

interface WaveAccessor {
    fun addWaveViewer(uuid: UUID)

    fun removeWaveViewer(uuid: UUID)

    fun waveTick(time: Long)

    companion object {
        fun create(position: IslandPosition): WaveAccessor = WaveAccessorImpl(position)
    }
}

private class WaveAccessorImpl(position: IslandPosition) : WaveAccessor {
    private val wrapperEntityContainer: EntityContainer = EntityContainer.basic()

    private val wavesByPosition: MutableMap<WavePosition, WrapperWave> = HashMap(WavePosition.WAVES_PER_ISLAND)

    init {
        for (wavePos in WavePosition.posSet(position)) {
            val wrapperWave = WrapperWave(wavePos)
            wrapperWave.spawn(wavePos.location(), wrapperEntityContainer)
            wavesByPosition[wavePos] = wrapperWave
        }
    }

    override fun addWaveViewer(uuid: UUID) {
        wavesByPosition.values.forEach { it.addViewer(uuid) }
    }

    override fun removeWaveViewer(uuid: UUID) {
        wavesByPosition.values.forEach { it.removeViewer(uuid) }
    }

    override fun waveTick(time: Long) {
        wavesByPosition.values.forEach { it.tick(time) }
    }
}
