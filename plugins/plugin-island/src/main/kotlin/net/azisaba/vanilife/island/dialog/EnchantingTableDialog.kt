package net.azisaba.vanilife.island.dialog

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.azisaba.vanilife.island.IslandFonts
import net.kyori.adventure.text.Component
import org.bukkit.block.BlockType
import org.bukkit.inventory.ItemStack

internal object EnchantingTableDialog {
    fun create(itemStack: ItemStack): Dialog = Dialog.create { builder ->
        builder.empty()
            .base(
                DialogBase.builder(
                    Component.text()
                        .append(Component.text(IslandFonts.Enchants.ENCHANTING_TABLE).font(IslandFonts.ENCHANTS))
                        .appendSpace()
                        .append(Component.translatable(BlockType.ENCHANTING_TABLE))
                        .build()
                )
                    .body(
                        listOf(
                            DialogBody.item(itemStack)
                                .build()
                        )
                    )
                    .build()
            )
            .type(
                DialogType.multiAction(
                    RegistryAccess.registryAccess()
                        .getRegistry(RegistryKey.ENCHANTMENT)
                        .filter { it.supportedItems.contains(itemStack.type.asItemType()!!.key()) }
                        .map { enchantment ->
                            ActionButton.builder(
                                Component.text()
                                    .append(
                                        Component.text(IslandFonts.Enchants.ENCHANTED_BOOK).font(IslandFonts.ENCHANTS)
                                    )
                                    .appendSpace()
                                    .append(enchantment.description())
                                    .build()
                            ).build()
                        }
                ).columns(1).exitAction(
                    ActionButton.builder(Component.translatable("gui.done"))
                        .build()
                ).build()
            )
    }
}
