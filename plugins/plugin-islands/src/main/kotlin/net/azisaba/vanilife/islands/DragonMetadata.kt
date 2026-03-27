package net.azisaba.vanilife.islands

import kotlinx.serialization.Serializable
import java.util.*

/**
 * Dragon-related metadata stored per-island.
 */
sealed interface DragonMetadata {
    val installed: Boolean
    val presetAir: String?
    val presetWater: String?
    val legacyExists: Boolean
    val legacyBoostCredit: Int
    val legacyLastPresetAir: String?

    @Serializable
    data class Snapshot(
        override val installed: Boolean = false,
        override val presetAir: String? = null,
        override val presetWater: String? = null,
        override val legacyExists: Boolean = false,
        override val legacyBoostCredit: Int = 0,
        override val legacyLastPresetAir: String? = null,
    ) : DragonMetadata

    class Writable internal constructor(
        installed: Boolean,
        presetAir: String?,
        presetWater: String?,
        legacyExists: Boolean,
        legacyBoostCredit: Int,
        legacyLastPresetAir: String?,
        private val islandPos: net.azisaba.vanilife.world.IslandPos,
        private val repository: net.azisaba.vanilife.islands.repository.IslandRepository
    ) : DragonMetadata {
        override var installed: Boolean = installed
            private set
        override var presetAir: String? = presetAir
            private set
        override var presetWater: String? = presetWater
            private set
        override var legacyExists: Boolean = legacyExists
            private set
        override var legacyBoostCredit: Int = legacyBoostCredit
            private set
        override var legacyLastPresetAir: String? = legacyLastPresetAir
            private set

        suspend fun setInstalled(value: Boolean) {
            repository.updateDragonInstalled(islandPos, value)
            installed = value
        }

        suspend fun setPresetAir(value: String?) {
            repository.updateDragonPresetAir(islandPos, value)
            presetAir = value
        }

        suspend fun setPresetWater(value: String?) {
            repository.updateDragonPresetWater(islandPos, value)
            presetWater = value
        }

        suspend fun setLegacyTicket(exists: Boolean, boost: Int = 0, lastPresetAir: String? = null) {
            repository.updateDragonLegacy(islandPos, exists, boost, lastPresetAir)
            legacyExists = exists
            legacyBoostCredit = boost
            legacyLastPresetAir = lastPresetAir
        }
    }
}
