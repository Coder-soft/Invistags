# InvisNametag — Fabric 1.21.1 Mod Development Plan

A step-by-step guide to building a client-side Fabric mod that renders nametags of invisible players at 100% opacity, at any render distance, regardless of what they are doing.

---

## Table of Contents

1. [Prerequisites & Tooling](#1-prerequisites--tooling)
2. [Project Scaffolding](#2-project-scaffolding)
3. [Project Structure Overview](#3-project-structure-overview)
4. [fabric.mod.json](#4-fabricmodjson)
5. [build.gradle](#5-buildgradle)
6. [Main Mod Entrypoint](#6-main-mod-entrypoint)
7. [How Minecraft Renders Nametags (Theory)](#7-how-minecraft-renders-nametags-theory)
8. [Mixin 1 — Force `hasLabel()` for Invisible Players](#8-mixin-1--force-haslabel-for-invisible-players)
9. [Mixin 2 — Force 100% Opacity on the Nametag](#9-mixin-2--force-100-opacity-on-the-nametag)
10. [Registering Mixins](#10-registering-mixins)
11. [Building & Installing](#11-building--installing)
12. [Testing Checklist](#12-testing-checklist)
13. [Troubleshooting](#13-troubleshooting)
14. [References & Sources](#14-references--sources)

---

## 1. Prerequisites & Tooling

### What You Need

| Tool | Version | Download |
|------|---------|----------|
| JDK (Java Development Kit) | 21 | https://adoptium.net |
| IntelliJ IDEA Community | Latest | https://www.jetbrains.com/idea |
| Fabric Loader | 0.15.x+ | Auto-pulled by Gradle |
| Fabric API | Latest for 1.21.1 | https://modrinth.com/mod/fabric-api |

> **Why Java 21?** Minecraft 1.21.x requires Java 21. Older JDKs will fail to compile.

### Verify Your JDK

```bash
java -version
# Expected output:
# openjdk version "21.0.x" ...
```

---

## 2. Project Scaffolding

### Use the Official Fabric Template Generator

Go to: **https://fabricmc.net/develop**

Fill in these fields exactly:

| Field | Value |
|-------|-------|
| Minecraft Version | `1.21.1` |
| Project Name | `InvisNametag` |
| Package Name | `com.yourname.invisnametag` |
| Use Kotlin? | No |

Click **Download Template** and unzip the folder.

Then open the folder in IntelliJ:

```
File → Open → select the unzipped folder → Trust Project
```

Wait for Gradle to sync (this downloads ~500 MB of Minecraft assets on first run).

---

## 3. Project Structure Overview

After scaffolding, your project should look like this:

```
invisnametag/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── src/
│   └── main/
│       ├── java/
│       │   └── com/yourname/invisnametag/
│       │       ├── InvisNametagMod.java          ← Entrypoint
│       │       └── mixin/
│       │           ├── EntityRendererMixin.java  ← hasLabel() hook
│       │           └── LabelOpacityMixin.java    ← Opacity fix
│       └── resources/
│           ├── fabric.mod.json                   ← Mod metadata
│           └── invisnametag.mixins.json          ← Mixin registry
```

---

## 4. `fabric.mod.json`

This file tells Fabric Loader everything about your mod: its ID, name, entrypoint class, and what environment it runs in.

**Location:** `src/main/resources/fabric.mod.json`

```json
{
  "schemaVersion": 1,
  "id": "invisnametag",
  "version": "1.0.0",
  "name": "InvisNametag",
  "description": "Always shows nametags of invisible players at full opacity.",
  "authors": ["yourname"],
  "environment": "client",
  "entrypoints": {
    "client": [
      "com.yourname.invisnametag.InvisNametagMod"
    ]
  },
  "mixins": [
    "invisnametag.mixins.json"
  ],
  "depends": {
    "fabricloader": ">=0.15.0",
    "minecraft": "~1.21.1",
    "java": ">=21",
    "fabric-api": "*"
  }
}
```

### Key Fields Explained

- `"environment": "client"` — This mod only runs on the client. It will be ignored by servers, making it safe to use on any server without getting kicked.
- `"entrypoints"` → `"client"` — Points to the class that Fabric calls when the game initialises on the client side.
- `"mixins"` — Points to your mixin config file where all mixin classes are registered.

> **Source:** [Fabric Wiki — fabric.mod.json](https://wiki.fabricmc.net/documentation:fabric_mod_json)

---

## 5. `build.gradle`

The template generates this for you. The key sections to verify:

```groovy
// build.gradle (relevant excerpts)

dependencies {
    minecraft "com.mojang:minecraft:1.21.1"
    mappings "net.fabricmc:yarn:1.21.1+build.3:v2"   // yarn mappings
    modImplementation "net.fabricmc:fabric-loader:0.15.11"
    modImplementation "net.fabricmc.fabric-api:fabric-api:0.102.0+1.21.1"
}
```

> **Why Yarn mappings?** Yarn provides human-readable names for Minecraft's obfuscated classes and methods (like `EntityRenderer`, `hasLabel`, etc.). Without mappings, everything would be named `a`, `b`, `c`.

> **Source:** [Fabric Wiki — Setting up a mod development environment](https://wiki.fabricmc.net/tutorial:setup)

---

## 6. Main Mod Entrypoint

This class is the entry point Fabric calls when your mod loads on the client. Since all logic lives in mixins, this file is intentionally minimal.

**Location:** `src/main/java/com/yourname/invisnametag/InvisNametagMod.java`

```java
package com.yourname.invisnametag;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvisNametagMod implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("invisnametag");

    @Override
    public void onInitializeClient() {
        LOGGER.info("InvisNametag loaded. Invisible players can no longer hide.");
    }
}
```

### Why `ClientModInitializer` and not `ModInitializer`?

`ModInitializer` runs on both client and server. Since this mod only affects rendering (which only exists on the client), use `ClientModInitializer` to avoid crashes on a dedicated server.

> **Source:** [Fabric Wiki — Entrypoints](https://wiki.fabricmc.net/tutorial:entrypoints)

---

## 7. How Minecraft Renders Nametags (Theory)

Understanding the rendering pipeline is critical before writing mixins. Here's what vanilla Minecraft does:

### The Call Chain

```
GameRenderer.render()
  └─ WorldRenderer.render()
       └─ EntityRenderDispatcher.render(entity, ...)
            └─ EntityRenderer.render(entity, ...)
                 └─ EntityRenderer.renderLabelIfPresent(entity, ...)  ← checks hasLabel()
                      └─ EntityRenderer.renderLabel(...)              ← draws the text
```

### `hasLabel()` — The Gatekeeper

```java
// Vanilla source (EntityRenderer.java, simplified)
protected boolean hasLabel(T entity, double squaredDistanceToCamera) {
    PlayerEntity localPlayer = MinecraftClient.getInstance().player;
    
    boolean isVisible = !entity.isInvisibleTo(localPlayer);
    // ^ Returns true only if YOU are on the same team as the invisible player
    
    if (entity == localPlayer) {
        return this.shouldRenderLabel(entity);
    }
    
    // Key: invisible entities bypass the label entirely unless on your team
    if (entity.isInvisible()) return false;  // ← THIS is what we override
    
    return squaredDistanceToCamera < ... && this.shouldRenderLabel(entity);
}
```

**Problem:** If `isInvisible()` is true and you're not on their team, `hasLabel()` returns `false` and `renderLabelIfPresent()` is never called — so no nametag appears at all.

**Our fix:** Inject at the `RETURN` point of `hasLabel()` and force it to return `true` when the entity is an invisible player.

### Opacity — The Second Problem

Even if `hasLabel()` returns true, vanilla reduces the background alpha of nametags for invisible entities:

```java
// Vanilla LivingEntityRenderer.java (simplified)
protected void renderLabelIfPresent(...) {
    int backgroundColor = entity.isInvisible()
        ? 0x20FFFFFF   // ~12% opacity background  ← this looks almost invisible
        : 0x4C000000;  // ~30% opacity background (normal sneaking)
    
    this.renderLabel(entity, text, matrices, vertexConsumers, light);
}
```

**Our fix:** Use `@ModifyArg` to intercept the background color argument and replace its alpha byte with `0xFF` (255 = fully opaque).

---

## 8. Mixin 1 — Force `hasLabel()` for Invisible Players

This is the most important mixin. It intercepts the return value of `EntityRenderer#hasLabel` and overrides it to `true` for any invisible player that isn't you.

**Location:** `src/main/java/com/yourname/invisnametag/mixin/EntityRendererMixin.java`

```java
package com.yourname.invisnametag.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity> {

    @Inject(
        method = "hasLabel(Lnet/minecraft/entity/Entity;D)Z",
        at = @At("RETURN"),
        cancellable = true
    )
    private void forceShowInvisPlayerLabel(
        T entity,
        double squaredDistanceToCamera,
        CallbackInfoReturnable<Boolean> cir
    ) {
        // Only care about players
        if (!(entity instanceof PlayerEntity player)) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Never show your own nametag through this (would be weird)
        if (player == client.player) return;

        // The one and only condition: is this player invisible?
        // Doesn't matter if they're jumping, sprinting, swimming,
        // drowning, sneaking, flying — if invisible, show their tag.
        if (player.isInvisible()) {
            cir.setReturnValue(true);
        }
    }
}
```

### Annotation Breakdown

| Annotation | Purpose |
|-----------|---------|
| `@Mixin(EntityRenderer.class)` | Targets the vanilla `EntityRenderer` class |
| `@Inject(method = "hasLabel(...)")` | Hooks into the `hasLabel` method |
| `at = @At("RETURN")` | Runs our code just before the method returns |
| `cancellable = true` | Allows us to override the return value |
| `cir.setReturnValue(true)` | Forces the method to return `true` |

### Why `@At("RETURN")` and not `@At("HEAD")`?

Using `HEAD` would run before vanilla logic populates anything. Using `RETURN` lets vanilla run its checks first, then we override the result only for our specific case (invisible players). This is safer and less likely to conflict with other mods.

> **Source:** [Mixin Wiki — Injectors](https://github.com/SpongePowered/Mixin/wiki/Introduction-to-Mixins---The-Mixin-Environment)

---

## 9. Mixin 2 — Force 100% Opacity on the Nametag

Even with the label showing, vanilla will render it nearly transparent for invisible entities. This mixin fixes the alpha channel of the nametag background color.

**Location:** `src/main/java/com/yourname/invisnametag/mixin/LabelOpacityMixin.java`

```java
package com.yourname.invisnametag.mixin;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EntityRenderer.class)
public class LabelOpacityMixin<T extends Entity> {

    @ModifyArg(
        method = "renderLabel",
        at = @At(
            value = "INVOKE",
            // Targets the drawWithOutline call inside renderLabel
            target = "Lnet/minecraft/client/font/TextRenderer;drawWithOutline(" +
                     "Lnet/minecraft/text/OrderedText;" +
                     "FFIILorg/joml/Matrix4f;" +
                     "Lnet/minecraft/client/render/VertexConsumerProvider;I)I"
        ),
        index = 4  // The 'backgroundColor' argument (0-indexed)
    )
    private int forceFullOpacityBackground(int backgroundColor) {
        // ARGB format: 0xAARRGGBB
        // We preserve the RGB color but force alpha to 0xFF (255 = 100% opaque)
        return (backgroundColor & 0x00FFFFFF) | 0xFF000000;
    }
}
```

### How the Alpha Byte Works

```
Color format: 0xAARRGGBB
               ^^
               Alpha channel (00 = invisible, FF = fully opaque)

Vanilla for invisible entities: 0x20FFFFFF  →  alpha = 0x20 = 32  (~12% opacity)
Our override:                   0xFFFFFFFF  →  alpha = 0xFF = 255 (100% opacity)

Bit operation breakdown:
  backgroundColor & 0x00FFFFFF  → strips the alpha byte, keeps RGB
  | 0xFF000000                  → sets alpha to 0xFF
```

### Why `@ModifyArg` and not `@Inject`?

`@ModifyArg` is designed specifically to modify a single argument of a method call inside the target method. It's cleaner and more precise than injecting and manually patching the stack. This is the recommended approach in the Mixin library for this use case.

> **Source:** [Mixin Wiki — @ModifyArg](https://github.com/SpongePowered/Mixin/wiki/Advanced-Mixin-Usage---Callback-Injectors#modifyarg)

---

## 10. Registering Mixins

All mixins must be declared in a mixin config JSON file, otherwise they will be silently ignored.

**Location:** `src/main/resources/invisnametag.mixins.json`

```json
{
  "required": true,
  "minVersion": "0.8",
  "package": "com.yourname.invisnametag.mixin",
  "compatibilityLevel": "JAVA_21",
  "client": [
    "EntityRendererMixin",
    "LabelOpacityMixin"
  ],
  "injectors": {
    "defaultRequire": 1
  }
}
```

### Field Explanations

| Field | Meaning |
|-------|---------|
| `"required": true` | The game will crash on startup if this mixin file can't be found — prevents silent failures |
| `"package"` | The Java package where all mixin classes live |
| `"compatibilityLevel": "JAVA_21"` | Tells Mixin what Java version to target |
| `"client": [...]` | Mixins listed here only apply to the client environment |
| `"defaultRequire": 1` | Every listed mixin must successfully inject at least once or throw an error |

> This file must also be referenced in `fabric.mod.json` under the `"mixins"` key (already done in Step 4).

---

## 11. Building & Installing

### Build the Mod JAR

Open a terminal in your project root and run:

```bash
# On Windows
gradlew.bat build

# On Linux / macOS
./gradlew build
```

If successful, you'll see:

```
BUILD SUCCESSFUL in 12s
```

Your compiled mod JAR will be at:

```
build/libs/invisnametag-1.0.0.jar
```

> Ignore the `-sources.jar` file in that folder — that's just source code, not the mod.

### Install the Mod

1. Open your Minecraft launcher and locate your `.minecraft` folder:
   - **Windows:** `%appdata%\.minecraft`
   - **Linux:** `~/.minecraft`
   - **macOS:** `~/Library/Application Support/minecraft`

2. Copy the JAR to `.minecraft/mods/`:
   ```
   .minecraft/
   └── mods/
       ├── fabric-api-0.102.0+1.21.1.jar   ← required dependency
       └── invisnametag-1.0.0.jar           ← your mod
   ```

3. Make sure your Fabric Loader profile is selected in the launcher, then launch the game.

4. Check the in-game mod list (if you have Mod Menu installed) or look for the startup log line:
   ```
   [InvisNametag] InvisNametag loaded. Invisible players can no longer hide.
   ```

---

## 12. Testing Checklist

To verify everything works, test these scenarios on a local LAN world or singleplayer with cheats:

```
Give yourself invisibility:
  /effect give @s minecraft:invisibility 9999 0 true

Then switch to spectator and back, or use a second account / alt.
```

| Scenario | Expected Result |
|----------|----------------|
| Invisible player standing still | Nametag visible at full opacity ✓ |
| Invisible player walking | Nametag visible ✓ |
| Invisible player sprinting | Nametag visible ✓ |
| Invisible player sneaking | Nametag visible ✓ |
| Invisible player jumping | Nametag visible ✓ |
| Invisible player swimming | Nametag visible ✓ |
| Invisible player underwater (drowning) | Nametag visible ✓ |
| Invisible player in a boat | Nametag visible ✓ |
| Non-invisible player | Nametag renders normally (unaffected) ✓ |
| Your own player (invisible) | Your own nametag not shown ✓ |
| Player at edge of render distance | Nametag visible (vanilla distance rules apply) ✓ |

---

## 13. Troubleshooting

### `ClassNotFoundException` on startup

**Cause:** Mixin class name in `invisnametag.mixins.json` doesn't match the actual Java class filename.

**Fix:** Make sure every entry in `"client": [...]` exactly matches the `.java` filename (without the `.java` extension).

---

### `@Inject` fails with "No candidates found"

**Cause:** The method descriptor in `method = "hasLabel(...)"` is wrong for 1.21.1.

**Fix:** Use the full descriptor:
```
method = "hasLabel(Lnet/minecraft/entity/Entity;D)Z"
```

You can verify method descriptors by decompiling the Minecraft JAR with [Recaf](https://github.com/Col-E/Recaf) or browsing Yarn mappings at https://mappings.fabricmc.net.

---

### Nametag shows but is still semi-transparent

**Cause:** `LabelOpacityMixin` isn't injecting correctly — the `target` descriptor for `drawWithOutline` may have changed.

**Fix:** Open the decompiled `EntityRenderer` class and look for the exact signature of `drawWithOutline` being called inside `renderLabel`. Update the `target` string to match.

---

### Mod works in singleplayer but not on a server

**Cause:** The server is running an anti-cheat or has `invisibility` visibility rules enforced server-side.

**Explanation:** This mod only affects the *rendering* step. If the server never sends the player entity to your client (which some anti-cheats do for invisible players), there's nothing to render a nametag on. This is a server-side limitation this mod cannot bypass.

---

### Conflict with other mods (e.g. Bobby, Sodium)

Sodium and Bobby don't touch nametag rendering, so conflicts are unlikely. If you see issues, add this to your `gradle.properties` to enable Mixin debug output:

```properties
mixin.debug.export=true
```

Then check `.minecraft/mixin.out/` to see the patched class bytecode.

---

## 14. References & Sources

| Resource | URL |
|----------|-----|
| Fabric Wiki — Getting Started | https://wiki.fabricmc.net/tutorial:getting_started |
| Fabric Wiki — fabric.mod.json spec | https://wiki.fabricmc.net/documentation:fabric_mod_json |
| Fabric Wiki — Entrypoints | https://wiki.fabricmc.net/tutorial:entrypoints |
| Fabric Wiki — Introduction to Mixins | https://wiki.fabricmc.net/tutorial:mixin_introduction |
| SpongePowered Mixin Wiki | https://github.com/SpongePowered/Mixin/wiki |
| Mixin — @Inject docs | https://github.com/SpongePowered/Mixin/wiki/Introduction-to-Mixins---The-Mixin-Environment |
| Mixin — @ModifyArg docs | https://github.com/SpongePowered/Mixin/wiki/Advanced-Mixin-Usage---Callback-Injectors |
| Yarn Mappings Browser | https://mappings.fabricmc.net |
| Recaf (bytecode viewer) | https://github.com/Col-E/Recaf |
| Fabric API (Modrinth) | https://modrinth.com/mod/fabric-api |
| Adoptium JDK 21 | https://adoptium.net |
| Fabric Template Generator | https://fabricmc.net/develop |

---

*Generated for Fabric 1.21.1 · Yarn Mappings · Java 21 · Mixin 0.8+*
