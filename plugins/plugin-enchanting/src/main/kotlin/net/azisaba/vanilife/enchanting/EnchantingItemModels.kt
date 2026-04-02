package net.azisaba.vanilife.enchanting

import net.azisaba.packed.PackedKey
import net.azisaba.packed.itemModel
import net.azisaba.packed.items.PackItemModel
import net.azisaba.packed.items.properties.PackModelItemModelProperties
import net.azisaba.vanilife.Vanilife

object EnchantingItemModels {
    val DIALOG_ENCHANTMENT_LOCKED: PackedKey<PackItemModel> = PackedKey.itemModel(Vanilife.NAMESPACE, "dialog/enchantment/locked")
    val DIALOG_ENCHANTMENT_UNLOCKED: PackedKey<PackItemModel> = PackedKey.itemModel(Vanilife.NAMESPACE, "dialog/enchantment/unlocked")

    fun locked(): PackItemModel = PackItemModel(PackModelItemModelProperties(EnchantingModels.DIALOG_ENCHANTMENT_LOCKED))

    fun unlocked(): PackItemModel = PackItemModel(PackModelItemModelProperties(EnchantingModels.DIALOG_ENCHANTMENT_UNLOCKED))
}
