package net.azisaba.vanilife.island

import net.azisaba.exposed.component
import net.azisaba.exposed.key
import net.kyori.adventure.text.Component
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object IslandsTable : LongIdTable(name = "islands", columnName = "position") {
    val type: Column<IslandType<*>> = key("type").transform(
        { IslandType.byKeyOrThrow(it) },
        { it.key() },
    )

    val displayName: Column<Component> = component("display_name").clientDefault { Component.text("Untitled") }

    val description: Column<Component> = component("description").clientDefault { Component.empty() }

    val spawnOffsetX: Column<Double> = double("spawn_offset_x").clientDefault { 0.0 }

    val spawnOffsetY: Column<Double> = double("spawn_offset_y").clientDefault { 0.0 }

    val spawnOffsetZ: Column<Double> = double("spawn_offset_z").clientDefault { 0.0 }

    val spawnYaw: Column<Float> = float("spawn_yaw").clientDefault { 90f }

    val spawnPitch: Column<Float> = float("spawn_pitch").clientDefault { 0f }
}
