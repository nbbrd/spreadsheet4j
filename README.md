# Spreadsheet facade for Java

[![Download](https://img.shields.io/github/release/nbbrd/spreadsheet4j.svg)](https://github.com/nbbrd/java-service-util/releases/latest)
[![Changes](https://img.shields.io/endpoint?url=https%3A%2F%2Fraw.githubusercontent.com%2Fnbbrd%2Fnbbrd-maven-tools%2Fbadges%2Funreleased-changes.json)](https://github.com/nbbrd/spreadsheet4j/blob/develop/CHANGELOG.md)

## Overview

**spreadsheet4j** is a lightweight Java facade for reading and writing spreadsheet files. Write your code once against a single API (`Book` / `Sheet` / `Cell`) and switch formats &mdash; Excel, OpenDocument, XML, HTML, Markdown &mdash; by adding or removing a runtime dependency. Format providers are discovered automatically through the Java `ServiceLoader` SPI.

### Key features

- **One API, many formats** &mdash; identical code reads `.xlsx`, `.xls`, `.xlsb`, `.ods`, `.xml`, HTML and Markdown.
- **Pluggable at runtime** &mdash; drop a format module on the classpath; no wiring required.
- **Lightweight core** &mdash; `spreadsheet-api` is a pure-Java, zero-dependency module targeting Java 8.
- **Pick your trade-off** &mdash; e.g. `spreadsheet-xl` for fast native `.xlsx`, `spreadsheet-poi` for full `.xls`/`.xlsx`/`.xlsb` compatibility, or `spreadsheet-fastexcel` for write-optimized output.
- **Modular &amp; JPMS-ready** &mdash; each module ships a `module-info.java` for the Java Platform Module System.
- **Standalone uber-jar** &mdash; `spreadsheet-standalone` shades a curated set of providers for environments without a dependency manager.

### Supported formats

| Module                  | Formats                              | Read | Write | Backed by         |
|-------------------------|--------------------------------------|:----:|:-----:|-------------------|
| `spreadsheet-api`       | _(core API &mdash; no format)_       | &mdash; | &mdash; | _none_         |
| `spreadsheet-xl`        | Excel `.xlsx`                        | ✓    | ✓     | LMAX Disruptor    |
| `spreadsheet-fastexcel` | Excel `.xlsx` _(write-optimized)_    |      | ✓     | fastexcel         |
| `spreadsheet-poi`       | Excel `.xls`, `.xlsx`, `.xlsb` _(read-only)_ | ✓ | ✓ _(xls/xlsx)_ | Apache POI |
| `spreadsheet-od`        | OpenDocument `.ods`                  | ✓    | ✓     | SODS              |
| `spreadsheet-xmlss`     | XML Spreadsheet 2003 `.xml`          | ✓    | ✓     | _(pure Java)_     |
| `spreadsheet-html`      | HTML tables                          | ✓    | ✓     | jsoup             |
| `spreadsheet-markdown`  | Markdown tables                      | ✓    | ✓     | flexmark          |

### Getting started

Declare the [BOM](spreadsheet-bom) to align versions, then add the API plus the format providers you need:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.github.nbbrd.spreadsheet4j</groupId>
            <artifactId>spreadsheet-bom</artifactId>
            <version>LATEST</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>com.github.nbbrd.spreadsheet4j</groupId>
        <artifactId>spreadsheet-api</artifactId>
    </dependency>
    <dependency>
        <groupId>com.github.nbbrd.spreadsheet4j</groupId>
        <artifactId>spreadsheet-xl</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

**Read a spreadsheet** &mdash; pick the first factory that accepts the file, then iterate:

```java
File file = new File("data.xlsx");

Book.Factory factory = BookFactoryLoader.load().stream()
        .filter(Book.Factory::canLoad)
        .filter(f -> f.accept(file))
        .findFirst()
        .orElseThrow(() -> new IOException("No factory found for: " + file));

try (Book book = factory.load(file)) {
    book.forEach((sheet, index) -> {
        System.out.println("Sheet: " + sheet.getName());
        sheet.forEachValue((row, col, value) ->
                System.out.printf("  [%d,%d] = %s%n", row, col, value));
    });
}
```

**Write a spreadsheet** &mdash; build an in-memory book and hand it to any writing factory:

```java
ArraySheet sheet = ArraySheet.builder()
        .name("Report")
        .row(0, 0, "Year", "Revenue")
        .row(1, 0, 2025, 1_000_000)
        .row(2, 0, 2026, 1_250_000)
        .build();

Book book = ArrayBook.builder().sheet(sheet).build();

BookFactoryLoader.loadById("Excel").orElseThrow()
        .store(new File("report.xlsx"), book);
```


## Developing

This project is written in Java and uses [Apache Maven](https://maven.apache.org/) as a build tool.  
It requires [Java 8 as minimum version](https://whichjdk.com/) and all its dependencies are hosted on [Maven Central](https://search.maven.org/).

The code can be build using any IDE or by just type-in the following commands in a terminal:

```shell
git clone https://github.com/nbbrd/spreadsheet4j.git
cd spreadsheet4j
mvn clean install
```

## Contributing

Any contribution is welcome and should be done through pull requests and/or issues.

## Licensing

The code of this project is licensed under the [European Union Public Licence (EUPL)](https://joinup.ec.europa.eu/page/eupl-text-11-12).
