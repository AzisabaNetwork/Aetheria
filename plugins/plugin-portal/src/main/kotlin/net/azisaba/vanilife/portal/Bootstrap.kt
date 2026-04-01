package net.azisaba.vanilife.portal

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.registry.event.RegistryEvents

class Bootstrap : PluginBootstrap {
    override fun bootstrap(context: BootstrapContext) {
        context.lifecycleManager.registerEventHandler(
            RegistryEvents.DIALOG.compose().newHandler(PortalDialogs::bootstrap)
        )
    }
}
