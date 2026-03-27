Purpose

This repository is a Folia (PaperMC fork) multi-module Gradle project. This file gives Copilot sessions concise, repository-specific guidance (commands, architecture, and conventions) so suggestions and code edits are aligned with the build/patch workflow.

Build, test, and lint commands

- Build full project (root):
  ./gradlew build

- Run server (development / packaged):
  ./gradlew :folia-server:runDevServer    # run from compiled classes (fast iteration)
  ./gradlew :folia-server:runServer       # run from assembled/reobf jar
  Other run tasks: runReobfServer, runPaperclip, runBundler (see folia-server build script)

- Generate development bundle (dev artifacts used by IDEs or publishing):
  ./gradlew generateDevelopmentBundle
  (publishDevBundle is gated by gradle property publishDevBundle)

- Run a single test (class or method):
  ./gradlew :<module>:test --tests "com.example.MyTest"
  ./gradlew :<module>:test --tests "com.example.MyTest.testMethod"
  Notes: tests use JUnit Platform, forkEvery=1, and exclude tag "Slow" by default.

- Run all tests for a module:
  ./gradlew :folia-server:test

- Lint/check:
  ./gradlew check
  (check depends on scanJarForBadCalls in folia-server)

- Useful info tasks:
  ./gradlew printMinecraftVersion
  ./gradlew printPaperVersion

High-level architecture (big picture)

- Multi-module Gradle project rooted at "vanilife". Primary modules:
  - folia-api: public API surface (published as Maven artifact)
  - folia-server: server implementation (fork/patch of upstream Paper/paper-server)
  - plugins/: many plugin subprojects (plugins:plugin-*) built as Shadow jars

- Upstream/patch workflow uses paperweight (paperweight/patcher plugin):
  - The root build maps upstream paper-api/paper-server files into this repo via patchFile/patchDir configurations.
  - Patches live in folia-api/paper-patches and folia-server/paper-patches; the build applies patches to produce output in paper-api / paper-server source sets.

- Source layout and compilation:
  - Kotlin + Java (toolchain configured for Java 21).
  - folia-server/paper-api often reference ../paper-server sources via sourceSets to compose the final runtime.
  - Plugins compileOnly against :folia-api and are packaged with ShadowJar (archiveClassifier set to empty to produce a plain plugin jar).

- Runtime and developer workflow:
  - runDevServer uses project classes for quick runs; runServer and runReobfServer use assembled jars.
  - Gradle run tasks automatically collect plugin shadow jars and pass them as --add-plugin args to the server run tasks.

Key repository conventions (non-obvious patterns)

- Versioning and upstream refs:
  - Important properties live in gradle.properties (mcVersion, apiVersion, paperRef). When updating upstream Paper, update paperRef and mcVersion accordingly.

- Patching model:
  - paperweight is used to manage upstream forks and apply patches. Keep patch files in the project-specific patch dirs (folia-server/paper-patches and folia-api/paper-patches). The root build.gradle.kts defines patchFile mappings; avoid renaming those mappings without updating the build.

- Plugins:
  - Plugin projects follow the plugins:plugin-* pattern in settings.gradle.kts.
  - Shadow jars are produced with no classifier (archiveClassifier.set("")) so runtime tasks can attach them directly.
  - Plugins should declare compileOnly(project(":folia-api")) and, if marking compatibility for Folia, add folia-supported: true in plugin.yml to opt-in to loading under Folia.

- Tests:
  - Tests are JUnit Jupiter (JUnit Platform) with mockito agent configured. The build registers a mockitoAgent configuration and adds a JVM argument provider for tests. When running tests manually or in CI outside Gradle, ensure the mockito -javaagent is available.
  - Tests are configured to exclude tags named "Slow" by default; use system properties or gradle arguments to include them when desired.

- Java and tooling:
  - Toolchain targets Java 21 (see build scripts and mise.toml which references zulu-21). Use the Gradle toolchain; do not hardcode a JDK path in IDE configs.

- Packaging and publishing:
  - folia-api is intended to be published to a Maven repository (paper/papermc repos are referenced). The root build config sets up a dev bundle and optional publishing behavior.

AI assistant config files

- No CLAUDE.md, .cursorrules, AGENTS.md, .windsurfrules, CONVENTIONS.md, AIDER_CONVENTIONS.md, .clinerules, or .cline_rules were found. If you have existing assistant configs, add them alongside this file so Copilot can merge guidance.

Where to read more

- Project overview and Folia docs are linked from README.md and REGION_LOGIC.md; upstream PaperMC folia docs are authoritative for region logic and scheduler semantics.

What this file is intended for

- Keep Copilot suggestions aligned with this repo's build, run, patch, and test workflows. When adding code that touches upstream-mapped sources or patches, prefer editing in the patch areas (folia-*/paper-patches) rather than directly modifying the generated paper-api/paper-server outputs.

Module details

- folia-api (./folia-api)
  - Purpose: Public API surface and documentation artifacts. Intended to be published to Maven.
  - Build: ./gradlew :folia-api:build
  - Tests: ./gradlew :folia-api:test  (single test: --tests "com.example.MyTest")
  - Notes: generated sources are placed under paper-api/src/generated/java and paperweight/paper-patches are used to manage upstream mappings.

- folia-server (./folia-server)
  - Purpose: Server implementation (fork/patch of upstream Paper/paper-server) and primary runtime.
  - Build: ./gradlew :folia-server:build  (produces reobf/jar outputs)
  - Run (dev): ./gradlew :folia-server:runDevServer  (fast iteration against compiled classes)
  - Run (packaged): ./gradlew :folia-server:runServer or :folia-server:runReobfServer
  - Other run tasks: runBundler, runPaperclip, runReobfPaperclip (see folia-server build script)
  - Tests: ./gradlew :folia-server:test  (single test: --tests "com.example.MyTest")
  - Important tasks: generateDevelopmentBundle, scanJarForBadCalls (check depends on this), registerRunTask helpers add plugin jars to server args.
  - Notes: sourceSets reference ../paper-server sources and the build config embeds many manifest attributes (git, mcVersion). Tests configure a mockito -javaagent provider.

- plugins (./plugins, modules: plugins:plugin-cooking, plugins:plugin-farming, plugins:plugin-fishing, plugins:plugin-forestry, plugins:plugin-islands, plugins:plugin-mining, plugins:plugin-npc, plugins:plugin-pack-host, plugins:plugin-portal, plugins:plugin-tool-swap)
  - Purpose: Game-specific plugin projects compiled against :folia-api and packaged as Shadow jars.
  - Build: ./gradlew :plugins:plugin-<name>:shadowJar  (or ./gradlew :plugins:plugin-<name>:build)
  - Packaging: shadowJar configured with archiveClassifier.set("") to emit a plain plugin jar consumed by run tasks.
  - Runtime: runDevServer and other run tasks automatically include built plugin jars via --add-plugin args.
  - Conventions: plugins should use compileOnly(project(":folia-api")) and opt-in to Folia by setting folia-supported: true in plugin.yml when applicable.

- paper-api / paper-server (upstream-mapped sources)
  - These are not direct modules in settings.gradle.kts but are referenced and produced/consumed by the paperweight patch process and sourceSets. Edits to upstream-mapped code should generally be made via the patch directories (folia-api/paper-patches and folia-server/paper-patches) rather than editing generated outputs.

- test-plugin (optional)
  - folia-server's run tasks detect a child project named "test-plugin" and will mount its jar automatically if present. Useful for quick local plugin testing.

If you want changes

- Request edits to this document to add any project-specific scripts, CI steps, or additional conventions to capture.
