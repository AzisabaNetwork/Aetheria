package net.azisaba.vanilife

fun interface ConfigurationHolder<T : Any> {
    fun value(): T

    fun <S : Any> map(accessor: (T) -> S): ConfigurationHolder<S> = ConfigurationHolder {
        value().let(accessor)
    }
}
