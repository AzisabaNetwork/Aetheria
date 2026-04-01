package net.azisaba.vanilife.menuprovider

import kotlinx.serialization.Serializable

@Serializable
internal data class Configuration(
    val discordUrl: String = "https://discord.com/invite/azisaba/",
)
