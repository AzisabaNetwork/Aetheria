package net.azisaba.vanilife.npc.trading

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.azisaba.vanilife.Season

internal object SubSeasonSerializer : KSerializer<Season.Sub> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("SubSeason") {
        element<String>("season")
        element<String>("stage")
    }

    override fun serialize(encoder: Encoder, value: Season.Sub) {
        encoder.beginStructure(descriptor).apply {
            encodeStringElement(descriptor, 0, value.season().name)
            encodeStringElement(descriptor, 1, value.stage().name)
            endStructure(descriptor)
        }
    }

    override fun deserialize(decoder: Decoder): Season.Sub {
        val composite = decoder.beginStructure(descriptor)
        var season: String? = null
        var stage: String? = null

        while (true) {
            when (val index = composite.decodeElementIndex(descriptor)) {
                CompositeDecoder.DECODE_DONE -> break
                0 -> season = composite.decodeStringElement(descriptor, index)
                1 -> stage = composite.decodeStringElement(descriptor, index)
                else -> error("Unexpected index: $index")
            }
        }
        composite.endStructure(descriptor)

        return Season.valueOf(requireNotNull(season)).withStage(Season.Stage.valueOf(requireNotNull(stage)))
    }
}
