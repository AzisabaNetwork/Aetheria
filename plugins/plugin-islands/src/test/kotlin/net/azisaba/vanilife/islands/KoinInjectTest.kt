package net.azisaba.vanilife.islands

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

private interface TestService { fun value(): String }
private class TestServiceImpl(private val v: String) : TestService { override fun value() = v }

class KoinInjectTest : StringSpec({
    afterTest { try { stopKoin() } catch (_: Exception) {} }

    "koin inject should provide bound instance" {
        val testModule = module {
            single<TestService> { TestServiceImpl("ok") }
        }

        startKoin { modules(testModule) }

        class Consumer : KoinComponent {
            val svc: TestService by inject()
        }

        val c = Consumer()
        c.svc.value() shouldBe "ok"
    }
})
