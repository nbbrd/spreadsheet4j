# AGENTS.md — Spreadsheet facade for Java

## Overview

spreadsheet4j is a **Java facade library** (EUPL-licensed, since 2014) that provides a unified API for reading and writing spreadsheet files in multiple formats. It abstracts over various underlying libraries behind a single, format-agnostic interface, so consumers only depend on `spreadsheet-api` and choose format implementations at runtime via the Java `ServiceLoader` SPI mechanism.

The core abstraction is a three-level model:

- **`Book`** — a spreadsheet workbook (maps to a file); also hosts the `Book.Factory` SPI that implementations register.
- **`Sheet`** — a named grid of cells within a book.
- **`Cell`** — a single value (string, number, or date) within a sheet.

## Architecture

The project is a multi-module Maven build. Modules fall into three layers:

### API layer

| Module | Role |
|---|---|
| `spreadsheet-api` | Public API (`Book`, `Sheet`, `Cell`, `Book.Factory` SPI) and helper utilities. No format-specific code. |

### Format implementation layer

Each module implements `Book.Factory` and registers it via `ServiceLoader`. All depend only on `spreadsheet-api` plus one external library.

| Module | Format | Underlying library |
|---|---|---|
| `spreadsheet-xl` | Excel `.xlsx` (native, no POI) | `com.lmax:disruptor` |
| `spreadsheet-fastexcel` | Excel `.xlsx` (write-optimized) | `org.dhatim:fastexcel` |
| `spreadsheet-poi` | Excel `.xls`/`.xlsx` (full-featured) | Apache POI |
| `spreadsheet-od` | OpenDocument Spreadsheet `.ods` | SODS |
| `spreadsheet-xmlss` | XML Spreadsheet 2003 `.xml` | _(none — pure Java)_ |
| `spreadsheet-html` | HTML tables | jsoup |
| `spreadsheet-markdown` | Markdown tables | flexmark |

### Distribution layer

| Module | Role |
|---|---|
| `spreadsheet-standalone` | Shaded uber-jar bundling `spreadsheet-api` + selected format modules (xl, fastexcel, html, od, xmlss). Suitable for environments without a dependency manager. |
| `spreadsheet-bom` | Maven Bill of Materials (BOM) for version alignment. |

### SPI / extension model

`Book.Factory` is annotated with `@ServiceDefinition` (from `java-service-util`) which generates the `BookFactoryLoader` service loader glue at compile time. Format modules simply provide a concrete `Book.Factory` implementation and the annotation processor registers it. Consumers load all available factories via `BookFactoryLoader.load()` and select the appropriate one by file extension or MIME type.


## Build & Test

```shell
mvn clean install                 # full build + tests + enforcer checks
mvn clean install -Pyolo          # skip all checks (fast local iteration)
mvn test -pl <module-name> -Pyolo # fast test a single module
mvn test -pl <module-name> -am    # full test a single module
```

- **Java 8 target** with JPMS `module-info.java` compiled separately on JDK 9+ (see `java8-with-jpms` profile in root POM)
- **JUnit 5** with parallel execution enabled (`junit.jupiter.execution.parallel.enabled=true`); **AssertJ** for assertions

## Key Conventions

- **Lombok**: use lombok annotations when possible. Config in `lombok.config`: `addNullAnnotations=jspecify`, `builder.className=Builder`
- **Nullability**: `@org.jspecify.annotations.Nullable` for nullable; `@lombok.NonNull` for non-null parameters. Return types use `@Nullable` or the `OrNull` suffix (e.g., `getThingOrNull`)
- **Design annotations** use annotations from `java-design-util` such as `@VisibleForTesting`, `@StaticFactoryMethod`, `@DirectImpl`, `@MightBeGenerated`, `@MightBePromoted`
- **Internal packages**: `internal.<project>.*` are implementation details; public API lives in the root and `spi` packages
- **Static analysis**: `forbiddenapis` (no `jdk-unsafe`, `jdk-deprecated`, `jdk-internal`, `jdk-non-portable`, `jdk-reflection`), `modernizer`
- **Reproducible builds**: `project.build.outputTimestamp` is set in the root POM
- **Formatting/style**: 
  - Use IntelliJ IDEA default code style for Java
  - Follow existing formatting and match naming conventions exactly
  - Follow the principles of "Effective Java"
  - Follow the principles of "Clean Code"
- **Java/JVM**: 
  - Target version defined in root POM properties; some modules may require higher versions
  - Use modern Java feature compatible with defined version

## Agent behavior

- Do respect existing architecture, coding style, and conventions
- Do prefer minimal, reviewable changes
- Do preserve backward compatibility
- Do not introduce new dependencies without justification
- Do not rewrite large sections for cleanliness
- Do not reformat code
- Do not propose additional features or changes beyond the scope of the task
