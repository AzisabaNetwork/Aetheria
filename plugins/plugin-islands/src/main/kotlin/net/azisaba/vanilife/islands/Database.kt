package net.azisaba.vanilife.islands

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.azisaba.vanilife.islands.storage.DatabaseIslandRepository
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

internal fun Main.setupDatabase(databaseConfig: DatabaseConfig): Database {
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = databaseConfig.url
        username = System.getenv(databaseConfig.usernameEnv)
        password = System.getenv(databaseConfig.passwordEnv)
        driverClassName = "org.postgresql.Driver"
        maximumPoolSize = databaseConfig.maxPoolSize
    }

    val dataSource = HikariDataSource(hikariConfig)

    return Database.connect(dataSource)
}

internal fun Database.setupTables(): Database = transaction(this) {
    SchemaUtils.create(
        DatabaseIslandRepository.IslandsTable,
    )

    // create portals table if available (defensive, class may exist now)
    try {
        SchemaUtils.create(net.azisaba.vanilife.islands.portal.PortalsTable)
    } catch (_: Throwable) {
    }
    this@setupTables
}
