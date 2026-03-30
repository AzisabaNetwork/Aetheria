package net.azisaba.vanilife.island

import net.azisaba.vanilife.island.enchantment.EnchantingTableBehaviour
import net.azisaba.vanilife.island.listener.EnchantingTableListener
import net.azisaba.vanilife.island.listener.IslandPlayerListener
import org.koin.core.Koin

internal fun Main.setupEventListeners(koin: Koin) {
    server.pluginManager.registerEvents(EnchantingTableListener(EnchantingTableBehaviour.Default), this)
    server.pluginManager.registerEvents(IslandPlayerListener(koin.get(), koin.get()), this)
}
