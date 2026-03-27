package net.azisaba.vanilife.islands.command

import kotlinx.coroutines.runBlocking
import net.azisaba.vanilife.islands.Config
import net.azisaba.vanilife.islands.IslandManager
import net.azisaba.vanilife.islands.repository.IslandRepository
import net.azisaba.vanilife.world.IslandPos
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.koin.core.context.GlobalContext

class IslandCommand : CommandExecutor, TabCompleter {
    private val config: Config
        get() = GlobalContext.get().get()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) return false
        if (args[0] != "dragon") return false
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

    private fun parseIslandPos(arg: String): IslandPos? {
        return try {
            val value = arg.toLong()
            IslandPos.fromLong(value)
        } catch (e: Exception) {
            null
        }
    }

    private fun handleGrant(sender: CommandSender, args: Array<out String>) {
        if (args.size < 3) {
            sender.sendMessage("Usage: /island dragon grant <islandId>")
            return
        }
        val pos = parseIslandPos(args[2]) ?: run { sender.sendMessage("Invalid islandId"); return }
        val repo = GlobalContext.get().get<IslandRepository>()
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(GlobalContext.get().get(), Runnable {
            runBlocking { repo.updateDragonInstalled(pos, true) }
            sender.sendMessage("Dragon granted to island ${args[2]}")
        })
    }

    private fun handleRevoke(sender: CommandSender, args: Array<out String>) {
        if (args.size < 3) {
            sender.sendMessage("Usage: /island dragon revoke <islandId>")
            return
        }
        val pos = parseIslandPos(args[2]) ?: run { sender.sendMessage("Invalid islandId"); return }
        val repo = GlobalContext.get().get<IslandRepository>()
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(GlobalContext.get().get(), Runnable {
            runBlocking { repo.updateDragonInstalled(pos, false) }
            sender.sendMessage("Dragon revoked for island ${args[2]}")
        })
    }

    private fun handleStatus(sender: CommandSender, args: Array<out String>) {
        if (args.size < 3) {
            sender.sendMessage("Usage: /island dragon status <islandId>")
            return
        }
        val pos = parseIslandPos(args[2]) ?: run { sender.sendMessage("Invalid islandId"); return }
        val manager = GlobalContext.get().get<IslandManager>()
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(GlobalContext.get().get(), Runnable {
            val island = runBlocking { manager.lookupByPos(pos) }
            if (island == null) {
                sender.sendMessage("Island not found")
            } else {
                sender.sendMessage("Dragon installed: ${island.dragonData.installed}")
                sender.sendMessage("Preset air: ${island.dragonData.presetAir}")
                sender.sendMessage("Preset water: ${island.dragonData.presetWater}")
                sender.sendMessage("Legacy exists: ${island.dragonData.legacyExists}")
            }
        })
    }

    private fun handleSetColor(sender: CommandSender, args: Array<out String>) {
        if (args.size < 5) {
            sender.sendMessage("Usage: /island dragon setcolor <islandId> <air|water> <presetKey>")
            return
        }
        val pos = parseIslandPos(args[2]) ?: run { sender.sendMessage("Invalid islandId"); return }
        val field = args[3]
        val preset = args[4]
        if (!config.dragon.customPresets.containsKey(preset)) {
            sender.sendMessage("Unknown preset '$preset'. Available: ${config.dragon.customPresets.keys.joinToString(", ")}")
            return
        }
        val repo = GlobalContext.get().get<IslandRepository>()
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(GlobalContext.get().get(), Runnable {
            when (field) {
                "air" -> runBlocking { repo.updateDragonPresetAir(pos, preset) }
                "water" -> runBlocking { repo.updateDragonPresetWater(pos, preset) }
                else -> sender.sendMessage("Field must be 'air' or 'water'")
            }
            sender.sendMessage("Set $field preset to $preset for island ${args[2]}")
        })
    }

    private fun handleRedeemLegacy(sender: CommandSender, args: Array<out String>) {
        if (args.size < 3) {
            sender.sendMessage("Usage: /island dragon redeem-legacy <islandId>")
            return
        }
        val pos = parseIslandPos(args[2]) ?: run { sender.sendMessage("Invalid islandId"); return }
        val repo = GlobalContext.get().get<IslandRepository>()
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(GlobalContext.get().get(), Runnable {
            runBlocking {
                val summary = repo.lookupByPos(pos)
                if (summary == null) {
                    sender.sendMessage("Island not found")
                    return@runBlocking
                }
                val current = summary
                val legacy = current.dragonData
                if (!legacy.legacyExists) {
                    sender.sendMessage("No legacy ticket present")
                    return@runBlocking
                }
                val newBoost = (legacy.legacyBoostCredit - 1).coerceAtLeast(0)
                repo.updateDragonLegacy(pos, false, newBoost, null)
                sender.sendMessage("Redeemed legacy for island ${args[2]}")
            }
        })
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        return mutableListOf()
    }
}
