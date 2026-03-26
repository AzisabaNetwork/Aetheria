package net.azisaba.vanilife.npc

import kr.toxicity.model.api.BetterModel
import kr.toxicity.model.api.data.renderer.ModelRenderer
import net.azisaba.vanilife.Vanilife
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed

enum class NpcType(val key: Key, val icon: Char, val modelName: String) : Keyed {
    CAVEMAN(
        key = Key.key(Vanilife.NAMESPACE, "caveman"),
        icon = NpcFonts.NpcIcons.CAVEMAN,
        modelName = "npc_caveman",
    ),
    CREEPER(
        key = Key.key(Vanilife.NAMESPACE, "creeper"),
        icon = NpcFonts.NpcIcons.CREEPER,
        modelName = "npc_creeper",
    ),
    ENDER(
      key = Key.key(Vanilife.NAMESPACE, "ender"),
        icon = NpcFonts.NpcIcons.ENDER,
        modelName = "npc_ender",
    ),
    NEKO(
        key = Key.key(Vanilife.NAMESPACE, "neko"),
        icon = NpcFonts.NpcIcons.NEKO,
        modelName = "npc_neko",
    );

    override fun key(): Key = key

    fun modelOrThrow(): ModelRenderer = BetterModel.model(modelName).orElseThrow()

    companion object {
        val BY_KEY: Map<Key, NpcType> = entries.associateBy { it.key }

        fun byKey(key: Key): NpcType? = BY_KEY[key]
    }
}
