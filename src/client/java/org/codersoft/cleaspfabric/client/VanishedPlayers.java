package org.codersoft.cleaspfabric.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

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
    private static ClientLevel trackedLevel;

    private VanishedPlayers() {
    }

    public static void tick(Minecraft client) {
        if (!ModConfig.flagVanishedPlayers) {
            clear();
            return;
        }

        ClientLevel level = client.level;
        ClientPacketListener connection = client.getConnection();
        if (level == null || connection == null) {
            clear();
            return;
        }
        if (level != trackedLevel) {
            FLAGGED.clear();
            trackedLevel = level;
        }

        Set<UUID> entityIds = new HashSet<>();
        for (Player player : level.players()) {
            entityIds.add(player.getUUID());
        }

        Set<UUID> tabListIds = new HashSet<>();
        for (PlayerInfo entry : connection.getListedOnlinePlayers()) {
            tabListIds.add(entry.getProfile().id());
        }

        UUID self = client.player == null ? null : client.player.getUUID();
        Set<UUID> vanished = VanishDetector.findVanished(entityIds, tabListIds, self);

        // Keep players flagged while they are out of tracking range so re-entering the
        // area does not produce another chat alert. Forget them once the server lists
        // them again.
        FLAGGED.removeIf(tabListIds::contains);
        Set<UUID> newlyFlagged = new HashSet<>(vanished);
        newlyFlagged.removeAll(FLAGGED);
        FLAGGED.addAll(vanished);

        if (client.player != null) {
            for (Player player : level.players()) {
                if (newlyFlagged.contains(player.getUUID())) {
                    client.player.sendSystemMessage(Component.literal(
                        "§c[InvisTags] §fVanished player flagged: §e"
                            + player.getName().getString()
                            + " §7(no tab entry)"));
                }
            }
        }
    }

    public static boolean isFlagged(Player player) {
        return ModConfig.flagVanishedPlayers && FLAGGED.contains(player.getUUID());
    }

    /** Names of currently visible flagged players, for the command output. */
    public static List<String> flaggedNames(Minecraft client) {
        List<String> names = new ArrayList<>();
        if (client.level == null) {
            return names;
        }
        for (Player player : client.level.players()) {
            if (isFlagged(player)) {
                names.add(player.getName().getString());
            }
        }
        return names;
    }

    /** Red bold suffix appended to a flagged player's nametag. */
    public static Component marker() {
        return Component.literal(" [VANISHED]")
            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
    }

    public static void clear() {
        FLAGGED.clear();
        trackedLevel = null;
    }
}
