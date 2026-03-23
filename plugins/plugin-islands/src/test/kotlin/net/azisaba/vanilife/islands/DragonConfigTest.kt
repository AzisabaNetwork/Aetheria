package net.azisaba.vanilife.islands

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DragonConfigTest {
    @Test
    fun `default dragon config values are present`() {
        val config = Config()

        assertTrue(config.dragon.legacy.enabled)
        assertEquals(30L, config.dragon.buff.tickIntervalSeconds)
        assertFalse(config.dragon.applyToVisitors)
        assertTrue(config.dragon.customPresets.isNotEmpty())
        assertEquals("#87CEEB", config.dragon.customPresets["sky_blue"])
    }
}
