# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

### Changed

- Improve `FastExcelBookFactory` name

### Fixed

- Fix dependency inheritance in BOM

## [2.5.1] - 2022-06-20

### Fixed

- Fix javadoc generation on module `spreadsheet-fastexcel`

## [2.5.0] - 2022-06-17

### Added

- Add lightweight xlsx writer [#5](https://github.com/nbbrd/spreadsheet4j/issues/5)

### Changed

- Deprecate `Sheet#inv()`
- Deprecate `Book.Factory#load(URL)`
- Deprecate `Cell`, `Sheet#getCell(int,int)`, `Sheet#forEach(SheetConsumer)`
- Replace `Book#getSheetCount()` with `Book#getSheetCount2()`

## [2.4.0] - 2022-06-14

### Added

- Add `module-info.java` to `spreadsheet-poi`
- Add media types to identify spreadsheet types
- Add sorting of book factories by rank

### Changed

- Move Excel native reader to its own module (`spreadsheet-xl`)

### Fixed

## [2.3.0] - 2022-06-08

### Added

- Add Maven BOM

### Fixed

- Fix issues with high memory usage (SODS)
- Fix Zip64 issue (POI)
- Fix invalid file lock on Windows+JDK8 (POI)

## [2.2.6] - 2020-03-25

### Changed

- Migration to Maven-Central
- **Breaking change:** Maven groupId is now `com.github.nbbrd.spreadsheet4j`

## [2.2.5] - 2020-02-26

### Fixed

- Fix XXE vulnerability (POI)
- Fix memory usage while writing large xlsx files (POI)
- Fix issue while reading huge sheets (SODS)
- Fix loading of blank cells (SODS)
- Fix loading of Excel-generated files (SODS)
- Fix several issues in underlying dependencies

## [2.2.4] - 2019-10-03

### Added

- Add new OpenDocument engine
- Add partial support of JPMS
- Add detection of invalid OpenDocument file

### Changed

- Repack java-io-util into spreadsheet-util

### Fixed

- Fix InvalidPathException
- Fix struct datetime parsing in .xlsx file

## [2.2.3] - 2018-12-19

### Added

- Add support of inline strings in .xlsx
- Add support of headers in xmlss

### Changed

- Improve file type detection
- Improve error reporting

### Fixed

- Fix overflow when dealing with unrealistic dimension in .xlsx
- Fix parsing of empty cell in .xlsx

## [2.2.2] - 2018-11-27

### Changed

- Improve performance and memory consumption of xlsx parser

### Fixed

- JDK11 cleanup

[Unreleased]: https://github.com/nbbrd/spreadsheet4j/compare/v2.5.1...HEAD
[2.5.1]: https://github.com/nbbrd/spreadsheet4j/compare/v2.5.0...v2.5.1
[2.5.0]: https://github.com/nbbrd/spreadsheet4j/compare/v2.4.0...v2.5.0
[2.4.0]: https://github.com/nbbrd/spreadsheet4j/compare/v2.3.0...v2.4.0
[2.3.0]: https://github.com/nbbrd/spreadsheet4j/compare/v2.2.6...v2.3.0
[2.2.6]: https://github.com/nbbrd/spreadsheet4j/compare/v2.2.5...v2.2.6
[2.2.5]: https://github.com/nbbrd/spreadsheet4j/compare/v2.2.4...v2.2.5
[2.2.4]: https://github.com/nbbrd/spreadsheet4j/compare/v2.2.3...v2.2.4
[2.2.3]: https://github.com/nbbrd/spreadsheet4j/compare/v2.2.2...v2.2.3
[2.2.2]: https://github.com/nbbrd/spreadsheet4j/releases/tag/v2.2.2
