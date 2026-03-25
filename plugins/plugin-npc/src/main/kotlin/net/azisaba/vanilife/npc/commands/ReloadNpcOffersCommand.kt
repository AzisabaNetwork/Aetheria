package net.azisaba.vanilife.npc.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import net.azisaba.vanilife.npc.NpcTranslations
import net.azisaba.vanilife.npc.NpcType
import net.azisaba.vanilife.npc.trading.NpcOffersLoader
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.CompletableFuture

internal object ReloadNpcOffersCommand : KoinComponent {
    private val loader: NpcOffersLoader by inject()

    fun create(): LiteralCommandNode<CommandSourceStack> = Commands.literal("reload-npc-offers")
        .requires(Commands.restricted { it.sender.hasPermission("minecraft.command.reload") })
        .executes(::reloadAll)
        .then(
            Commands.argument("npcType", ArgumentTypes.key())
                .suggests(::suggestNpcTypes)
                .executes(::reloadOne)
        )
        .build()

    private fun reloadAll(context: CommandContext<CommandSourceStack>): Int {
        loader.loadAll()
        context.source.sender.sendMessage(Component.translatable(NpcTranslations.COMMANDS_VANILIFE_RELOAD_NPC_OFFERS_ALL))
        return Command.SINGLE_SUCCESS
    }

    private fun reloadOne(context: CommandContext<CommandSourceStack>): Int {
        val npcType = NpcType.byKey(context.getArgument("npcType", Key::class.java))
        if (npcType == null) {
            context.source.sender.sendMessage(
                Component.translatable(
                    NpcTranslations.COMMANDS_VANILIFE_RELOAD_NPC_OFFERS_UNKNOWN_NPC_TYPE,
                    NamedTextColor.RED,
                )
            )
            return 0
        }
        loader.reloadOne(npcType)
        context.source.sender.sendMessage(
            Component.translatable(
                NpcTranslations.COMMANDS_VANILIFE_RELOAD_NPC_OFFERS_ONE,
                Component.text(npcType.key.asString()),
            )
        )
        return Command.SINGLE_SUCCESS
    }

    private fun suggestNpcTypes(
        context: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        val remaining = builder.remainingLowerCase
        NpcType.entries
            .map { it.key.asString() }
            .filter { it.startsWith(remaining) }
            .sorted()
            .forEach(builder::suggest)
        return builder.buildFuture()
    }
}
