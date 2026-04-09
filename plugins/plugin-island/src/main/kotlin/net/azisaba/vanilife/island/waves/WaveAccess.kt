package net.azisaba.vanilife.island.waves

import java.util.*

interface WaveAccess {
    fun addWaveViewer(uuid: UUID)

    fun removeWaveViewer(uuid: UUID)

    fun waveTick(time: Long)
}
