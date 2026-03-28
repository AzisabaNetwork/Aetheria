package net.azisaba.vanilife.island.enchantment

import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import net.azisaba.vanilife.world.IslandPos
import org.bukkit.enchantments.Enchantment
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

interface EnchantmentAccessor {
    suspend fun enchantments(): Set<TypedKey<Enchantment>>

    suspend fun addEnchantment(enchantment: TypedKey<Enchantment>): Boolean

    suspend fun removeEnchantment(enchantment: TypedKey<Enchantment>): Boolean

    suspend fun clearEnchantments()

    companion object {
        fun fromDatabase(position: IslandPos, database: Database): EnchantmentAccessor =
            EnchantmentAccessorImpl(position, database)
    }
}

private class EnchantmentAccessorImpl(
    private val position: IslandPos, private val database: Database,
) : EnchantmentAccessor {
    override suspend fun enchantments(): Set<TypedKey<Enchantment>> = suspendTransaction(database) {
        IslandEnchantmentsTable.select(IslandEnchantmentsTable.enchantment)
            .where { IslandEnchantmentsTable.island eq position.toLong() }
            .map { RegistryKey.ENCHANTMENT.typedKey(it[IslandEnchantmentsTable.enchantment]) }
            .toSet()
    }

    override suspend fun addEnchantment(enchantment: TypedKey<Enchantment>) = suspendTransaction(database) {
        IslandEnchantmentsTable.insertIgnore {
            it[IslandEnchantmentsTable.island] = position.toLong()
            it[IslandEnchantmentsTable.enchantment] = enchantment
        }.insertedCount > 0
    }

    override suspend fun removeEnchantment(enchantment: TypedKey<Enchantment>) = suspendTransaction(database) {
        IslandEnchantmentsTable.deleteWhere {
            (IslandEnchantmentsTable.island eq position.toLong()) and
                (IslandEnchantmentsTable.enchantment eq enchantment)
        } > 0
    }

    override suspend fun clearEnchantments() = suspendTransaction(database) {
        IslandEnchantmentsTable.deleteWhere { IslandEnchantmentsTable.island eq position.toLong() }
        Unit
    }
}
