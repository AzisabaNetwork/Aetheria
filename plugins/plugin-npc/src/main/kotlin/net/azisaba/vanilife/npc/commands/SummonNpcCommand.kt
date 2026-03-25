package net.azisaba.vanilife.npc.commands

import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.regionDispatcher
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
import net.azisaba.vanilife.npc.spawn
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.CompletableFuture
import kotlin.random.Random

internal object SummonNpcCommand : KoinComponent {
    private val plugin: Plugin by inject()

    fun create(): LiteralCommandNode<CommandSourceStack> = Commands.literal("summon-npc")
        .then(
            Commands.argument("npcType", ArgumentTypes.key())
                .suggests(::suggestNpcTypes)
                .executes(::summon)
        )
        .executes(::summon)
        .build()

    private fun summon(context: CommandContext<CommandSourceStack>): Int {
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

        val spawnLocation = context.source.location
        plugin.launch(plugin.regionDispatcher(spawnLocation)) {
            spawnLocation.world.spawn(spawnLocation, npcType)
        }

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
