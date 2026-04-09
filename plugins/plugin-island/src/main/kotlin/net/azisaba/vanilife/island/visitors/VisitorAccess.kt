package net.azisaba.vanilife.island.visitors

import org.bukkit.OfflinePlayer
import java.util.*
import kotlin.time.Duration
import kotlin.time.Instant

interface VisitorAccess {
    val visitors: Set<UUID>

    val totalStayTime: Duration

    suspend fun beginVisit(uuid: UUID)

    suspend fun beginVisit(player: OfflinePlayer) = beginVisit(player.uniqueId)

    suspend fun endVisit(uuid: UUID)

    suspend fun endVisit(player: OfflinePlayer) = endVisit(player.uniqueId)

    suspend fun firstVisitAt(uuid: UUID): Instant?

    suspend fun firstVisitAt(player: OfflinePlayer): Instant? = firstVisitAt(player.uniqueId)

    suspend fun lastVisitAt(uuid: UUID): Instant?

    suspend fun lastVisitAt(player: OfflinePlayer): Instant? = lastVisitAt(player.uniqueId)

    suspend fun stayTimeOf(uuid: UUID): Duration

    suspend fun stayTimeOf(player: OfflinePlayer): Duration = stayTimeOf(player.uniqueId)

    fun hasVisited(uuid: UUID): Boolean = uuid in visitors

    fun hasVisited(player: OfflinePlayer): Boolean = hasVisited(player.uniqueId)
}
