package net.azisaba.vanilife.island.enchantments

import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import net.azisaba.vanilife.island.util.EnchantmentUnlockRates
import net.azisaba.vanilife.world.IslandPosition
import org.bukkit.enchantments.Enchantment
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

internal class IslandEnchantmentHolder(
    position: IslandPosition, private val database: Database,
) : EnchantmentHolder {
    override val enchantments: Set<TypedKey<Enchantment>>
        get() = requireLoaded().toSet()

    private val positionId: Long = position.toLong()

    private var cacheSet: MutableSet<TypedKey<Enchantment>>? = null

    override suspend fun addEnchantment(enchantment: TypedKey<Enchantment>): Boolean = suspendTransaction(database) {
        requireLoaded().add(enchantment)
        EnchantmentUnlockRates.handleEnchantmentAdd(enchantment)
        IslandEnchantmentsTable.insertIgnore {
            it[IslandEnchantmentsTable.position] = positionId
            it[IslandEnchantmentsTable.enchantment] = enchantment
        }.insertedCount > 0
    }

    override suspend fun removeEnchantment(enchantment: TypedKey<Enchantment>): Boolean = suspendTransaction(database) {
        requireLoaded().remove(enchantment)
        EnchantmentUnlockRates.handleEnchantmentRemove(enchantment)
        IslandEnchantmentsTable.deleteWhere {
            (IslandEnchantmentsTable.position eq positionId) and (IslandEnchantmentsTable.enchantment eq enchantment)
        } > 0
    }

    override suspend fun clearEnchantments() = suspendTransaction(database) {
        requireLoaded().clear()
        IslandEnchantmentsTable.deleteWhere { IslandEnchantmentsTable.position eq positionId }
        Unit
    }

    suspend fun bootstrap() = suspendTransaction(database) {
        val cacheSet = mutableSetOf<TypedKey<Enchantment>>()

        val rows = IslandEnchantmentsTable.select(IslandEnchantmentsTable.enchantment)
            .where { IslandEnchantmentsTable.position eq positionId }

        for (row in rows) {
            val enchantmentKey = RegistryKey.ENCHANTMENT.typedKey(row[IslandEnchantmentsTable.enchantment])
            cacheSet.add(enchantmentKey)
        }

        this@IslandEnchantmentHolder.cacheSet = cacheSet
    }

    private fun requireLoaded(): MutableSet<TypedKey<Enchantment>> =
        cacheSet ?: throw IllegalStateException("Enchantments has not yet been loaded")
}
