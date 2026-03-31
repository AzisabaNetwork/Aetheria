package net.azisaba.vanilife.island.leveling.score

import io.papermc.paper.registry.set.RegistryValueSet
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.azisaba.serialization.BlockTypeSetSerializer
import net.azisaba.serialization.EntityTypeSetSerializer
import net.azisaba.vanilife.DynamicContents
import org.bukkit.block.BlockState
import org.bukkit.block.BlockType
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType

@Serializable
sealed interface ScoreSource {
    val rule: ScoringRule

    fun score(context: ScoringContext, nowMillis: Long): ScoringRule.Result = rule.calculate(context, nowMillis)

    companion object : DynamicContents<ScoreSource>("score_source", lazy { ScoreSource.serializer() })

    @Serializable
    @SerialName("BreakBlock")
    data class BreakBlock(
        val blocks: @Serializable(with = BlockTypeSetSerializer::class) RegistryValueSet<BlockType>,
        override val rule: ScoringRule,
    ) : ScoreSource {
        fun containsBlock(blockState: BlockState): Boolean = blocks.contains(blockState.type.asBlockType()!!)
    }

    @Serializable
    @SerialName("PlaceBlock")
    data class PlaceBlock(
        val blocks: @Serializable(with = BlockTypeSetSerializer::class) RegistryValueSet<BlockType>,
        override val rule: ScoringRule,
    ) : ScoreSource {
        fun containsBlock(blockState: BlockState): Boolean = blocks.contains(blockState.type.asBlockType()!!)
    }

    @Serializable
    @SerialName("Breed")
    data class Breed(
        val entities: @Serializable(with = EntityTypeSetSerializer::class) RegistryValueSet<EntityType>,
        override val rule: ScoringRule,
    ) : ScoreSource {
        fun containsEntity(entity: Entity): Boolean = entities.contains(entity.type)
    }

    @Serializable
    @SerialName("Harvest")
    data class Harvest(
        val crops: @Serializable(with = BlockTypeSetSerializer::class) RegistryValueSet<BlockType>,
        override val rule: ScoringRule,
    ) : ScoreSource {
        fun containsCrop(blockState: BlockState): Boolean = crops.contains(blockState.type.asBlockType()!!)
    }

    @Serializable
    @SerialName("VisitPlayer")
    data class VisitPlayer(
        val firstVisitOnly: Boolean = false,
        override val rule: ScoringRule,
    ) : ScoreSource
}
