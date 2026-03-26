package net.azisaba.vanilife.npc.wrapper

import net.azisaba.vanilife.npc.NpcType
import net.azisaba.vanilife.npc.trading.NpcOffersLoader
import org.bukkit.Bukkit
import org.bukkit.inventory.Merchant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface NpcMerchantAccessor {
    val merchant: Merchant

    fun rollMerchantRecipes()
}

internal class NpcMerchantAccessorImpl(private val npcType: NpcType) : KoinComponent, NpcMerchantAccessor {
    override val merchant: Merchant = Bukkit.createMerchant()

    private val offersLoader: NpcOffersLoader by inject()

    init {
        rollMerchantRecipes()
    }

    override fun rollMerchantRecipes() {
        val offers = offersLoader.get(npcType)
        merchant.recipes = offers.roll()
    }
}
