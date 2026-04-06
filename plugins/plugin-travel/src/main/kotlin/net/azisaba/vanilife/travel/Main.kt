package net.azisaba.vanilife.travel

import org.bukkit.plugin.java.JavaPlugin

internal class Main : JavaPlugin() {
    override fun onEnable() {
        setupEventListeners()
    }
}
