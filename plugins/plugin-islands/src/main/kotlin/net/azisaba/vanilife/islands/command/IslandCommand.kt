package net.azisaba.vanilife.islands.command

import com.github.shynixn.mccoroutine.folia.launch
import kotlinx.coroutines.runBlocking
import net.azisaba.vanilife.islands.Config
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
            repository.updateDragonInstalled(pos, true)
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
            repository.updateDragonInstalled(pos, false)
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
                sender.sendMessage("Preset air: ${island.dragonData.presetAir}")
                sender.sendMessage("Preset water: ${island.dragonData.presetWater}")
                sender.sendMessage("Legacy exists: ${island.dragonData.legacyExists}")
            }
        }
    }

    private fun resolveTargetIslandPos(sender: CommandSender, args: Array<out String>, commandIndex: Int): IslandPos? {
        val islandIdArg = args.getOrNull(commandIndex)
        if (islandIdArg != null && islandIdArg != "air" && islandIdArg != "water") {
            return parseIslandPos(islandIdArg)
        }
        val player = sender as? Player ?: return null
        return runBlocking { manager.lookupByOwner(player.uniqueId)?.pos }
    }

    private fun handleSetColor(sender: CommandSender, args: Array<out String>) {
        if (!requirePermission(sender, "vanilife.dragon.use")) return
        if (args.size < 4) {
            sender.sendMessage("Usage: /island dragon setcolor [islandId] <air|water> <presetKey>")
            return
        }

        val hasIslandId = args.size >= 5
        val islandPos = if (hasIslandId) resolveTargetIslandPos(sender, args, 2) else resolveTargetIslandPos(sender, args, 99)
        if (islandPos == null) {
            sender.sendMessage("Could not resolve target island")
            return
        }

        val field = if (hasIslandId) args[3] else args[2]
        val preset = if (hasIslandId) args[4] else args[3]
        if (!config.dragon.customPresets.containsKey(preset)) {
            sender.sendMessage("Unknown preset '$preset'. Available: ${config.dragon.customPresets.keys.joinToString(", ")}")
            return
        }

        plugin.launch {
            when (field) {
                "air" -> repository.updateDragonPresetAir(islandPos, preset)
                "water" -> repository.updateDragonPresetWater(islandPos, preset)
                else -> {
                    sender.sendMessage("Field must be 'air' or 'water'")
                    return@launch
                }
            }
            sender.sendMessage("Set $field preset to $preset")
        }
    }

    private fun handleRedeemLegacy(sender: CommandSender, args: Array<out String>) {
        if (!requirePermission(sender, "vanilife.dragon.redeem")) return
        val islandPosValue = (if (args.size >= 3) parseIslandPos(args[2]) else null)
            ?: ((sender as? Player)?.let { player -> runBlocking { manager.lookupByOwner(player.uniqueId)?.pos } })
        val islandPos = islandPosValue ?: run {
            sender.sendMessage("Usage: /island dragon redeem-legacy [islandId]")
            return
        }

        plugin.launch {
            val summary = repository.lookupByPos(islandPos)
            if (summary == null) {
                sender.sendMessage("Island not found")
                return@launch
            }
            val legacy = summary.dragonData
            if (!legacy.legacyExists) {
                sender.sendMessage("No legacy ticket present")
                return@launch
            }
            val newBoost = (legacy.legacyBoostCredit - 1).coerceAtLeast(0)
            repository.updateDragonLegacy(islandPos, false, newBoost, null)
            sender.sendMessage("Redeemed legacy")
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>,
    ): MutableList<String> = mutableListOf()
}
