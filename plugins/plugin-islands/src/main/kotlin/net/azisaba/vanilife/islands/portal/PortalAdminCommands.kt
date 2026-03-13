package net.azisaba.vanilife.islands.portal

import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PortalAdminCommands : CommandExecutor, KoinComponent {
    private val manager: PortalManager by inject()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(Component.text("Usage: /portaladmin <reindex>") )
            return true
        }
        when (args[0].lowercase()) {
            "reindex" -> {
                if (!sender.hasPermission("vanilife.portal.admin.reindex") && !sender.isOp) {
                    sender.sendMessage(Component.text("You don't have permission to run this command."))
                    return true
                }
                manager.rebuildOriginIndex()
                sender.sendMessage(Component.text("Portal origin index rebuilt."))
                return true
            }
            else -> {
                sender.sendMessage(Component.text("Unknown subcommand: ${args[0]}"))
                return true
            }
        }
    }
}
