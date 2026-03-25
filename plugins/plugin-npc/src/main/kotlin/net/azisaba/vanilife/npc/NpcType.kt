package net.azisaba.vanilife.npc

import kr.toxicity.model.api.BetterModel
import kr.toxicity.model.api.data.renderer.ModelRenderer
import net.azisaba.vanilife.Vanilife
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed

enum class NpcType(val key: Key, val icon: Char, val modelName: String) : Keyed {
    NEKO(
        key = Key.key(Vanilife.NAMESPACE, "neko"),
        icon = NpcFonts.NpcIcons.NEKO,
        modelName = "npc",
    ),
    CAVEMAN(
        key = Key.key(Vanilife.NAMESPACE, "caveman"),
        icon = NpcFonts.NpcIcons.NEKO,
        modelName = "caveman",
    );

    override fun key(): Key = key

    fun modelOrThrow(): ModelRenderer = BetterModel.model(modelName).orElseThrow()

    companion object {
        val BY_KEY: Map<Key, NpcType> = entries.associateBy { it.key }

        fun byKey(key: Key): NpcType? = BY_KEY[key]

        fun byInput(input: String): NpcType? {
            val key = if (':' in input) Key.key(input) else Key.key(Vanilife.NAMESPACE, input)
            return byKey(key)
        }
    }
}
