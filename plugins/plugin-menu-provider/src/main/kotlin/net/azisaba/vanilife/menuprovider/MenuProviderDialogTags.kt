package net.azisaba.vanilife.menuprovider

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent
import io.papermc.paper.registry.keys.tags.DialogTagKeys
import io.papermc.paper.tag.PostFlattenTagRegistrar

object MenuProviderDialogTags {
    internal fun bootstrap(event: ReloadableRegistrarEvent<PostFlattenTagRegistrar<Dialog>>) {
        event.registrar().addToTag(DialogTagKeys.PAUSE_SCREEN_ADDITIONS, listOf(MenuProviderDialogs.MENU))
        event.registrar().addToTag(DialogTagKeys.QUICK_ACTIONS, listOf(MenuProviderDialogs.MENU))
    }
}
