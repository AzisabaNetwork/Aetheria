dependencies {
    compileOnly(project(":plugins:plugin-cooking"))
    compileOnly(project(":plugins:plugin-fishing"))
    compileOnly(project(":plugins:plugin-forestry"))
    compileOnly(project(":plugins:plugin-islands"))
    compileOnly(project(":plugins:plugin-runtime"))
    compileOnly(libs.bettermodel.bukkit.api)
    implementation(libs.koin.core)
}
