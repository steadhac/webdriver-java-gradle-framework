# Gradle — What, Why, and When

## What is Gradle?

Gradle is a **build automation tool** for JVM-based projects (Java, Kotlin, Groovy, Scala). It handles:

- **Compiling** source code
- **Managing dependencies** (downloading libraries like Selenium, RestAssured, etc.)
- **Running tests** and generating reports
- **Packaging** applications (JARs, WARs)
- **Custom tasks** (e.g., `apiTests`, `uiTests`, `allureReport`)

Gradle uses a **Groovy or Kotlin DSL** instead of XML, making build scripts concise and programmable.

## Core concepts

| Concept | What it is |
|---|---|
| `build.gradle` | The build script — declares plugins, dependencies, tasks, and configuration. |
| `settings.gradle` | Defines the project name and multi-project structure. |
| `gradlew` / `gradlew.bat` | The **Gradle Wrapper** — a checked-in script that downloads and uses a specific Gradle version so every developer and CI server uses the same one. |
| `gradle/wrapper/` | Contains the wrapper JAR and properties (including the pinned Gradle version). |
| **Task** | A unit of work (e.g., `compileJava`, `test`, `apiTests`). Tasks can depend on each other. |
| **Plugin** | Adds tasks and conventions (e.g., `java`, `io.qameta.allure`). |
| **Dependency** | A library your project needs, declared with a scope like `implementation` or `testImplementation`. |
| **Repository** | Where Gradle downloads dependencies from (e.g., Maven Central, `mavenCentral()`). |

## Gradle vs Maven vs other tools

| Criteria | Gradle | Maven | Ant |
|---|---|---|---|
| **Config format** | Groovy/Kotlin DSL (code) | XML (`pom.xml`) | XML (`build.xml`) |
| **Readability** | High — concise, scriptable | Medium — verbose but structured | Low — very verbose |
| **Performance** | Fast — incremental builds, build cache, daemon | Slower — always rebuilds | Manual — no built-in caching |
| **Flexibility** | Very high — custom tasks are just code | Low — plugin-driven, hard to customize | High — but everything is manual |
| **Dependency mgmt** | Built-in, supports BOMs, platforms, version catalogs | Built-in, mature | Requires Ivy |
| **IDE support** | Excellent (IntelliJ, VS Code, Eclipse) | Excellent | Limited |
| **Learning curve** | Moderate | Low–moderate | High for complex builds |
| **Community/ecosystem** | Large, growing (Android default) | Very large, mature | Shrinking |

## When to choose Gradle

| Scenario | Recommendation |
|---|---|
| **New Java/Kotlin project** | **Gradle** — modern defaults, fast builds, concise config. |
| **Android development** | **Gradle** — it's the only officially supported build tool. |
| **Test automation frameworks** | **Gradle** — easy custom tasks (`apiTests`, `uiTests`), good plugin ecosystem (Allure, Selenium). |
| **Multi-module projects** | **Gradle** — composite builds and included builds simplify large repos. |
| **Legacy enterprise project already on Maven** | **Maven** — migration cost may not be worth it unless builds are painfully slow. |
| **Simple single-module library** | Either works — Maven is simpler if you don't need custom tasks. |
| **Team has no Gradle experience** | **Maven** — lower learning curve; switch later if needed. |
| **Maximum build speed matters** | **Gradle** — incremental compilation, build cache, and configuration cache give significant speedups. |

## Why this project uses Gradle

This framework uses Gradle because:

1. **Custom test tasks** — `apiTests` and `uiTests` are defined as simple task blocks, not XML profiles.
2. **Allure integration** — the `io.qameta.allure` Gradle plugin wires report generation with minimal config.
3. **Gradle Wrapper** — `gradlew` guarantees the same Gradle version locally and in CI.
4. **Fast feedback** — the Gradle daemon keeps the JVM warm between runs, speeding up local iteration.

## Quick reference — common commands

| Command | What it does |
|---|---|
| `./gradlew tasks` | List all available tasks. |
| `./gradlew build` | Compile + test + assemble. |
| `./gradlew apiTests` | Run API tests only. |
| `./gradlew uiTests` | Run UI tests only. |
| `./gradlew allureReport` | Generate the Allure HTML report. |
| `./gradlew dependencies` | Print the full dependency tree. |
| `./gradlew clean` | Delete the `build/` directory. |
| `./gradlew --stop` | Stop the Gradle daemon. |

## Further reading

- [Gradle User Manual](https://docs.gradle.org/current/userguide/userguide.html)
- [Gradle vs Maven (official comparison)](https://gradle.org/maven-vs-gradle/)
- [Gradle Wrapper docs](https://docs.gradle.org/current/userguide/gradle_wrapper.html)