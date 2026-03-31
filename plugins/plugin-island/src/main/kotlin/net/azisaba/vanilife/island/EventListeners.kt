package net.azisaba.vanilife.island

import net.azisaba.vanilife.island.enchantment.EnchantingTableBehaviour
import net.azisaba.vanilife.island.listener.EnchantingTableListener
import net.azisaba.vanilife.island.listener.FlightListener
import net.azisaba.vanilife.island.listener.PlayerListener
import net.azisaba.vanilife.island.listener.ScoreSourceListener
import net.azisaba.vanilife.island.listener.SpawnLocationListener
import org.koin.core.Koin

internal fun Main.setupEventListeners(koin: Koin) {
    server.pluginManager.registerEvents(EnchantingTableListener(EnchantingTableBehaviour.Default), this)
    server.pluginManager.registerEvents(FlightListener(koin.get()), this)
    server.pluginManager.registerEvents(PlayerListener(koin.get(), koin.get()), this)
    server.pluginManager.registerEvents(ScoreSourceListener(koin.get(), koin.get()), this)
    server.pluginManager.registerEvents(SpawnLocationListener(koin.get()), koin.get())
}
