package org.codersoft.cleaspfabric.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Detects players the server sent as world entities but removed from the tab list
 * (the usual signature of a vanish plugin) and exposes them for nametag marking.
 */
public final class VanishedPlayers {

    private static final Set<UUID> FLAGGED = new HashSet<>();
    private static ClientWorld trackedWorld;

    private VanishedPlayers() {
    }

    public static void tick(MinecraftClient client) {
        if (!ModConfig.flagVanishedPlayers) {
            clear();
            return;
        }

        ClientWorld world = client.world;
        ClientPlayNetworkHandler handler = client.getNetworkHandler();
        if (world == null || handler == null) {
            clear();
            return;
        }
        if (world != trackedWorld) {
            FLAGGED.clear();
            trackedWorld = world;
        }

        Set<UUID> entityIds = new HashSet<>();
        for (PlayerEntity player : world.getPlayers()) {
            entityIds.add(player.getUuid());
        }

        Set<UUID> tabListIds = new HashSet<>();
        for (PlayerListEntry entry : handler.getListedPlayerListEntries()) {
            tabListIds.add(entry.getProfile().id());
        }

        UUID self = client.player == null ? null : client.player.getUuid();
        Set<UUID> vanished = VanishDetector.findVanished(entityIds, tabListIds, self);

        // Keep players flagged while they are out of tracking range so re-entering the
        // area does not produce another chat alert. Forget them once the server lists
        // them again.
        FLAGGED.removeIf(tabListIds::contains);
        Set<UUID> newlyFlagged = new HashSet<>(vanished);
        newlyFlagged.removeAll(FLAGGED);
        FLAGGED.addAll(vanished);

        if (client.player != null) {
            for (PlayerEntity player : world.getPlayers()) {
                if (newlyFlagged.contains(player.getUuid())) {
                    client.player.sendMessage(Text.literal(
                        "§c[InvisTags] §fVanished player flagged: §e"
                            + player.getName().getString()
                            + " §7(no tab entry)"), false);
                }
            }
        }
    }

    public static boolean isFlagged(PlayerEntity player) {
        return ModConfig.flagVanishedPlayers && FLAGGED.contains(player.getUuid());
    }

    /** Names of currently visible flagged players, for the command output. */
    public static List<String> flaggedNames(MinecraftClient client) {
        List<String> names = new ArrayList<>();
        if (client.world == null) {
            return names;
        }
        for (PlayerEntity player : client.world.getPlayers()) {
            if (isFlagged(player)) {
                names.add(player.getName().getString());
            }
        }
        return names;
    }

    /** Red bold suffix appended to a flagged player's nametag. */
    public static Text marker() {
        return Text.literal(" [VANISHED]").formatted(Formatting.RED, Formatting.BOLD);
    }

    public static void clear() {
        FLAGGED.clear();
        trackedWorld = null;
    }
}
