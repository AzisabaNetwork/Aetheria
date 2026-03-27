package net.azisaba.vanilife.npc.ai

import net.azisaba.vanilife.npc.NpcMode
import net.azisaba.vanilife.npc.wrapper.NpcWrapper
import org.bukkit.Location
import org.bukkit.block.Block
import java.util.concurrent.ThreadLocalRandom

internal fun NpcWrapper.isFreeMode(): Boolean = mode == NpcMode.FREE

internal fun nextCooldownTicks(minTicks: Int, maxTicks: Int): Int =
    ThreadLocalRandom.current().nextInt(minTicks, maxTicks + 1)

internal fun tickCooldown(ticksRemaining: Int): Int = (ticksRemaining - 1).coerceAtLeast(0)

internal fun Block.toCenterLocation(): Location = location.add(0.5, 0.0, 0.5)

internal fun canStandAt(floorBlock: Block, bodyBlock: Block, headBlock: Block): Boolean {
    if (!floorBlock.isSolid || floorBlock.isLiquid) return false
    if (!bodyBlock.isPassable || !headBlock.isPassable) return false
    if (bodyBlock.isLiquid || headBlock.isLiquid) return false
    return true
}
