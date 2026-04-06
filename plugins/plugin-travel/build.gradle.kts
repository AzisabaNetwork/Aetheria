dependencies {
    compileOnly(project(":plugins:plugin-island"))
    compileOnly(project(":plugins:plugin-runtime"))
    implementation(libs.koin.core)
}
