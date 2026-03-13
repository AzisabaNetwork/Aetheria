package net.azisaba.vanilife.islands.portal

import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

/**
 * Standalone `/portal` command handling: list, unlink, giveigniter
 */
class PortalCommands : CommandExecutor, KoinComponent {
    private val manager: PortalManager by inject()
    private val repository: PortalRepository by inject()
    private val plugin: org.bukkit.plugin.Plugin by inject()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(Component.text("Usage: /portal <list|unlink|giveigniter>"))
            return true
        }

        when (args[0].lowercase()) {
            "list" -> return handleList(sender)
            "unlink" -> return handleUnlink(sender, args.drop(1).toTypedArray())
            "giveigniter" -> return handleGiveIgniter(sender, args.drop(1).toTypedArray())
            else -> {
                sender.sendMessage(Component.text("Unknown subcommand: ${args[0]}"))
                return true
            }
        }
    }

    private fun handleList(sender: CommandSender): Boolean {
        if (!sender.hasPermission("vanilife.portal.list") && !sender.isOp) {
            sender.sendMessage(Component.text("You don't have permission to list portals."))
            return true
        }

        val list = manager.getAllPortals()
        sender.sendMessage(Component.text("Active portals: ${list.size}"))
        list.forEach { p ->
            sender.sendMessage(Component.text("id=${p.id} owner=${p.ownerUuid} origin=${p.originWorldName}:${p.originMin}..${p.originMax} resource=${p.resourceWorldName} hologram=${p.hologramUuid}"))
        }
        return true
    }

    private fun handleUnlink(sender: CommandSender, args: Array<String>): Boolean {
        // permission: vanilife.portal.unlink OR owner may unlink their own portal
        if (args.isEmpty()) {
            // try to unlink by player owner if sender is player
            if (sender is Player) {
                val player = sender
                val owned = manager.getAllPortals().find { it.ownerUuid == player.uniqueId }
                if (owned == null) {
                    player.sendMessage(Component.text("No portal found for your island."))
                    return true
                }

                if (player.uniqueId == owned.ownerUuid || player.hasPermission("vanilife.portal.unlink") || player.isOp) {
                    manager.removePortal(owned)
                    player.sendMessage(Component.text("Portal unlinked."))
                } else {
                    player.sendMessage(Component.text("You are not allowed to unlink this portal."))
                }
                return true
            } else {
                sender.sendMessage(Component.text("Usage: /portal unlink <id> or run as player to unlink your portal"))
                return true
            }
        }

        // unlink by id (admin)
        if (!sender.hasPermission("vanilife.portal.unlink") && !sender.isOp) {
            sender.sendMessage(Component.text("You don't have permission to unlink a portal by id."))
            return true
        }

        val id = args[0].toLongOrNull()
        if (id == null) {
            sender.sendMessage(Component.text("Invalid id: ${args[0]}"))
            return true
        }

        val portal = manager.getAllPortals().find { it.id == id }
        if (portal == null) {
            sender.sendMessage(Component.text("Portal id $id not found."))
            return true
        }

        manager.removePortal(portal)
        sender.sendMessage(Component.text("Portal $id unlinked."))
        return true
    }

    private fun handleGiveIgniter(sender: CommandSender, args: Array<String>): Boolean {
        if (!sender.hasPermission("vanilife.portal.giveigniter") && !sender.isOp) {
            sender.sendMessage(Component.text("You don't have permission to give igniters."))
            return true
        }

        val targetPlayer: Player? = when {
            args.isNotEmpty() -> org.bukkit.Bukkit.getPlayer(args[0])
            sender is Player -> sender
            else -> null
        }

        if (targetPlayer == null) {
            sender.sendMessage(Component.text("Player not found or must be run by a player when no target is provided."))
            return true
        }

        val item = ItemStack(org.bukkit.Material.FLINT_AND_STEEL)
        val meta = item.itemMeta
        // use plain string display name (ItemMeta#setDisplayName expects String)
        meta?.setDisplayName("Portal Igniter")
        val key = NamespacedKey(plugin, "portal_igniter")
        meta?.persistentDataContainer?.set(key, PersistentDataType.BYTE, 1.toByte())
        item.itemMeta = meta

        targetPlayer.inventory.addItem(item)
        targetPlayer.sendMessage(Component.text("Given Portal Igniter."))
        if (sender != targetPlayer) sender.sendMessage(Component.text("Igniter given to ${targetPlayer.name}"))
        return true
    }
}
