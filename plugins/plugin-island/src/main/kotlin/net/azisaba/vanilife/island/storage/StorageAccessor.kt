package net.azisaba.vanilife.island.storage

import net.azisaba.vanilife.world.IslandPosition
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Range
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

interface StorageAccessor {
    fun getItem(index: @Range(from = 0, to = 53) Int): ItemStack?

    suspend fun setItem(index: @Range(from = 0, to = 53) Int, itemStack: ItemStack)

    suspend fun clearItem(index: @Range(from = 0, to = 53) Int)

    fun applyToInventory(inventory: Inventory)

    suspend fun updateFromInventory(inventory: Inventory)

    @ApiStatus.Internal
    suspend fun bootstrapStorage()

    companion object {
        fun fromDatabase(position: IslandPosition, database: Database): StorageAccessor =
            StorageAccessorImpl(position, database)
    }
}

private class StorageAccessorImpl(position: IslandPosition, private val database: Database) : StorageAccessor {
    private val positionId: Long = position.toLong()

    private var cacheMap: MutableMap<Int, ItemStack>? = null

    override fun getItem(index: Int): ItemStack? {
        require(index in 0..53) { "Invalid slot: $index" }
        return checkLoaded()[index]?.clone()
    }

    override suspend fun setItem(index: @Range(from = 0, to = 53) Int, itemStack: ItemStack) {
        require(index in 0..53) { "Invalid slot: $index" }

        val cloned = itemStack.clone()
        checkLoaded()[index] = cloned
        suspendTransaction(database) {
            IslandStorageTable.deleteWhere {
                (IslandStorageTable.position eq positionId) and (IslandStorageTable.index eq index)
            }
            IslandStorageTable.insert {
                it[IslandStorageTable.position] = positionId
                it[IslandStorageTable.index] = index
                it[IslandStorageTable.itemStack] = cloned
            }
        }
    }

    override suspend fun clearItem(index: @Range(from = 0, to = 53) Int) {
        require(index in 0..53) { "Invalid slot: $index" }

        checkLoaded().remove(index)
        suspendTransaction(database) {
            IslandStorageTable.deleteWhere {
                (IslandStorageTable.position eq positionId) and (IslandStorageTable.index eq index)
            }
        }
    }

    override fun applyToInventory(inventory: Inventory) {
        val loaded = checkLoaded()
        for (index in 0 until inventory.size) {
            val itemStack = loaded[index]
            inventory.setItem(index, itemStack?.clone())
        }
    }

    override suspend fun updateFromInventory(inventory: Inventory) {
        val loaded = checkLoaded()

        val toInsert = mutableListOf<Pair<Int, ItemStack>>()
        val toDelete = mutableListOf<Int>()

        for (index in 0 until inventory.size) {
            val newItemStack = inventory.getItem(index)?.clone()
            val oldItemStack = loaded[index]

            when {
                newItemStack == null && oldItemStack != null -> {
                    toDelete += index
                    loaded.remove(index)
                }

                newItemStack != null && oldItemStack == null -> {
                    toInsert += index to newItemStack
                    loaded[index] = newItemStack
                }

                newItemStack != null && oldItemStack != newItemStack -> {
                    toDelete += index
                    toInsert += index to newItemStack
                    loaded[index] = newItemStack
                }
            }
        }

        suspendTransaction(database) {
            if (toDelete.isNotEmpty()) {
                IslandStorageTable.deleteWhere {
                    (IslandStorageTable.position eq positionId) and (IslandStorageTable.index inList toDelete)
                }
            }

            if (toInsert.isNotEmpty()) {
                toInsert.forEach { (index, itemStack) ->
                    IslandStorageTable.insert {
                        it[IslandStorageTable.position] = positionId
                        it[IslandStorageTable.index] = index
                        it[IslandStorageTable.itemStack] = itemStack
                    }
                }
            }
        }
    }

    override suspend fun bootstrapStorage() {
        val cacheMap = mutableMapOf<Int, ItemStack>()

        val rows = suspendTransaction(database) {
            IslandStorageTable.select(IslandStorageTable.index, IslandStorageTable.itemStack)
                .where { IslandStorageTable.position eq positionId }
                .toList()
        }

        for (row in rows) {
            cacheMap[row[IslandStorageTable.index]] = row[IslandStorageTable.itemStack].clone()
        }

        this.cacheMap = cacheMap
    }

    private fun checkLoaded(): MutableMap<Int, ItemStack> = cacheMap ?: error("Storage has not been loaded yet")
}
