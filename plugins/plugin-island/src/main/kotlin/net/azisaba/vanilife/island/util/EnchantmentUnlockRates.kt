package net.azisaba.vanilife.island.util

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import net.azisaba.vanilife.island.IslandsTable
import net.azisaba.vanilife.island.enchantments.IslandEnchantmentsTable
import org.bukkit.enchantments.Enchantment
import org.jetbrains.exposed.v1.core.count
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.util.concurrent.atomic.AtomicReference

object EnchantmentUnlockRates {
    private val stats: AtomicReference<Map<TypedKey<Enchantment>, Stat>?> = AtomicReference(null)

    operator fun get(enchantment: TypedKey<Enchantment>): Double {
        val stat = requireLoaded()[enchantment] ?: return 0.0
        return if (stat.total == 0L) 0.0 else stat.unlocked.toDouble() / stat.total.toDouble()
    }

    operator fun get(enchantment: Enchantment): Double = get(RegistryKey.ENCHANTMENT.typedKey(enchantment.key()))

    suspend fun bootstrap(database: Database) = suspendTransaction(database) {
        val totalIslands = IslandsTable.selectAll().count()

        val countExpression = IslandEnchantmentsTable.enchantment.count()
        val counts = IslandEnchantmentsTable.select(IslandEnchantmentsTable.enchantment, countExpression)
            .groupBy(IslandEnchantmentsTable.enchantment)
            .associate { row ->
                val enchantmentKey = RegistryKey.ENCHANTMENT.typedKey(row[IslandEnchantmentsTable.enchantment])
                enchantmentKey to row[countExpression]
            }

        val enchantments = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
        val stats = HashMap<TypedKey<Enchantment>, Stat>()

        for (enchantment in enchantments) {
            val enchantmentKey = RegistryKey.ENCHANTMENT.typedKey(enchantment.key())
            val unlocked = counts[enchantmentKey] ?: 0L
            stats[enchantmentKey] = Stat(unlocked, totalIslands)
        }

        this@EnchantmentUnlockRates.stats.set(stats)
    }

    internal fun handleEnchantmentAdd(enchantment: TypedKey<Enchantment>) {
        stats.updateAndGet { old ->
            val map = old ?: return@updateAndGet old
            val new = HashMap(map)

            val current = new[enchantment]
            if (current != null) {
                new[enchantment] = current.copy(unlocked = current.unlocked + 1)
            }

            new
        }
    }

    internal fun handleEnchantmentRemove(enchantment: TypedKey<Enchantment>) {
        stats.updateAndGet { old ->
            val map = old ?: return@updateAndGet old
            val new = HashMap(map)

            val current = new[enchantment]
            if (current != null) {
                new[enchantment] = current.copy(unlocked = current.unlocked - 1)
            }

            new
        }
    }

    internal fun handleIslandInit() {
        stats.updateAndGet { old ->
            val map = old ?: return@updateAndGet null

            map.mapValues { (_, stat) ->
                stat.copy(total = stat.total + 1)
            }
        }
    }

    private fun requireLoaded(): Map<TypedKey<Enchantment>, Stat> = stats.get()
        ?: throw IllegalStateException("Unlock rates has not loaded yet")

    private data class Stat(val unlocked: Long, val total: Long)
}