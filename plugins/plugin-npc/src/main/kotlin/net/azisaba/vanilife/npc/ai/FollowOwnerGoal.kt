package net.azisaba.vanilife.npc.ai

import com.destroystokyo.paper.entity.ai.Goal
import com.destroystokyo.paper.entity.ai.GoalKey
import com.destroystokyo.paper.entity.ai.GoalType
import io.papermc.paper.entity.TeleportFlag
import net.azisaba.vanilife.npc.NpcMode
import net.azisaba.vanilife.npc.wrapper.NpcWrapper
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Chicken
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.*
import java.util.concurrent.ThreadLocalRandom

internal class FollowOwnerGoal(private val npcWrapper: NpcWrapper) : Goal<Chicken> {
    private var ticksUntilRepath: Int = 0

    override fun getKey(): GoalKey<Chicken> = NpcGoalKeys.FOLLOW_OWNER

    override fun getTypes(): EnumSet<GoalType> = EnumSet.of(GoalType.MOVE, GoalType.LOOK, GoalType.JUMP)

    override fun shouldActivate(): Boolean {
        val owner = resolveOwner() ?: return false
        if (!canFollowOwner(owner)) return false
        if (npcWrapper.location.distanceSquared(owner.location) < START_DISTANCE_SQUARED) return false
        return true
    }

    override fun shouldStayActive(): Boolean {
        val owner = resolveOwner() ?: return false
        return canFollowOwner(owner) && npcWrapper.location.distanceSquared(owner.location) > STOP_DISTANCE_SQUARED
    }

    override fun start() {
        ticksUntilRepath = 0
    }

    override fun stop() {
        npcWrapper.delegate.pathfinder.stopPathfinding()
    }

    override fun tick() {
        val owner = resolveOwner() ?: return
        if (!canFollowOwner(owner)) {
            stop()
            return
        }

        val ownerLocation = owner.location
        val distanceSquared = npcWrapper.location.distanceSquared(ownerLocation)
        if (distanceSquared >= TELEPORT_DISTANCE_SQUARED) {
            tryTeleportNear(owner)
            return
        }

        npcWrapper.delegate.lookAt(owner)
        if (--ticksUntilRepath <= 0) {
            ticksUntilRepath = REPATH_INTERVAL_TICKS
            val pathfinder = npcWrapper.delegate.pathfinder
            if (!pathfinder.moveTo(owner, FOLLOW_SPEED)) {
                pathfinder.moveTo(owner.location, FOLLOW_SPEED)
            }
        }
    }

    private fun resolveOwner(): Player? = npcWrapper.owner?.let(Bukkit::getPlayer)

    private fun canFollowOwner(owner: Player): Boolean {
        if (npcWrapper.mode != NpcMode.FOLLOW) return false
        if (!npcWrapper.hasOwner) return false
        if (!owner.isOnline || owner.isDead || !owner.isValid) return false
        if (owner.gameMode == GameMode.SPECTATOR) return false
        if (owner.world != npcWrapper.delegate.world) return false
        return true
    }

    private fun tryTeleportNear(owner: Player) {
        val destination = findTeleportDestination(owner) ?: return
        npcWrapper.delegate.pathfinder.stopPathfinding()
        npcWrapper.delegate.teleportAsync(
            destination,
            PlayerTeleportEvent.TeleportCause.PLUGIN,
            TeleportFlag.Relative.VELOCITY_X,
            TeleportFlag.Relative.VELOCITY_Y,
            TeleportFlag.Relative.VELOCITY_Z,
        )
    }

    private fun findTeleportDestination(owner: Player): Location? {
        val ownerBlock = owner.location.block
        repeat(MAX_TELEPORT_ATTEMPTS) {
            val offsetX = ThreadLocalRandom.current().nextInt(-3, 4)
            val offsetZ = ThreadLocalRandom.current().nextInt(-3, 4)
            if (kotlin.math.abs(offsetX) < 2 && kotlin.math.abs(offsetZ) < 2) return@repeat

            val floorBlock = ownerBlock.getRelative(offsetX, ThreadLocalRandom.current().nextInt(-1, 2), offsetZ)
            val bodyBlock = floorBlock.getRelative(BlockFace.UP)
            val headBlock = bodyBlock.getRelative(BlockFace.UP)

            if (!isSafeTeleportSpace(floorBlock.type, bodyBlock.type, headBlock.type)) return@repeat
            if (!bodyBlock.isPassable || !headBlock.isPassable) return@repeat
            if (!floorBlock.isSolid || floorBlock.isLiquid || bodyBlock.isLiquid || headBlock.isLiquid) return@repeat

            return bodyBlock.location.add(0.5, 0.0, 0.5).apply {
                yaw = owner.location.yaw
                pitch = owner.location.pitch
            }
        }

        return null
    }

    private fun isSafeTeleportSpace(floor: Material, body: Material, head: Material): Boolean {
        if (floor in DANGEROUS_FLOOR_MATERIALS) return false
        if (body in DANGEROUS_BODY_MATERIALS || head in DANGEROUS_BODY_MATERIALS) return false
        return true
    }

    private companion object {
        const val FOLLOW_SPEED: Double = 1.25
        const val START_DISTANCE_SQUARED: Double = 9.0
        const val STOP_DISTANCE_SQUARED: Double = 4.0
        const val TELEPORT_DISTANCE_SQUARED: Double = 144.0
        const val REPATH_INTERVAL_TICKS: Int = 5
        const val MAX_TELEPORT_ATTEMPTS: Int = 10

        val DANGEROUS_BODY_MATERIALS: Set<Material> = setOf(
            Material.LAVA,
            Material.FIRE,
            Material.SOUL_FIRE,
            Material.CACTUS,
            Material.SWEET_BERRY_BUSH,
        )

        val DANGEROUS_FLOOR_MATERIALS: Set<Material> = setOf(
            Material.LAVA,
            Material.MAGMA_BLOCK,
            Material.CACTUS,
            Material.CAMPFIRE,
            Material.SOUL_CAMPFIRE,
            Material.FIRE,
            Material.SOUL_FIRE,
        )
    }
}
