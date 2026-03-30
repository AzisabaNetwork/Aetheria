package net.azisaba.vanilife.server.world.wiper;

import org.jspecify.annotations.NullMarked;

/**
 * Lifecycle states for resource world hot-swap.
 * Transitions: {@code ACTIVE → DRAINING → CLOSING → DELETED → CREATING → ACTIVE}.
 */
@NullMarked
public enum WorldLifecycleState {
    /** World is operational — players may enter. */
    ACTIVE,

    /** Teleporting players out; rejecting new entry. */
    DRAINING,

    /** Chunk system halting, data saving, removing from registries. */
    CLOSING,

    /** World files deleted from disk. No ServerLevel exists. */
    DELETED,

    /** Fresh ServerLevel being created and initial chunks loading. */
    CREATING
}
