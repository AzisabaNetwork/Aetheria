dependencies {
    compileOnly(project(":plugins:plugin-runtime"))
    compileOnly(libs.bettermodel.bukkit.api)
    implementation(libs.entitylib)
}
