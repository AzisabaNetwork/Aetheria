dependencies {
    compileOnly(project(":plugins:plugin-enchanting"))
    compileOnly(project(":plugins:plugin-island"))
    compileOnly(project(":plugins:plugin-portal"))
    compileOnly(project(":plugins:plugin-runtime"))
    compileOnly(project(":plugins:plugin-travel"))
    implementation(libs.koin.core)
}
