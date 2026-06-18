# InvisNoMore — Always Show Invisible Players

A **client-side Fabric mod** for **Minecraft 1.21.11** that forces nametags and player skins of invisible players to render at full opacity. All features are toggleable at runtime via the `/invis` command.

Also branded as **Invistags** (mod ID: `invistags`).

## Features

- **Nametag visibility** — Forces nametags to render above invisible players regardless of render distance or status effects.
- **Full nametag opacity** — Restores the nametag background and text to 100% opacity (bypasses the transparency applied by the invisibility effect).
- **Skin visibility** — Renders the player body/skin at full opacity instead of the ghostly semi-transparent effect.
- **Sneaking fix** — Corrects the sneaking pose for invisible players when the nametag is forced visible.
- **Runtime toggling** — All features can be enabled/disabled mid-game without restarting.

## Requirements

| Dependency | Version |
|---|---|
| [Minecraft](https://minecraft.net) | `1.21.11` |
| [Fabric Loader](https://fabricmc.net/use) | `>=0.19.3` |
| [Fabric API](https://modrinth.com/mod/fabric-api) | `0.141.4+1.21.11` |
| [Java](https://adoptium.net) (JDK) | `21` (required by Minecraft 1.21.x) |

For building from source:

| Tool | Version |
|---|---|
| [Gradle](https://gradle.org) (wrapper) | `9.5.0` (auto-downloaded by `gradlew`) |
| [Fabric Loom](https://github.com/FabricMC/fabric-loom) | `1.17-SNAPSHOT` (Gradle plugin) |
| [Yarn Mappings](https://fabricmc.net/develop) | `1.21.11+build.6` |

## Building from Source

### 1. Verify JDK 21

```bash
java -version
# Must show: openjdk version "21.0.x" ...
```

If you do not have JDK 21, download it from [Adoptium](https://adoptium.net/temurin/releases/?version=21).

### 2. Clone the Repository

```bash
git clone https://github.com/Coder-soft/InvisNoMore.git
cd InvisNoMore
```

### 3. Build the Mod

**Linux / macOS:**

```bash
./gradlew build
```

**Windows:**

```cmd
gradlew.bat build
```

The first build will download Gradle 9.5.0 (via the wrapper), Minecraft assets, and all dependencies — this may take a few minutes depending on your internet connection.

### 4. Locate the Build Artifact

```
build/libs/invisnomore-1.0-SNAPSHOT.jar
```

## Installation

### 1. Install Fabric Loader

Download and run the Fabric installer from [fabricmc.net/use](https://fabricmc.net/use). Select **Minecraft 1.21.11** and **Client**.

### 2. Install Fabric API

Download **Fabric API 0.141.4+1.21.11** from:
- [Modrinth](https://modrinth.com/mod/fabric-api)
- [CurseForge](https://curseforge.com/minecraft/mc-mods/fabric-api)

### 3. Install InvisNoMore

Copy the built JAR to your Minecraft `mods` folder:

**Linux / macOS:**
```bash
cp build/libs/invisnomore-1.0-SNAPSHOT.jar ~/.minecraft/mods/
```

**Windows:**
```cmd
copy build\libs\invisnomore-1.0-SNAPSHOT.jar %appdata%\.minecraft\mods\
```

Your `mods` folder should contain at minimum:

```
mods/
├── fabric-api-0.141.4+1.21.11.jar
└── invisnomore-1.0-SNAPSHOT.jar
```

### 4. Launch

Start the Fabric profile in the Minecraft launcher. Verify the mod loaded by checking the Mods menu — look for **Invistags**.

## Usage

Use the `/invis` command in-game:

| Command | Effect |
|---|---|
| `/invis` | Shows current toggle state of both features |
| `/invis skin true` | Enables rendering of invisible players' skins at full opacity |
| `/invis skin false` | Disables skin override (reverts to vanilla behavior) |
| `/invis nametag true` | Enables rendering of invisible players' nametags |
| `/invis nametag false` | Disables nametag override (reverts to vanilla behavior) |

Both features default to **enabled** on first launch.

## Building for Other Minecraft Versions

Edit `gradle.properties` to target a different Minecraft version, ensuring the corresponding Fabric API version exists:

```properties
minecraft_version=1.21.11
yarn_mappings=1.21.11+build.6
loader_version=0.19.3
fabric_version=0.141.4+1.21.11
```

Check available versions at [modmuss50.me/fabric.html](https://modmuss50.me/fabric.html).

## Project Structure

```
src/
├── main/
│   ├── java/org/codersoft/cleaspfabric/
│   │   ├── Cleaspfabric.java              — Mod entrypoint (ModInitializer)
│   │   └── ...
│   └── resources/
│       ├── fabric.mod.json                 — Mod metadata
│       ├── cleaspfabric.mixins.json        — Main source set mixins (reserved)
│       └── assets/invistags/icon.png       — Mod icon
└── client/
    ├── java/org/codersoft/cleaspfabric/
    │   ├── client/
    │   │   ├── CleaspfabricClient.java     — Client entrypoint, /invis command
    │   │   ├── CleaspfabricDataGenerator.java
    │   │   └── ModConfig.java              — Toggle state
    │   └── mixin/client/
    │       ├── EntityRendererMixin.java    — Forces hasLabel() to true
    │       ├── InvisibilityRenderMixin.java— Restores skin opacity
    │       ├── LabelRenderMixin.java       — Restores nametag opacity
    │       └── LabelSneakMixin.java        — Fixes sneaking pose
    └── resources/
        └── cleaspfabric.client.mixins.json — Client mixin registration
```

## Development

Open the project in IntelliJ IDEA or Eclipse:

```bash
# IntelliJ: File → Open → select project folder
# Eclipse: File → Import → Gradle → Existing Gradle Project
```

Run configurations are included at `.idea/runConfigurations/` for IntelliJ users:

- **Minecraft Client** — Launches the Fabric client with the mod loaded
- **Minecraft Server** — Launches a Fabric dedicated server
- **Data Generation** — Runs the Fabric data generator

To generate an IntelliJ IDEA project from scratch:

```bash
./gradlew idea
```

## License

All Rights Reserved. See [LICENSE.txt](LICENSE.txt).

Copyright (c) 2026 CoderSoft.
