# HookVisuals 1.0.38 source

This reconstructed source archive contains the HookVisuals client sources and resources, plus the decompiled Swordcore source used for its movement and combat controller. It targets Minecraft 1.21.11, Fabric, and Java 21.

The project keeps the runtime dependency JARs under `libs/`. The source copy under `third_party/swordcore-decompiled/` is provided for inspection; the Gradle build uses `libs/swordcore-runtime.jar` for binary compatibility. Several legacy project files are reconstructed from decompiled classes and may need manual repair for a complete source build.

Build with `./gradlew build` in an environment with network access and the required Gradle dependencies available.
