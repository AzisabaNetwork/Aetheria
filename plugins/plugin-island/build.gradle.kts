dependencies {
    compileOnly(project(":plugins:plugin-runtime"))
    compileOnly(libs.bettercommand)
    compileOnly(libs.betterhud.bukkit.api)
    compileOnly(libs.betterhud.standard.api)
    compileOnly(libs.bettermodel.bukkit.api)
    implementation(libs.entitylib)
    implementation(libs.koin.core)
}
