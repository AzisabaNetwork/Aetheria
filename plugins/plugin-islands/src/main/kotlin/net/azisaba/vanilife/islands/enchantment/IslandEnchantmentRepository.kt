package net.azisaba.vanilife.islands.enchantment

import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import net.azisaba.exposed.key
import net.azisaba.vanilife.world.IslandPos
import net.kyori.adventure.key.Key
import org.bukkit.enchantments.Enchantment
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

interface IslandEnchantmentRepository {
    suspend fun lookupByPos(islandPos: IslandPos): Set<TypedKey<Enchantment>>

    suspend fun addEnchantment(where: IslandPos, enchantment: TypedKey<Enchantment>)

    suspend fun removeEnchantment(where: IslandPos, enchantment: TypedKey<Enchantment>)

    suspend fun clearEnchantments(where: IslandPos)
}

internal class DatabaseIslandEnchantmentRepository(private val database: Database) : IslandEnchantmentRepository {
    override suspend fun lookupByPos(islandPos: IslandPos): Set<TypedKey<Enchantment>> = suspendTransaction(database) {
        Table
            .selectAll()
            .where { Table.pos eq islandPos.toLong() }
            .map { it.toEnchantmentKey() }
            .toSet()
    }

    override suspend fun addEnchantment(where: IslandPos, enchantment: TypedKey<Enchantment>) = suspendTransaction(database) {
        Table.insertIgnore {
            it[pos] = where.toLong()
            it[Table.enchantment] = enchantment.key()
        }
        Unit
    }

    override suspend fun removeEnchantment(where: IslandPos, enchantment: TypedKey<Enchantment>) = suspendTransaction(database) {
        Table.deleteWhere {
            (pos eq where.toLong()) and (Table.enchantment eq enchantment.key())
        }
        Unit
    }

    override suspend fun clearEnchantments(where: IslandPos) = suspendTransaction(database) {
        Table.deleteWhere { pos eq where.toLong() }
        Unit
    }

    private fun ResultRow.toEnchantmentKey(): TypedKey<Enchantment> {
        return RegistryKey.ENCHANTMENT.typedKey(get(Table.enchantment))
    }

    object Table : org.jetbrains.exposed.v1.core.Table("island_enchantments") {
        val pos: Column<Long> = long("pos")
        val enchantment: Column<Key> = key("enchantment")

        init {
            uniqueIndex(pos, enchantment)
        }
    }
}
