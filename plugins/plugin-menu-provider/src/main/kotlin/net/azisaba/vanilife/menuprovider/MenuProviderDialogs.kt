package net.azisaba.vanilife.menuprovider

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry
import io.papermc.paper.registry.event.RegistryComposeEvent
import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.menuprovider.dialog.MenuDialog
import net.kyori.adventure.key.Key

object MenuProviderDialogs {
    val MENU: TypedKey<Dialog> = RegistryKey.DIALOG.typedKey(Key.key(Vanilife.NAMESPACE, "menu"))

    internal fun bootstrap(event: RegistryComposeEvent<Dialog, DialogRegistryEntry.Builder>) {
        event.registry().register(MENU, MenuDialog::bootstrap)
    }
}
