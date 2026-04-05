package net.azisaba.vanilife.enchanting.dialog

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.azisaba.vanilife.enchanting.EnchantingFonts
import net.azisaba.vanilife.enchanting.EnchantingItemModels
import net.azisaba.vanilife.enchanting.EnchantingTranslations
import net.azisaba.vanilife.enchanting.UnlockRateSource
import net.azisaba.vanilife.island.Island
import net.azisaba.vanilife.island.IslandFonts
import net.azisaba.vanilife.island.enchantment.EnchantmentAccessor
import net.azisaba.vanilife.island.wrack.WrackType
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import kotlin.math.roundToInt

object EnchantmentsDialog {
    val TITLE: Component = Component.text()
        .append(
            Component.text(EnchantingFonts.EnchantingIcons.ENCHANTING_TABLE)
                .font(EnchantingFonts.ENCHANTING_ICONS)
        )
        .appendSpace()
        .append(Component.translatable(EnchantingTranslations.DIALOG_VANILIFE_ENCHANTMENTS))
        .build()

    fun create(island: Island): Dialog = Dialog.create { builder ->
        builder.empty()
            .base(
                DialogBase.builder(TITLE)
                    .body(
                        buildList {
                            add(summary(island))
                            addAll(section(island, 1, 9))
                            addAll(section(island, 10, 19))
                            addAll(section(island, 20, 29))
                            addAll(section(island, 30, 39))
                            addAll(section(island, 40, 50))
                        }
                    )
                    .build()
            )
            .type(DialogType.notice())
    }

    private fun summary(enchantments: EnchantmentAccessor): PlainMessageDialogBody = DialogBody.plainMessage(
        Component.translatable(
            EnchantingTranslations.DIALOG_VANILIFE_ENCHANTMENTS_SUMMARY,
            Component.text()
                .append(Component.text(enchantments.enchantments.size, NamedTextColor.GREEN))
                .append(Component.text("/", NamedTextColor.GRAY))
                .append(Component.text(WrackType.filterIsInstance<WrackType.Enchantment>().size))
                .build()
        )
    )

    private fun section(island: Island, from: Int, to: Int): List<DialogBody> = buildList {
        add(
            DialogBody.plainMessage(
                Component.text()
                    .append(
                        Component.text(IslandFonts.LevelIcons.levelOf(from))
                            .font(IslandFonts.LEVEL_ICONS)
                    )
                    .append(
                        Component.translatable(
                            EnchantingTranslations.DIALOG_VANILIFE_ENCHANTMENTS_SECTION,
                            Component.text(from),
                            Component.text(to),
                        )
                    )
                    .build()
            )
        )

        val wrackTypes = WrackType.filterIsInstance<WrackType.Enchantment>()
            .filter { wrackType -> wrackType.targetLevel.min in from..to }
            .sortedBy { it.targetLevel.min }

        for (wrackType in wrackTypes) {
            add(if (island.has(wrackType.enchantment)) unlocked(wrackType) else locked(wrackType))
        }
    }

    private fun locked(wrackType: WrackType.Enchantment): DialogBody = DialogBody.item(
        ItemStack.of(Material.STICK).apply {
            setData(
                DataComponentTypes.ITEM_NAME,
                Component.text("I wanna commit suicide", NamedTextColor.DARK_GRAY, TextDecoration.OBFUSCATED) // 自殺したい...
                    .font(Key.key("alt"))
            )
            setData(DataComponentTypes.ITEM_MODEL, EnchantingItemModels.DIALOG_ENCHANTMENT_LOCKED)
            lore(listOf(requiredLevel(wrackType), unlockRate(wrackType.enchantment)))
        }
    ).description(
        DialogBody.plainMessage(Component.text("???", NamedTextColor.GRAY))
    ).build()

    private fun unlocked(wrackType: WrackType.Enchantment): DialogBody = DialogBody.item(
        ItemStack.of(Material.STICK).apply {
            setData(DataComponentTypes.ITEM_NAME, Component.translatable(wrackType.enchantment))
            setData(DataComponentTypes.ITEM_MODEL, EnchantingItemModels.DIALOG_ENCHANTMENT_UNLOCKED)
            lore(listOf(requiredLevel(wrackType), unlockRate(wrackType.enchantment)))
        }
    ).description(
        DialogBody.plainMessage(Component.translatable(wrackType.enchantment))
    ).build()

    private fun requiredLevel(wrackType: WrackType.Enchantment): Component =
        Component.translatable(
            EnchantingTranslations.DIALOG_VANILIFE_ENCHANTMENTS_REQUIRED_LEVEL,
            NamedTextColor.GRAY,
            Component.text()
                .color(NamedTextColor.WHITE)
                .append(
                    Component.text(IslandFonts.LevelIcons.levelOf(wrackType.targetLevel.min))
                        .font(IslandFonts.LEVEL_ICONS)
                )
                .append(Component.text(wrackType.targetLevel.min))
                .build(),
        ).decoration(TextDecoration.ITALIC, false)

    private fun unlockRate(enchantment: Enchantment): Component = Component.text()
        .color(NamedTextColor.GRAY)
        .decoration(TextDecoration.ITALIC, false)
        .append(
            Component.text(EnchantingFonts.EnchantingIcons.UNLOCK_RATE)
                .font(EnchantingFonts.ENCHANTING_ICONS)
        )
        .append(
            Component.translatable(
                EnchantingTranslations.DIALOG_VANILIFE_ENCHANTMENTS_UNLOCK_RATE,
                Component.text((UnlockRateSource[enchantment] * 100).roundToInt(), NamedTextColor.WHITE),
            )
        )
        .build()
}
