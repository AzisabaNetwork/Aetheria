package net.azisaba.vanilife.npc.commands

import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.regionDispatcher
import com.mojang.brigadier.Command
import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver
import io.papermc.paper.math.Position
import net.azisaba.vanilife.npc.NpcFonts
import net.azisaba.vanilife.npc.NpcTranslations
import net.azisaba.vanilife.npc.NpcType
import net.azisaba.vanilife.npc.spawn
import net.azisaba.vanilife.npc.trading.NpcOffersLoader
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.ShadowColor
import org.bukkit.Location
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.CompletableFuture

internal object NpcCommand : KoinComponent {
    private val plugin: Plugin by inject()

    private val offersLoader: NpcOffersLoader by inject()

    private val INVALID_NPC_TYPE: SimpleCommandExceptionType =
        SimpleCommandExceptionType(LiteralMessage("Invalid NPC type"))

    fun create(): LiteralCommandNode<CommandSourceStack> = Commands.literal("npc")
        .requires(Commands.restricted { it.sender.hasPermission("vanilife.command.npc") })
        .then(
            Commands.literal("reload")
                .then(
                    Commands.argument("npcType", ArgumentTypes.key())
                        .suggests(::suggestNpcTypes)
                        .executes(::reloadOne)
                )
                .executes(::reloadAll)
        )
        .then(
            Commands.literal("summon")
                .then(
                    Commands.argument("npcType", ArgumentTypes.key())
                        .suggests(::suggestNpcTypes)
                        .executes(::summon)
                        .then(
                            Commands.argument("pos", ArgumentTypes.finePosition())
                                .executes(::summonWithPosition)
                        )
                )
        )
        .build()

    private fun reloadAll(context: CommandContext<CommandSourceStack>): Int {
        offersLoader.loadAll()
        context.source.sender.sendMessage(Component.translatable(NpcTranslations.COMMANDS_VANILIFE_NPC_RELOAD_ALL))
        return Command.SINGLE_SUCCESS
    }

    private fun reloadOne(context: CommandContext<CommandSourceStack>): Int {
        val npcType = ensureNpcType(context)
        offersLoader.reloadOne(npcType)
        context.source.sender.sendMessage(
            Component.translatable(
                NpcTranslations.COMMANDS_VANILIFE_NPC_RELOAD_ONE,
                Component.text()
                    .append(Component.text(npcType.icon).font(NpcFonts.NPC_ICONS).shadowColor(ShadowColor.none()))
                    .append(Component.text(npcType.key.asString(), NamedTextColor.GRAY))
                    .build(),
            )
        )
        return Command.SINGLE_SUCCESS
    }

    private fun summon(context: CommandContext<CommandSourceStack>): Int {
        val npcType = ensureNpcType(context)
        return summon(context, npcType, context.source.location)
    }

    private fun summonWithPosition(context: CommandContext<CommandSourceStack>): Int {
        val npcType = ensureNpcType(context)
        val positionResolver = context.getArgument("pos", FinePositionResolver::class.java)
        val position = positionResolver.resolve(context.source)
        return summon(context, npcType, position)
    }

    private fun summon(context: CommandContext<CommandSourceStack>, npcType: NpcType, position: Position): Int {
        val world = context.source.location.world
        val location = Location(world, position.x(), position.y(), position.z())
        plugin.launch(plugin.regionDispatcher(location)) {
            world.spawn(location, npcType)
            context.source.sender.sendMessage(
                Component.translatable(
                    NpcTranslations.COMMANDS_VANILIFE_NPC_SUMMON_SUCCESS,
                    Component.text()
                        .append(Component.text(npcType.icon).font(NpcFonts.NPC_ICONS).shadowColor(ShadowColor.none()))
                        .append(Component.text(npcType.key.asString(), NamedTextColor.GRAY))
                        .build(),
                )
            )
        }

        return Command.SINGLE_SUCCESS
    }

    private fun ensureNpcType(context: CommandContext<CommandSourceStack>): NpcType {
        val npcTypeKey = context.getArgument("npcType", Key::class.java)
        val npcType = npcTypeKey?.let(NpcType::byKey)
        return npcType ?: throw INVALID_NPC_TYPE.create()
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
