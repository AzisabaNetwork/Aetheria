package net.azisaba.vanilife.island

import net.azisaba.packed.PackedKey
import net.azisaba.packed.soundEvent
import net.azisaba.packed.sounds.PackSound
import net.azisaba.packed.sounds.PackSoundEvent
import net.azisaba.packed.sounds.PackSoundType
import net.azisaba.vanilife.Vanilife
import net.kyori.adventure.key.Key

object IslandSoundEvents {
    val ISLAND_LEVEL_UP: PackedKey<PackSoundEvent> = PackedKey.soundEvent(Vanilife.NAMESPACE, "island.level_up")

    fun levelUp(): PackSoundEvent = PackSoundEvent(
        sounds = listOf(
            PackSound(
                type = PackSoundType.FILE,
                name = Key.key(Vanilife.NAMESPACE, "island/level_up"),
                weight = 1,
            )
        ),
    )
}
