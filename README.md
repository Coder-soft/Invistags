# InvisTags by CoderSoft

A **client-side Fabric mod** that forces nametags, player skins, armour and held items of invisible players to render, and flags players a server has vanished from the tab list. All features are toggleable at runtime via the `/invis` command.

> **Beta build.** The gear and vanished-player features are new and have only been smoke-tested in a singleplayer client. Treat this build as untested on multiplayer servers.

Built for **Minecraft 1.21.11**, **26.1.2**, **26.2**, and **26.3**.

## Features

- **Nametag visibility** — Forces nametags to render above invisible players regardless of render distance or status effects.
- **Full nametag opacity** — Restores the nametag background and text to 100% opacity (bypasses the transparency applied by the invisibility effect).
- **Skin visibility** — Renders the player body/skin at full opacity instead of the ghostly semi-transparent effect.
- **Gear reveal** — Restores armour and held items on invisible players when a server strips them, using the last gear your client saw. Vanilla itself keeps gear visible; servers that send empty equipment packets are the target here.
- **Vanished-player flag** — Detects player entities the server sent but hid from the tab list, adds a red `[VANISHED]` suffix to their nametag and posts a one-time chat alert.
- **Sneaking fix** — Corrects the sneaking pose for invisible players when the nametag is forced visible.
- **Invisible mob support** — Optional reveal for invisible mobs' bodies, plus nametags for custom-named invisible mobs (off by default).
- **State persistence** — **Remember state** is on by default, so the last toggles are restored on launch. Turn it off to start with everything disabled every time.
- **Keybinds** — `I` toggles skin + nametags + gear together, `O` opens the config screen.
- **Config GUI** — Cloth Config screen, reachable through Mod Menu or `/invis config`.
- **Runtime toggling** — All features can be enabled/disabled mid-game without restarting.

## Requirements

| Dependency | 1.21.11 | 26.1.2 | 26.2 | 26.3 |
|---|---|---|---|---|
| Minecraft | `1.21.11` | `26.1.2` | `26.2` | `26.3` |
| Fabric Loader | `>=0.19.3` | `>=0.19.3` | `>=0.19.3` | `>=0.19.3` |
| Fabric API | `0.141.4+1.21.11` | `0.152.1+26.1.2` | `0.152.2+26.2` | `0.164.0+26.3` |
| Java (JDK) | `21` | `25` | `25` | `25` |

**Cloth Config is required** for the config screen (`21.11.153` on 1.21.11, `26.2.155` on 26.2, `26.3.159` on 26.3). **Mod Menu is optional** — install it to reach the config screen from the Mods list; the `O` keybind and `/invis config` work without it.

## Version Branches

| Branch | MC Version | Notes |
|---|---|---|
| `main` | 1.21.11 | Render-state-based, Fabric Loom |
| `26.1` | 26.1.2 | Avatar API, unobfuscated mappings, Fabric Loom |
| `26` | 26.2 | Avatar API, unobfuscated mappings, Fabric Loom |
| `26.3` | 26.3 | Avatar API, SDL keyboard input, unobfuscated mappings, Fabric Loom |

## Building from Source

### 1. Clone the Repository

```bash
git clone https://github.com/MohenjoMC/Invistags.git
cd Invistags
```

### 2. Switch to the Desired Version Branch

```bash
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
build/libs/invistags-1.1.0-beta.1.jar
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
cp build/libs/invistags-1.1.0-beta.1.jar ~/.minecraft/mods/
```

**Windows:**
```cmd
copy build\libs\invistags-1.1.0-beta.1.jar %appdata%\.minecraft\mods\
```

Your `mods` folder should contain at minimum:

```
mods/
├── fabric-api-<version>.jar
└── invistags-1.1.0-beta.1.jar
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
| `/invis gear true` | Restores last-seen armour and held items on invisible players |
| `/invis gear false` | Stops restoring gear (vanilla behaviour) |
| `/invis vanished true` | Flags entities that are missing from the tab list with `[VANISHED]` |
| `/invis vanished false` | Stops flagging and removes all markers |
| `/invis vanished list` | Lists currently flagged vanished players |
| `/invis remember true` | Restores the last toggles on the next launch |
| `/invis remember false` | Starts with all features disabled on the next launch |

### Keybinds

| Default key | Action |
|---|---|
| `I` | Toggle skin + nametag + gear reveal together |
| `O` | Open the InvisTags config screen |

Both keybinds can be changed in **Options → Controls → Key Binds**.

Features start **disabled** on the first launch. **Remember state** is on by default, so later launches restore the last toggles. Turn it off in the config screen, or run `/invis remember false`, to start disabled every time.

### Gear reveal limits

Vanilla invisibility hides only the player model; armour and held items stay visible. Servers that want invisible players to look completely empty send equipment packets with empty stacks instead.

A client cannot invent gear it has never received. InvisTags therefore remembers the six equipment slots while a player is visible and re-applies the remembered stacks for the render pass while that player is invisible. This works for gear you have seen since joining the server. It cannot recover gear the server never sent, and it cannot see gear swapped while the player stays invisible if the server also strips the update.

### Vanished-player detection

The tab list is compared against the player entities in the current world every tick. A player entity whose UUID is absent from the tab list is flagged. This is the usual signature of a vanish plugin.

- If the tab list is empty, detection is skipped. Otherwise every player on a server that does not use the tab list would be a false positive.
- Server NPCs/fake players are entities with no tab entry and will also be flagged.
- Players outside the client's tracking range are not entities yet and cannot be flagged until they load in.
- The nametag marker is independent of the invisibility nametag toggle; the chat alert is sent once per flag transition.


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
| `PlayerEntityRenderer#updateRenderState` | `AvatarRenderer#extractRenderState` |
| `getEquippedStack` / `equipStack` | `getItemBySlot` / `setItemSlot` |
| `getListedPlayerListEntries` | `getListedOnlinePlayers` |
| `EntityRenderState.displayName` | `EntityRenderState.nameTag` |

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
    │   │   ├── GearReveal.java             — Last-seen gear cache + render-state swap
    │   │   ├── InvistagsConfigScreen.java  — Shared Cloth Config screen
    │   │   ├── ModConfig.java              — Toggle state + JSON persistence
    │   │   ├── ModMenuIntegration.java     — Mod Menu entrypoint
    │   │   ├── VanishDetector.java         — Pure tab-list comparison logic
    │   │   └── VanishedPlayers.java        — Per-tick vanish tracking + chat alerts
    │   └── mixin/client/
    │       ├── EntityRendererMixin.java    — Forces shouldShowName() to true
    │       ├── InvisibilityRenderMixin.java— Restores skin opacity
    │       ├── LabelRenderMixin.java       — Restores nametag opacity
    │       ├── LabelSneakMixin.java        — Fixes sneaking pose
    │       └── PlayerGearMixin.java        — Temporary gear swap + [VANISHED] label
    └── resources/
        ├── assets/invistags/lang/en_us.json — Keybind/config strings
        └── cleaspfabric.client.mixins.json — Client mixin registration

src/gametest/                                — Optional client gametest harness
```

## Development

Run the headless unit tests (config persistence and vanish detection, no Minecraft launch required):

```bash
./gradlew test
```

Run the client gametest (boots a real singleplayer world, applies every mixin, opens the config screen and runs `/invis`):

```bash
./gradlew runClientGameTest
```

Screenshots are written to `build/run/clientGameTest/screenshots/`.

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
