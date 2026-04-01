package net.azisaba.vanilife.island

import net.azisaba.exposed.component
import net.kyori.adventure.text.Component
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.between
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.core.java.javaUUID
import java.util.*

internal object IslandsTable : LongIdTable("islands", "position") {
    val owner: Column<UUID> = javaUUID("owner").uniqueIndex()

    val level: Column<Int> = integer("level").check { it.between(Island.MIN_LEVEL, Island.MAX_LEVEL) }

    val score: Column<Double> = double("score")

    val displayName: Column<Component?> = component("display_name").nullable()

    val spawnOffsetX: Column<Double> = double("spawn_offset_x")

    val spawnOffsetY: Column<Double> = double("spawn_offset_y")

    val spawnOffsetZ: Column<Double> = double("spawn_offset_z")

    val spawnYaw: Column<Float> = float("spawn_yaw")

    val spawnPitch: Column<Float> = float("spawn_pitch")
}
