package net.azisaba.vanilife.island.enchantment

import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import net.azisaba.vanilife.world.IslandPosition
import org.bukkit.enchantments.Enchantment
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

interface EnchantmentAccessor {
    val enchantments: Set<TypedKey<Enchantment>>

    fun has(enchantment: TypedKey<Enchantment>): Boolean = enchantment in enchantments

    fun has(enchantment: Enchantment): Boolean = has(RegistryKey.ENCHANTMENT.typedKey(enchantment.key()))

    suspend fun addEnchantment(enchantment: TypedKey<Enchantment>): Boolean

    suspend fun addEnchantment(enchantment: Enchantment): Boolean =
        addEnchantment(RegistryKey.ENCHANTMENT.typedKey(enchantment.key()))

    suspend fun removeEnchantment(enchantment: TypedKey<Enchantment>): Boolean

    suspend fun removeEnchantment(enchantment: Enchantment): Boolean =
        removeEnchantment(RegistryKey.ENCHANTMENT.typedKey(enchantment.key()))

    suspend fun clearEnchantments()

    @ApiStatus.Internal
    suspend fun bootstrapEnchantments()

    companion object {
        fun fromDatabase(position: IslandPosition, database: Database): EnchantmentAccessor =
            EnchantmentAccessorImpl(position, database)
    }
}

private class EnchantmentAccessorImpl(position: IslandPosition, private val database: Database) : EnchantmentAccessor {
    override val enchantments: Set<TypedKey<Enchantment>>
        get() = requireLoaded().toSet()

    private val positionId: Long = position.toLong()

    private var cacheSet: MutableSet<TypedKey<Enchantment>>? = null

    override suspend fun addEnchantment(enchantment: TypedKey<Enchantment>): Boolean = suspendTransaction(database) {
        requireLoaded().add(enchantment)
        IslandEnchantmentsTable.insertIgnore {
            it[IslandEnchantmentsTable.position] = positionId
            it[IslandEnchantmentsTable.enchantment] = enchantment
        }.insertedCount > 0
    }

    override suspend fun removeEnchantment(enchantment: TypedKey<Enchantment>): Boolean = suspendTransaction(database) {
        requireLoaded().remove(enchantment)
        IslandEnchantmentsTable.deleteWhere {
            (IslandEnchantmentsTable.position eq positionId) and (IslandEnchantmentsTable.enchantment eq enchantment)
        } > 0
    }

    override suspend fun clearEnchantments() = suspendTransaction(database) {
        requireLoaded().clear()
        IslandEnchantmentsTable.deleteWhere { IslandEnchantmentsTable.position eq positionId }
        Unit
    }

    override suspend fun bootstrapEnchantments() = suspendTransaction(database) {
        val cacheSet = mutableSetOf<TypedKey<Enchantment>>()

        val rows = IslandEnchantmentsTable.select(IslandEnchantmentsTable.enchantment)
            .where { IslandEnchantmentsTable.position eq positionId }

        for (row in rows) {
            val enchantmentKey = RegistryKey.ENCHANTMENT.typedKey(row[IslandEnchantmentsTable.enchantment])
            cacheSet.add(enchantmentKey)
        }

        this@EnchantmentAccessorImpl.cacheSet = cacheSet
    }

    private fun requireLoaded(): MutableSet<TypedKey<Enchantment>> = cacheSet
        ?: throw IllegalStateException("Enchantments has not yet been loaded")
}
