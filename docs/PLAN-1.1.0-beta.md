# InvisTags 1.1.0-beta — gear reveal + vanished-player flag

Working notes for the beta that adds two optional toggles on top of the existing
invisibility reveal. Applies to `main` (1.21.11) and the `26.1`, `26`, `26.3`
branches.

## Goals

1. **Gear reveal (`showInvisibleGear`)** — show armour and held items on invisible
   players even when the server removes them from equipment packets.
2. **Vanished-player flag (`flagVanishedPlayers`)** — flag players the server sent
   as world entities but removed from the tab list (vanish plugins used by staff).
3. Both are independent, runtime toggles with config-file persistence, Cloth Config
   entries, and `/invis` subcommands.

## Primary-source findings

### Invisibility and gear

- Minecraft Wiki, [Invisibility](https://minecraft.wiki/w/Invisibility): the effect
  "turns players and mobs completely invisible ... but has no effect on held items,
  worn armor, or certain parts of some mobs." Vanilla therefore *renders* armour and
  held items for invisible players; only the base model disappears.
- Consequence: InvisTags already shows gear whenever the client has equipment data.
  Servers that want invisible players to look empty send
  `ClientboundSetEquipmentPacket` with empty stacks. No client can invent equipment
  it has never received, so the only correct client-side counter is to remember the
  last gear the client actually saw and re-use it while the player is invisible.

### Vanished staff and the tab list

- The tab list is filled by `ClientboundPlayerInfoUpdate` (`UPDATE_LISTED`) and
  cleared by `ClientboundPlayerInfoRemove`. Vanish plugins keep the player entity in
  the world but stop listing them, or remove their info entry entirely.
- 1.21.11: `ClientPlayNetworkHandler#getListedPlayerListEntries()` is the tab list;
  `getPlayerList()` is every tracked entry.
- 26.x: `ClientPacketListener#getListedOnlinePlayers()` / `getOnlinePlayers()`.
- Guard: if the tab list is empty, the server does not use it (common on some
  servers and proxies). Flagging every entity there would be a false positive, so
  detection is skipped until at least one player is listed.

### API signatures checked against the mapped jars

Checked with `javap` against the Yarn-mapped 1.21.11 client jar and the Mojang-mapped
26.3 client jar (the same Loom caches the build uses):

| Concern | 1.21.11 | 26.x |
|---|---|---|
| Player type | `PlayerEntity` | `Player` |
| Body visibility gate | `LivingEntityRenderer#isVisible` = `!state.invisible` | `#isBodyVisible` |
| Player render state | `PlayerEntityRenderState` | `AvatarRenderState` |
| State extraction | `PlayerEntityRenderer#updateRenderState(PlayerLikeEntity, PlayerEntityRenderState, float)` | `AvatarRenderer#extractRenderState(Avatar, AvatarRenderState, float)` |
| Label gate | `LivingEntityRenderer#hasLabel` | `#shouldShowName` |
| Label text | `EntityRenderState.displayName` | `EntityRenderState.nameTag` |
| Equipment read/write | `getEquippedStack` / `equipStack` | `getItemBySlot` / `setItemSlot` |
| Tab list | `getListedPlayerListEntries()` | `getListedOnlinePlayers()` |

Armour and held-item renderers read the render state, not the entity, and the state
is filled from `LivingEntity#getEquippedStack` on every frame
(`BipedEntityRenderer#updateBipedRenderState`), which is why the gear cache can be
applied for one extraction pass.

### Fabric / Mixin docs

- Fabric docs (1.21.11), [Key Mappings](https://docs.fabricmc.net/1.21.11/develop/key-mappings)
  and Events: per-tick work belongs in `ClientTickEvents.END_CLIENT_TICK`.
- Mixin: `@Inject(at = @At("HEAD"/"RETURN"))` is used for the temporary gear swap;
  no overwrite or argument surgery is required.
- Cross-version API names were taken from the actual mapped jars rather than memory,
  because the 26.x line moved to Mojang mappings and renamed most render types.

## Design

### Gear cache and one-frame equipment swap

`GearReveal` (client, per version) keeps
`Map<UUID, EnumMap<EquipmentSlot, ItemStack>>` for the six humanoid slots
(head, chest, legs, feet, main hand, off hand).

- Every client tick, when the toggle is on:
  - visible player → snapshot all six slots, **including empties**, so removed armour
    is forgotten;
  - invisible player → merge any non-empty stack the server still sent, but never
    clear a slot on an empty packet.
- Cache is cleared on world/dimension change and when the toggle is off.
- During render-state extraction for an invisible player, temporarily equip cached
  items into slots the server left empty, let vanilla fill the render state (armour
  layers, item models, glint, trims), then restore the real stacks. This keeps the
  entity untouched outside the extraction and makes toggle-off instant.

Limitations (documented in README):

- Gear the client has never received cannot be reconstructed.
- Gear changed while the player stays invisible and the server strips it is not seen.
- If the server removed gear on the same tick invisibility started, the cache may
  hold the last visible snapshot (usually the correct one).

### Vanished detection

`VanishDetector` is pure Java (UUID sets) so it is unit-testable:
`findVanished(entityIds, tabListIds, self)` returns entity IDs not in the tab list,
or an empty set when the tab list is empty.

`VanishedPlayers` runs it each client tick, keeps `Set<UUID>` plus an entity-id set
for rendering, and sends one chat alert per newly flagged player. Flagged players
get `hasLabel`/`shouldShowName` forced true and a red bold `[VANISHED]` suffix on the
name, independent of the invisibility nametag toggle.

Limitations:

- Server NPCs/fake players are entities with no tab entry and will be flagged too.
- Servers that never populate the tab list are skipped by design.
- Players outside the client's tracking range are not entities on the client and
  cannot be flagged until they enter it.

## Files

Per branch:

- `ModConfig` — two new booleans + load/save.
- `GearReveal` — new, MC-specific cache/tick/frame swap.
- `VanishDetector` — new, pure Java.
- `VanishedPlayers` — new, detection, chat alert, label helpers.
- `PlayerGearMixin` (1.21.11) / `AvatarGearMixin` (26.x) — temp equip + marker.
- `EntityRendererMixin` — force label for flagged players.
- `CleaspfabricClient` — tick wiring, `/invis gear`, `/invis vanished`, toggle key.
- `InvistagsConfigScreen`, `en_us.json`, `README.md`.
- `ModConfigTest`, new `VanishDetectorTest`.

## Verification

- `./gradlew test` — config persistence and vanish-detector cases.
- `./gradlew build` — main.
- Same implementation ported and built on `26.1`, `26`, `26.3`.
- Optional client gametest smoke run (world load + toggles) if the harness behaves
  on this Mac; the published build is still labelled **beta / untested**.
- Release: GitHub prerelease with all four `invistags-mc<version>-*.jar` artifacts.
