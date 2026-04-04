package net.azisaba.vanilife.enchanting

import net.azisaba.packed.PackedKey
import net.azisaba.packed.models.PackModel
import net.azisaba.vanilife.Vanilife
import net.kyori.adventure.key.Key

object EnchantingModels {
    val A: PackedKey<PackModel> = PackedKey.key(Vanilife.NAMESPACE, "a")

    val B: PackedKey<PackModel> = PackedKey.key(Vanilife.NAMESPACE, "b")

    val DIALOG_ENCHANTMENT_LOCKED: PackedKey<PackModel> = PackedKey.key(Vanilife.NAMESPACE, "locked")
    val DIALOG_ENCHANTMENT_UNLOCKED: PackedKey<PackModel> = PackedKey.key(Vanilife.NAMESPACE, "unlocked")

    fun a(): PackModel = PackModel.item(Key.key(Vanilife.NAMESPACE, "item/a"))

    fun b(): PackModel = PackModel.item(Key.key(Vanilife.NAMESPACE, "item/b"))

    fun locked(): PackModel = PackModel.item(Key.key(Vanilife.NAMESPACE, "item/dialog/enchantments/locked"))

    fun unlocked(): PackModel = PackModel.item(Key.key(Vanilife.NAMESPACE, "item/dialog/enchantments/unlocked"))
}
