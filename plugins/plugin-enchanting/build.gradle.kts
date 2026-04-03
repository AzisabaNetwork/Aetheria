dependencies {
    compileOnly(project(":plugins:plugin-island"))
    compileOnly(project(":plugins:plugin-runtime"))
    compileOnly(libs.packetevents)
    compileOnly(libs.entitylib)
    implementation(libs.koin.core)
}
