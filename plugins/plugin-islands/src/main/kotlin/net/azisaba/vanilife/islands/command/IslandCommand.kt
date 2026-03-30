package net.azisaba.vanilife.islands.command

import com.github.shynixn.mccoroutine.folia.launch
import net.azisaba.vanilife.event.DragonCustomizationChangedEvent
import net.azisaba.vanilife.event.DragonInstalledEvent
import net.azisaba.vanilife.event.DragonRemovedEvent
import net.azisaba.vanilife.islands.Config
import net.azisaba.vanilife.islands.DragonMetadata
import net.azisaba.vanilife.islands.Island
import net.azisaba.vanilife.islands.IslandManager
import net.azisaba.vanilife.islands.repository.IslandRepository
import net.azisaba.vanilife.world.IslandPos
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.koin.core.context.GlobalContext
import java.time.Instant
import java.util.logging.Level

internal class IslandCommand(private val plugin: Plugin) : CommandExecutor, TabCompleter {
    private val config: Config
        get() = GlobalContext.get().get()

    private val repository: IslandRepository
        get() = GlobalContext.get().get()

    private val manager: IslandManager
        get() = GlobalContext.get().get()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty() || args[0] != "dragon") return false
        if (args.size < 2) {
            sender.sendMessage("Usage: /island dragon <grant|revoke|status|setcolor|redeem-legacy> ...")
            return true
        }

        when (args[1]) {
            "grant" -> handleGrant(sender, args)
            "revoke" -> handleRevoke(sender, args)
            "status" -> handleStatus(sender, args)
            "setcolor" -> handleSetColor(sender, args)
            "redeem-legacy" -> handleRedeemLegacy(sender, args)
            else -> sender.sendMessage("Unknown subcommand: ${args[1]}")
        }
        return true
    }

    private fun parseIslandPos(arg: String): IslandPos? = try {
        IslandPos.fromLong(arg.toLong())
    } catch (_: Exception) {
        null
    }

    private fun requirePermission(sender: CommandSender, permission: String): Boolean {
        if (sender.hasPermission(permission)) return true
        sender.sendMessage("You do not have permission: $permission")
        return false
    }

    private fun handleGrant(sender: CommandSender, args: Array<out String>) {
        if (!requirePermission(sender, "vanilife.dragon.admin.grant")) return
        if (args.size < 3) {
            sender.sendMessage("Usage: /island dragon grant <islandId>")
            return
        }
        val pos = parseIslandPos(args[2]) ?: run {
            sender.sendMessage("Invalid islandId")
            return
        }
        plugin.launch {
            val island = manager.lookupByPos(pos)
            if (island == null) {
                sender.sendMessage("Island not found")
                return@launch
            }
            if (island.dragonData.installed) {
                sender.sendMessage("Dragon is already installed on this island")
                return@launch
            }
            val dragonData = island.dragonData
            if (dragonData is DragonMetadata.Writable) {
                dragonData.setInstalled(true)
            } else {
                repository.updateDragonInstalled(pos, true)
            }

            val islandId = island.pos.toLong().toString()
            plugin.server.pluginManager.callEvent(
                DragonInstalledEvent(islandId, island.ownerUuid, Instant.now())
            )

            plugin.logger.log(Level.INFO, "[Dragon] Granted dragon to island $islandId by ${sender.name}")
            sender.sendMessage("Dragon granted to island ${args[2]}")
        }
    }

    private fun handleRevoke(sender: CommandSender, args: Array<out String>) {
        if (!requirePermission(sender, "vanilife.dragon.admin.revoke")) return
        if (args.size < 3) {
            sender.sendMessage("Usage: /island dragon revoke <islandId>")
            return
        }
        val pos = parseIslandPos(args[2]) ?: run {
            sender.sendMessage("Invalid islandId")
            return
        }
        plugin.launch {
            val island = manager.lookupByPos(pos)
            if (island == null) {
                sender.sendMessage("Island not found")
                return@launch
            }
            if (!island.dragonData.installed) {
                sender.sendMessage("Dragon is not installed on this island")
                return@launch
            }
            val dragonData = island.dragonData
            if (dragonData is DragonMetadata.Writable) {
                dragonData.setInstalled(false)
            } else {
                repository.updateDragonInstalled(pos, false)
            }

            val islandId = island.pos.toLong().toString()
            plugin.server.pluginManager.callEvent(
                DragonRemovedEvent(islandId, "admin_revoke", Instant.now())
            )

            plugin.logger.log(Level.INFO, "[Dragon] Revoked dragon from island $islandId by ${sender.name}")
            sender.sendMessage("Dragon revoked for island ${args[2]}")
        }
    }

    private fun handleStatus(sender: CommandSender, args: Array<out String>) {
        if (!requirePermission(sender, "vanilife.dragon.admin.status")) return
        if (args.size < 3) {
            sender.sendMessage("Usage: /island dragon status <islandId>")
            return
        }
        val pos = parseIslandPos(args[2]) ?: run {
            sender.sendMessage("Invalid islandId")
            return
        }
        plugin.launch {
            val island = manager.lookupByPos(pos)
            if (island == null) {
                sender.sendMessage("Island not found")
            } else {
                sender.sendMessage("Dragon installed: ${island.dragonData.installed}")
                sender.sendMessage("Preset air: ${island.dragonData.presetAir ?: "none"}")
                sender.sendMessage("Preset water: ${island.dragonData.presetWater ?: "none"}")
                sender.sendMessage("Legacy exists: ${island.dragonData.legacyExists}")
                sender.sendMessage("Legacy boost credit: ${island.dragonData.legacyBoostCredit}")
                sender.sendMessage("Legacy last preset air: ${island.dragonData.legacyLastPresetAir ?: "none"}")
            }
        }
    }

    private fun handleSetColor(sender: CommandSender, args: Array<out String>) {
        if (!requirePermission(sender, "vanilife.dragon.use")) return
        if (args.size < 4) {
            sender.sendMessage("Usage: /island dragon setcolor [islandId] <air|water> <presetKey>")
            return
        }

        val hasIslandId = args.size >= 5
        val field = if (hasIslandId) args[3] else args[2]
        val preset = if (hasIslandId) args[4] else args[3]

        if (field != "air" && field != "water") {
            sender.sendMessage("Field must be 'air' or 'water'")
            return
        }
        if (!config.dragon.customPresets.containsKey(preset)) {
            sender.sendMessage("Unknown preset '$preset'. Available: ${config.dragon.customPresets.keys.joinToString(", ")}")
            return
        }

        plugin.launch {
            val island: Island? = if (hasIslandId) {
                val pos = parseIslandPos(args[2])
                if (pos == null) {
                    sender.sendMessage("Invalid islandId")
                    return@launch
                }
                manager.lookupByPos(pos)
            } else {
                val player = sender as? Player
                if (player == null) {
                    sender.sendMessage("Only players can use this without specifying islandId")
                    return@launch
                }
                manager.lookupByOwner(player.uniqueId)
            }

            if (island == null) {
                sender.sendMessage("Island not found")
                return@launch
            }

            if (!island.dragonData.installed) {
                sender.sendMessage("Dragon is not installed on this island")
                return@launch
            }

            val dragonData = island.dragonData
            when (field) {
                "air" -> {
                    if (dragonData is DragonMetadata.Writable) {
                        dragonData.setPresetAir(preset)
                    } else {
                        repository.updateDragonPresetAir(island.pos, preset)
                    }
                }
                "water" -> {
                    if (dragonData is DragonMetadata.Writable) {
                        dragonData.setPresetWater(preset)
                    } else {
                        repository.updateDragonPresetWater(island.pos, preset)
                    }
                }
            }

            val islandId = island.pos.toLong().toString()
            val changedBy = (sender as? Player)?.uniqueId ?: island.ownerUuid
            plugin.server.pluginManager.callEvent(
                DragonCustomizationChangedEvent(islandId, changedBy, field, preset)
            )

            plugin.logger.log(Level.INFO, "[Dragon] Customization changed on island $islandId: $field=$preset by ${sender.name}")
            sender.sendMessage("Set $field preset to $preset")
        }
    }

    private fun handleRedeemLegacy(sender: CommandSender, args: Array<out String>) {
        if (!requirePermission(sender, "vanilife.dragon.redeem")) return

        plugin.launch {
            val island: Island? = if (args.size >= 3) {
                val pos = parseIslandPos(args[2])
                if (pos == null) {
                    sender.sendMessage("Invalid islandId")
                    return@launch
                }
                manager.lookupByPos(pos)
            } else {
                val player = sender as? Player
                if (player == null) {
                    sender.sendMessage("Usage: /island dragon redeem-legacy [islandId]")
                    return@launch
                }
                manager.lookupByOwner(player.uniqueId)
            }

            if (island == null) {
                sender.sendMessage("Island not found")
                return@launch
            }

            val dragonData = island.dragonData
            if (!dragonData.legacyExists) {
                sender.sendMessage("No legacy ticket present")
                return@launch
            }

            val newBoost = (dragonData.legacyBoostCredit - 1).coerceAtLeast(0)
            if (dragonData is DragonMetadata.Writable) {
                dragonData.setLegacyTicket(false, newBoost, null)
            } else {
                repository.updateDragonLegacy(island.pos, false, newBoost, null)
            }

            val islandId = island.pos.toLong().toString()
            plugin.logger.log(Level.INFO, "[Dragon] Legacy redeemed on island $islandId by ${sender.name}")
            sender.sendMessage("Redeemed legacy ticket. ${sender.name} received the dragon legacy token.")
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>,
    ): MutableList<String> {
        if (args.isEmpty()) return mutableListOf()

        if (args.size == 1) {
            return mutableListOf("dragon").filter { it.startsWith(args[0], ignoreCase = true) }.toMutableList()
        }

        if (args[0] != "dragon") return mutableListOf()

        if (args.size == 2) {
            val subcommands = mutableListOf<String>()
            if (sender.hasPermission("vanilife.dragon.admin.grant")) subcommands.add("grant")
            if (sender.hasPermission("vanilife.dragon.admin.revoke")) subcommands.add("revoke")
            if (sender.hasPermission("vanilife.dragon.admin.status")) subcommands.add("status")
            if (sender.hasPermission("vanilife.dragon.use")) subcommands.add("setcolor")
            if (sender.hasPermission("vanilife.dragon.redeem")) subcommands.add("redeem-legacy")
            return subcommands.filter { it.startsWith(args[1], ignoreCase = true) }.toMutableList()
        }

        return when (args[1]) {
            "setcolor" -> tabCompleteSetColor(sender, args)
            else -> mutableListOf()
        }
    }

    private fun tabCompleteSetColor(sender: CommandSender, args: Array<out String>): MutableList<String> {
        val hasIslandId = args.size >= 6 || (args.size >= 4 && args[2] != "air" && args[2] != "water")
        val fieldArgIndex = if (hasIslandId) 3 else 2
        val presetArgIndex = if (hasIslandId) 4 else 3

        return when (args.size - 1) {
            fieldArgIndex -> {
                listOf("air", "water").filter { it.startsWith(args.last(), ignoreCase = true) }.toMutableList()
            }
            presetArgIndex -> {
                config.dragon.customPresets.keys.filter { it.startsWith(args.last(), ignoreCase = true) }.toMutableList()
            }
            else -> mutableListOf()
        }
    }
}
