package net.azisaba.vanilife.npc.wrapper

import kr.toxicity.model.api.animation.AnimationIterator
import kr.toxicity.model.api.animation.AnimationModifier
import kr.toxicity.model.api.tracker.Tracker

interface NpcPoseAccessor {
    val isSitting: Boolean

    fun sitDown()

    fun standUp()
}

internal class NpcPoseAccessorImpl(private val tracker: Tracker) : NpcPoseAccessor {
    override val isSitting: Boolean
        get() = tracker.bones().any { bone -> bone.runningAnimation()?.name == SIT }

    override fun sitDown() {
        tracker.animate(
            SIT,
            AnimationModifier.builder()
                .type(AnimationIterator.Type.LOOP)
                .build(),
        )
    }

    override fun standUp() {
        tracker.stopAnimation(SIT)
    }

    companion object {
        const val SIT: String = "sit"
    }
}
