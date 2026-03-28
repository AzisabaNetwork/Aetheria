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
        if (project.path != ":plugins:plugin-runtime") {
            dependencies {
                exclude(dependency("org.jetbrains.kotlin:.*"))
                exclude(dependency("org.jetbrains.kotlinx:.*"))
            }
        }
    }
}

gradle.projectsEvaluated {
    val pluginProjects = subprojects.filter { it.name.startsWith("plugin-") }
    val pluginShadowJarTasks = pluginProjects.map { it.tasks.named<ShadowJar>("shadowJar") }
    val pluginShadowJarFiles = pluginProjects.map { project ->
        project.tasks.named<ShadowJar>("shadowJar").flatMap { it.archiveFile }
    }

    tasks.register("shadowPluginModules") {
        group = "build"
        description = "Builds shadow JARs for plugin modules under :plugins."
        dependsOn(pluginShadowJarTasks)
    }

    listOf("runServer", "runDevServer").forEach { taskName ->
        (rootProject.findProject(":folia-server")?.tasks?.findByName(taskName) as? JavaExec)?.apply {
            dependsOn(pluginShadowJarTasks)
            argumentProviders.add(
                objects.newInstance(AddPluginArgumentProvider::class.java).apply {
                    pluginJars.from(pluginShadowJarFiles)
                },
            )
        }
    }
}

abstract class AddPluginArgumentProvider : CommandLineArgumentProvider {
    @get:InputFiles
    abstract val pluginJars: ConfigurableFileCollection

    override fun asArguments(): Iterable<String> =
        pluginJars.files.flatMap { listOf("--add-plugin", it.absolutePath) }
}
