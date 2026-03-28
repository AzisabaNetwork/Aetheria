dependencies {
    compileOnly(project(":plugins:plugin-islands"))
    compileOnly(project(":plugins:plugin-runtime"))
    implementation(libs.koin.core)
}
