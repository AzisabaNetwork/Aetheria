import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

tasks.withType<AbstractArchiveTask>().configureEach {
    enabled = false
}

subprojects {
    apply(plugin = "com.gradleup.shadow")

    dependencies {
        compileOnly(project(":folia-api"))
    }

    tasks.named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
    }
}

gradle.projectsEvaluated {
    val pluginShadowJarTasks = subprojects.map { it.tasks.named<ShadowJar>("shadowJar") }
    val pluginShadowJarFiles = subprojects.map { project ->
        project.tasks.named<ShadowJar>("shadowJar").flatMap { it.archiveFile }
    }

    listOf("runServer", "runDevServer").forEach { taskName ->
        (rootProject.findProject(":folia-server")?.tasks?.findByName(taskName) as? JavaExec)?.apply {
            dependsOn(pluginShadowJarTasks)
            doFirst {
                pluginShadowJarFiles.forEach { shadowJarFile ->
                    args("--add-plugin", shadowJarFile.get().asFile.absolutePath)
                }
            }
        }
    }
}
