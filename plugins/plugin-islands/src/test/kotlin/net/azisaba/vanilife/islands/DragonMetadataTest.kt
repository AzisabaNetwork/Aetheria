package net.azisaba.vanilife.islands

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DragonMetadataTest {
    @Test
    fun `snapshot defaults are safe`() {
        val snapshot = DragonMetadata.Snapshot()

        assertFalse(snapshot.installed)
        assertNull(snapshot.presetAir)
        assertNull(snapshot.presetWater)
        assertFalse(snapshot.legacyExists)
        assertEquals(0, snapshot.legacyBoostCredit)
        assertNull(snapshot.legacyLastPresetAir)
    }

    @Test
    fun `snapshot stores provided values`() {
        val snapshot = DragonMetadata.Snapshot(
            installed = true,
            presetAir = "sky_blue",
            presetWater = "aqua",
            legacyExists = true,
            legacyBoostCredit = 1,
            legacyLastPresetAir = "sky_blue",
        )

        assertTrue(snapshot.installed)
        assertEquals("sky_blue", snapshot.presetAir)
        assertEquals("aqua", snapshot.presetWater)
        assertTrue(snapshot.legacyExists)
        assertEquals(1, snapshot.legacyBoostCredit)
        assertEquals("sky_blue", snapshot.legacyLastPresetAir)
    }
}
