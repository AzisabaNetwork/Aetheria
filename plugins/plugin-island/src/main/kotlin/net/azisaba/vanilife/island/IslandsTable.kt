package net.azisaba.vanilife.island

import net.azisaba.exposed.component
import net.kyori.adventure.text.Component
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import java.util.*

internal object IslandsTable : LongIdTable("islands", "position") {
    val owner: Column<UUID> = javaUUID("owner").uniqueIndex()

    val level: Column<Int> = integer("level")

    val displayName: Column<Component?> = component("display_name").nullable()
}
