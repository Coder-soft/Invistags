# InvisTags by CoderSoft

A **client-side Fabric mod** that forces nametags and player skins of invisible players to render at full opacity. All features are toggleable at runtime via the `/invis` command.

Built for **Minecraft 1.20.4**, **1.21.11**, **26.1.2**, **26.2**, and **26.3**.

## Features

- **Nametag visibility** — Forces nametags to render above invisible players regardless of render distance or status effects.
- **Full nametag opacity** — Restores the nametag background and text to 100% opacity (bypasses the transparency applied by the invisibility effect).
- **Skin visibility** — Renders the player body/skin at full opacity instead of the ghostly semi-transparent effect.
- **Sneaking fix** — Corrects the sneaking pose for invisible players when the nametag is forced visible.
- **Invisible mob support** — Optional reveal for invisible mobs' bodies, plus nametags for custom-named invisible mobs (off by default).
- **Persistent settings** — Toggles are saved to `config/invistags.json` and restored on launch.
- **Keybinds** — `I` toggles skin + nametags together, `O` opens the config screen.
- **Config GUI** — Cloth Config screen, reachable through Mod Menu or `/invis config`.
- **Runtime toggling** — All features can be enabled/disabled mid-game without restarting.

## Requirements

| Dependency | 1.20.4 | 1.21.11 | 26.1.2 | 26.2 | 26.3 |
|---|---|---|---|---|---|
| Minecraft | `1.20.4` | `1.21.11` | `26.1.2` | `26.2` | `26.3` |
| Fabric Loader | `>=0.19.3` | `>=0.19.3` | `>=0.19.3` | `>=0.19.3` | `>=0.19.3` |
| Fabric API | `0.107.0+1.20.4` | `0.141.4+1.21.11` | `0.152.1+26.1.2` | `0.152.2+26.2` | `0.164.0+26.3` |
| Java (JDK) | `21` | `21` | `25` | `25` | `25` |

**Cloth Config is required** for the config screen (`21.11.153` on 1.21.11, `26.2.155` on 26.2, `26.3.159` on 26.3). **Mod Menu is optional** — install it to reach the config screen from the Mods list; the `O` keybind and `/invis config` work without it.

## Version Branches

| Branch | MC Version | Notes |
|---|---|---|
| `1.20.4` | 1.20.4 | Entity-based rendering (no render states), Fabric Loom |
| `main` | 1.21.11 | Render-state-based, Fabric Loom |
| `26.1` | 26.1.2 | Avatar API, unobfuscated mappings, Fabric Loom |
| `26` | 26.2 | Avatar API, unobfuscated mappings, Fabric Loom |
| `26.3` | 26.3 | Avatar API, SDL keyboard input, unobfuscated mappings, Fabric Loom |

## Building from Source

### 1. Clone the Repository

```bash
git clone https://github.com/Coder-soft/Invistags.git
cd Invistags
```

### 2. Switch to the Desired Version Branch

```bash
git checkout 1.20.4   # MC 1.20.4
git checkout main     # MC 1.21.11  (default)
git checkout 26.1     # MC 26.1.2
git checkout 26       # MC 26.2
git checkout 26.3     # MC 26.3
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

The first build will download Gradle, Minecraft assets, and all dependencies.

### 4. Locate the Build Artifact

```
build/libs/invistags-1.0-SNAPSHOT.jar
```

## Installation

### 1. Install Fabric Loader

Download and run the Fabric installer from [fabricmc.net/use](https://fabricmc.net/use). Select your target Minecraft version and **Client**.

### 2. Install Fabric API

Download the matching Fabric API version from [Modrinth](https://modrinth.com/mod/fabric-api) or [CurseForge](https://curseforge.com/minecraft/mc-mods/fabric-api).

### 3. Install InvisTags

Copy the built JAR to your Minecraft `mods` folder:

**Linux / macOS:**
```bash
cp build/libs/invistags-1.0-SNAPSHOT.jar ~/.minecraft/mods/
```

**Windows:**
```cmd
copy build\libs\invistags-1.0-SNAPSHOT.jar %appdata%\.minecraft\mods\
```

Your `mods` folder should contain at minimum:

```
mods/
├── fabric-api-<version>.jar
└── invistags-1.0-SNAPSHOT.jar
```

### 4. Launch

Start the Fabric profile in the Minecraft launcher. Verify the mod loaded by checking the Mods menu — look for **Invistags**.

## Usage

Use the `/invis` command in-game:

| Command | Effect |
|---|---|
| `/invis` | Shows current toggle state of all features |
| `/invis config` | Opens the Cloth Config screen |
| `/invis skin true` | Enables rendering of invisible players' skins at full opacity |
| `/invis skin false` | Disables skin override (reverts to vanilla behavior) |
| `/invis nametag true` | Enables rendering of invisible players' nametags |
| `/invis nametag false` | Disables nametag override (reverts to vanilla behavior) |
| `/invis mobs true` | Reveals invisible mobs (nametags only for custom-named mobs) |
| `/invis mobs false` | Disables mob reveal (reverts to vanilla behavior) |

### Keybinds

| Default key | Action |
|---|---|
| `I` | Toggle skin + nametag reveal together |
| `O` | Open the InvisTags config screen |

Both keybinds can be changed in **Options → Controls → Key Binds**.

Skins and nametags default to **enabled**; mob reveal defaults to **disabled**. All settings persist across restarts.

## API Differences Between Versions

The 26.x branches use a significantly different rendering API (unobfuscated Mojang mappings):

| 1.21.x | 26.x |
|---|---|
| `PlayerEntityRenderer` | `AvatarRenderer` |
| `PlayerEntityRenderState` | `AvatarRenderState` |
| `OrderedRenderCommandQueue` | `SubmitNodeCollector` |
| `render()` | `submit()` |
| `renderLabelIfPresent()` | `submitNameDisplay()` |
| `hasLabel()` | `shouldShowName()` |
| `ClientCommandManager` | `ClientCommands` |
| `state.invisible` | `state.isInvisible` / `state.isInvisibleToPlayer` |
| `state.sneaking` | `state.isCrouching` |
| `CameraRenderState` (top-level) | `state.level.CameraRenderState` |

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
    │   │   ├── CleaspfabricClient.java     — Client entrypoint, /invis command, keybinds
    │   │   ├── CleaspfabricDataGenerator.java
    │   │   ├── InvistagsConfigScreen.java  — Shared Cloth Config screen
    │   │   ├── ModConfig.java              — Toggle state + JSON persistence
    │   │   └── ModMenuIntegration.java     — Mod Menu entrypoint
    │   └── mixin/client/
    │       ├── EntityRendererMixin.java    — Forces shouldShowName() to true
    │       ├── InvisibilityRenderMixin.java— Restores skin opacity
    │       ├── LabelRenderMixin.java       — Restores nametag opacity
    │       └── LabelSneakMixin.java        — Fixes sneaking pose
    └── resources/
        ├── assets/invistags/lang/en_us.json — Keybind/config strings
        └── cleaspfabric.client.mixins.json — Client mixin registration
```

## Development

Run the headless unit tests (config persistence, no Minecraft launch required):

```bash
./gradlew test
```

Open the project in IntelliJ IDEA or Eclipse:

```bash
# IntelliJ: File → Open → select project folder
# Eclipse: File → Import → Gradle → Existing Gradle Project
```

To generate an IntelliJ IDEA project from scratch:

```bash
./gradlew idea
```

## License

All Rights Reserved. See [LICENSE.txt](LICENSE.txt).

Copyright (c) 2026 CoderSoft.
