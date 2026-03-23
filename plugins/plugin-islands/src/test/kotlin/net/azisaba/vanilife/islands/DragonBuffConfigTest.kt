package net.azisaba.vanilife.islands

import kotlin.test.Test
import kotlin.test.assertEquals

class DragonBuffConfigTest {
    @Test
    fun `tick interval can be customized through config object`() {
        val config = Config(
            dragon = DragonConfig(
                buff = DragonBuffConfig(tickIntervalSeconds = 10L),
            ),
        )
        assertEquals(10L, config.dragon.buff.tickIntervalSeconds)
    }
}
