package net.azisaba.vanilife.island.waves

import me.tofaa.entitylib.container.EntityContainer
import net.azisaba.vanilife.world.IslandPosition
import java.util.HashMap
import java.util.UUID

internal class IslandWaveAccess(position: IslandPosition) : WaveAccess {
    private val wrapperEntityContainer: EntityContainer = EntityContainer.basic()

    private val wavesByPosition: MutableMap<WavePosition, WrapperWave> = HashMap(WavePosition.WAVES_PER_ISLAND)

    init {
        for (wavePos in WavePosition.posSet(position)) {
            val wrapperWave = WrapperWave(wavePos)
            wrapperWave.spawn(wavePos.toLocation(), wrapperEntityContainer)
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
