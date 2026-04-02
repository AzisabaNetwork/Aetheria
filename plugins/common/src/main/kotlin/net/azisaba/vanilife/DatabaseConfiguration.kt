package net.azisaba.vanilife

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.jdbc.Database

@Serializable
data class DatabaseConfiguration(
    val url: String = "jdbc:postgresql://localhost:5432/vanilife",
    val usernameEnv: String = "DATABASE_USERNAME",
    val passwordEnv: String = "DATABASE_PASSWORD",
    val maxPoolSize: Int = 12,
) {
    fun createConnection(): Database {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = url
            username = System.getenv(usernameEnv)
            password = System.getenv(passwordEnv)
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = maxPoolSize
        }

        val dataSource = HikariDataSource(hikariConfig)

        return Database.connect(dataSource)
    }
}
