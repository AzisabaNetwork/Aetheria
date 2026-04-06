package net.azisaba.vanilife.island

import net.azisaba.exposed.component
import net.kyori.adventure.text.Component
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.between
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.core.java.javaUUID
import java.util.*

object IslandsTable : LongIdTable("islands", "position") {
    val owner: Column<UUID> = javaUUID("owner").uniqueIndex()

    val level: Column<Int> = integer("level").clientDefault { Island.MIN_LEVEL }.check { it.between(Island.MIN_LEVEL, Island.MAX_LEVEL) }

    val score: Column<Double> = double("score").clientDefault { 0.0 }

    val displayName: Column<Component> = component("display_name").clientDefault { Component.text("Untitled") }

    val spawnOffsetX: Column<Double> = double("spawn_offset_x").clientDefault { 0.0 }

    val spawnOffsetY: Column<Double> = double("spawn_offset_y").clientDefault { 0.0 }

    val spawnOffsetZ: Column<Double> = double("spawn_offset_z").clientDefault { 0.0 }

    val spawnYaw: Column<Float> = float("spawn_yaw").clientDefault { 90f }

    val spawnPitch: Column<Float> = float("spawn_pitch").clientDefault { 0f }
}
