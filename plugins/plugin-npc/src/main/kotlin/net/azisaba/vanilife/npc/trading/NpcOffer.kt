package net.azisaba.vanilife.npc.trading

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.azisaba.vanilife.Season
import org.bukkit.inventory.MerchantRecipe
import kotlin.random.Random

@Serializable
sealed interface NpcOffer {
    fun offer(random: Random): MerchantRecipe?

    @Serializable
    @SerialName("Item")
    data class Item(
        val result: ItemStackProvider,
        val cost: ItemStackProvider,
        val secondaryCost: ItemStackProvider?,
        val maxUses: Int = 8,
    ) : NpcOffer {
        override fun offer(random: Random): MerchantRecipe = MerchantRecipe(result.provide(random), maxUses).apply {
            addIngredient(cost.provide(random))
            secondaryCost?.provide(random)?.let(::addIngredient)
        }
    }

    @Serializable
    @SerialName("RandomChoice")
    data class RandomChoice(val entries: List<NpcOffer>) : NpcOffer {
        override fun offer(random: Random): MerchantRecipe? =
            if (entries.isNotEmpty()) entries.random(random).offer(random) else null
    }

    @Serializable
    @SerialName("WeightedRandomChoice")
    data class WeightedRandomChoice(val entries: List<Entry>) : NpcOffer {
        override fun offer(random: Random): MerchantRecipe? {
            if (entries.isEmpty()) return null

            val totalWeight = entries.sumOf(Entry::weight)
            if (totalWeight <= 0) return null

            val roll = random.nextInt(totalWeight)
            var accumulatedWeight = 0
            for (entry in entries) {
                accumulatedWeight += entry.weight
                if (roll < accumulatedWeight) {
                    return entry.offer.offer(random)
                }
            }

            return null
        }

        @Serializable
        data class Entry(val offer: NpcOffer, val weight: Int)
    }

    @Serializable
    @SerialName("WithProbability")
    data class WithProbability(val offer: NpcOffer, val chance: Double) : NpcOffer {
        override fun offer(random: Random): MerchantRecipe? =
            if (random.nextDouble() <= chance) offer.offer(random) else null
    }

    @Serializable
    @SerialName("WithSeasons")
    data class WithSeasons(
        val offer: NpcOffer,
        val seasons: Set<@Serializable(with = SubSeasonSerializer::class) Season.Sub>,
    ) : NpcOffer {
        override fun offer(random: Random): MerchantRecipe? =
            if (Season.Sub.now() in seasons) offer.offer(random) else null
    }

    @Serializable
    @SerialName("WithSeasonalDiscount")
    data class WithSeasonalDiscount(val offer: NpcOffer, val discountRate: Double = 0.5) : NpcOffer {
        override fun offer(random: Random): MerchantRecipe? {
            val base = offer.offer(random) ?: return null
            val primaryCost = base.ingredients.firstOrNull() ?: return base

            val targetPeriod = base.result.serverItem()?.peakSeason() ?: return base
            if (Season.Sub.now() in targetPeriod) {
                base.specialPrice = -(primaryCost.amount * discountRate).toInt()
            }

            return base
        }
    }
}
