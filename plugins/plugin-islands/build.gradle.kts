dependencies {
    compileOnly(project(":plugins:plugin-data-driven"))
    compileOnly(project(":plugins:plugin-runtime"))
    implementation(libs.entitylib)
    implementation(libs.koin.core)
    implementation(libs.minecraftexposed.adventure)
}
