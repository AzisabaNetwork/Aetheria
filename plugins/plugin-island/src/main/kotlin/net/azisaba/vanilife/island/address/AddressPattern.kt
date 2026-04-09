package net.azisaba.vanilife.island.address

import org.intellij.lang.annotations.Pattern

@Pattern(Address.REGEX_PATTERN)
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.LOCAL_VARIABLE, AnnotationTarget.PROPERTY, AnnotationTarget.TYPE,
)
annotation class AddressPattern
