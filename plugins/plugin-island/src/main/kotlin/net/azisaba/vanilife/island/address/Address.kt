package net.azisaba.vanilife.island.address

import kotlin.random.Random

data class Address(val value: @AddressPattern String) {
    init {
        requireValid(value)
    }

    fun prefix(): String = PREFIX

    fun body(): String = value.substring(PREFIX_LENGTH)

    companion object {
        const val PREFIX: String = "IA-"
        const val PREFIX_LENGTH: Int = PREFIX.length
        const val MIN_BODY_LENGTH: Int = 3
        const val MAX_BODY_LENGTH: Int = 12
        const val MIN_LENGTH: Int = PREFIX_LENGTH + MIN_BODY_LENGTH
        const val MAX_LENGTH: Int = PREFIX_LENGTH + MAX_BODY_LENGTH
        const val REGEX_PATTERN: String = "^$PREFIX[A-Z0-9]{$MIN_BODY_LENGTH,$MAX_BODY_LENGTH}$"

        private val regex: Regex = Regex(REGEX_PATTERN)
        private val safeChars: CharArray = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray()

        fun isValid(value: String): Boolean = regex.matches(value)

        fun requireValid(value: String): @AddressPattern String {
            require(isValid(value)) {
                "Invalid island address: $value"
            }
            return value
        }

        fun random(random: Random = Random.Default): Address {
            val length = random.nextInt(MIN_BODY_LENGTH, MAX_BODY_LENGTH + 1)

            val body = buildString(length) {
                repeat(length) {
                    append(safeChars[random.nextInt(safeChars.size)])
                }
            }

            return Address(PREFIX + body)
        }
    }
}
