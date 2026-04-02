dependencies {
    compileOnly(project(":plugins:plugin-enchanting"))
    compileOnly(project(":plugins:plugin-island"))
    compileOnly(project(":plugins:plugin-runtime"))
    implementation(libs.koin.core)
}
