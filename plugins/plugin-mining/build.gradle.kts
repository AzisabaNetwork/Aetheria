repositories {
    maven("https://maven.playpro.com/")
}

dependencies {
    compileOnly(project(":plugins:plugin-runtime"))
    compileOnly(libs.bettercommand)
    compileOnly(libs.betterhud.bukkit.api)
    compileOnly(libs.betterhud.standard.api)
    compileOnly(libs.coreprotect)
}
