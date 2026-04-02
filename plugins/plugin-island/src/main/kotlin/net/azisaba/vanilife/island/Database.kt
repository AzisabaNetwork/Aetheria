package net.azisaba.vanilife.island

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.azisaba.vanilife.island.enchantment.IslandEnchantmentsTable
import net.azisaba.vanilife.island.storage.IslandStorageTable
import net.azisaba.vanilife.island.visitors.IslandVisitorsTable
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

internal fun Main.setupDatabase(config: DatabaseConfiguration): Database {
    val hikariConfig =
        HikariConfig().apply {
            jdbcUrl = config.url
            username = System.getenv(config.usernameEnv)
            password = System.getenv(config.passwordEnv)
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = config.maxPoolSize
        }

    val dataSource = HikariDataSource(hikariConfig)

    return Database.connect(dataSource)
}

internal fun Database.setupTables(): Database = transaction(this) {
    SchemaUtils.create(
        IslandsTable,
        IslandEnchantmentsTable,
        IslandStorageTable,
        IslandVisitorsTable,
    )
    this@setupTables
}
