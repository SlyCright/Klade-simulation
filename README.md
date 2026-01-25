# Klade Simulation - Shared ECS Engine

**Version:** 2025.01.25_ver.02

## Description
The shared simulation engine for Klade Evolutionary Simulation Game. This library contains the Entity Component System (ECS) framework, physics calculations, and genetic algorithms that power both the server-side simulation and client-side visualization. It serves as the architectural backbone ensuring consistency between backend logic and frontend rendering.

## Live Site: [klade.site](https://klade.site/)

## Vision
A high-performance, deterministic simulation core that enables complex evolutionary computations while maintaining strict separation between simulation logic and presentation layers. The engine is designed to be deterministic and reproducible, allowing the same genetic algorithms to run identically on both server and client for verification and replay purposes.

## Tech Stack
- **Core Framework**: Ashley ECS 1.7.4
- **Language**: Java 11 (libGDX/GWT compatibility)
- **Build System**: Gradle (Groovy DSL) with Maven publication
- **Compatibility**: GWT-ready for HTML5 client compilation

## Project Architecture
This repository contains the **simulation/** component of the three-project Klade architecture:
- **main**: Spring Boot + Vaadin management UI (separate repository at https://github.com/SlyCright/Klade)
- **stage**: libGDX HTML5 simulation client (separate repository at https://github.com/SlyCright/klade-stage)
- **simulation** (this repo): Shared ECS components, physics engine, genetic algorithms

## Setup for Development
1. Open this folder as a standalone project
2. Configure Gradle JVM to Java 11 or higher
3. Run `./gradlew publishToMavenLocal` to make the library available to main and stage projects
4. The library will be available as `site.klade:simulation:0.0.1` in local Maven repository

## Building for Production
Execute `./gradlew publishToMavenLocal` before building dependent projects. For CI/CD, this task runs automatically as a dependency of the main project's build.

## Integration Notes
The simulation library must maintain **strict GWT compatibility** - no Java features beyond what 
GWT 2.11.0 supports. It uses `gwt` plugin in Gradle to validate compatibility. Both main and 
stage projects depend on this library.

The library intentionally contains **no networking, authentication, or rendering code** to maintain purity and reusability.

## Development Constraints
- **Java 11 maximum**: Must be GWT-compatible (stage requirement)
- **No Spring dependencies**: Library must remain framework-agnostic
- **Deterministic logic**: All simulation updates must produce identical results given same inputs
- **Immutable DTOs**: All data transfer objects should be immutable snapshots

## Community & Support
- **Main Repository**: https://github.com/SlyCright/Klade
- **Stage Repository**: https://github.com/SlyCright/klade-stage
- **Funding**: https://boosty.to/klade (RUS)
- **License**: Apache 2.0

## License
This project is licensed under the Apache License 2.0 - see the LICENSE file for details.