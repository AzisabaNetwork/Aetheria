dependencies {
    compileOnly(libs.bettermodel.bukkit.api)
    compileOnly(libs.packed.core)
    compileOnly(libs.packed.resource)
    compileOnly(libs.packetevents)
    implementation(libs.entitylib)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.hikaricp)
    implementation(libs.koin.core)
    implementation(libs.mccoroutine.folia.api)
    implementation(libs.mccoroutine.folia.core)
    implementation(libs.postgresql)
    implementation(libs.tomlkt)
}

// Test dependencies for unit tests (Kotest + MockK + coroutines test)
dependencies {
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
    testImplementation("io.kotest:kotest-assertions-core:5.6.2")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

tasks.test {
    useJUnitPlatform()
}

// Paper API for test compile (provides BlockPosition etc.)
dependencies {
    testImplementation("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
}
