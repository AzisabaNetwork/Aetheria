dependencies {
    compileOnly(project(":plugins:plugin-runtime"))
    implementation(libs.entitylib)
    implementation(libs.koin.core)
}
