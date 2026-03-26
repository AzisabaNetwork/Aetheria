package net.azisaba.vanilife.npc

import kr.toxicity.model.api.animation.AnimationIterator
import kr.toxicity.model.api.animation.AnimationModifier
import kr.toxicity.model.api.bukkit.platform.BukkitEntity
import kr.toxicity.model.api.tracker.Tracker
import net.azisaba.vanilife.npc.ai.SitGoal
import net.azisaba.vanilife.npc.ai.TradingGoal
import net.azisaba.vanilife.npc.trading.NpcOffersLoader
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Chicken
import org.bukkit.inventory.Merchant
import org.bukkit.util.Vector
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NpcWrapper internal constructor(
    val npcType: NpcType, internal val delegate: Chicken,
) : Audience, KoinComponent, NpcOwnerAccessor by NpcOwnerAccessorImpl(delegate) {
    val merchant: Merchant = Bukkit.createMerchant()

    private val offersLoader: NpcOffersLoader by inject()

    val location: Location
        get() = delegate.location

    val velocity: Vector
        get() = delegate.velocity

    val isSitting: Boolean
        get() = tracker.bones().any { bone -> bone.runningAnimation()?.name == "sit" }

    private val tracker: Tracker = npcType.modelOrThrow().create(BukkitEntity(delegate))

    init {
        Bukkit.getMobGoals().addGoal(delegate, 3, TradingGoal(this, delegate, tracker))
        Bukkit.getMobGoals().addGoal(delegate, 2, SitGoal(this, delegate, tracker))
        rollMerchantRecipes()
    }

    fun sitDown() {
        tracker.animate(
            "sit",
            AnimationModifier.builder()
                .type(AnimationIterator.Type.LOOP)
                .build()
        )
    }

    fun standUp() {
        tracker.stopAnimation("sit")
    }

    fun rollMerchantRecipes() {
        merchant.recipes = offersLoader.get(npcType).roll()
    }

    fun remove() {
        if (delegate.isValid) {
            delegate.remove()
        }
        dispose()
    }

    fun dispose() {
        tracker.close()
    }
}
