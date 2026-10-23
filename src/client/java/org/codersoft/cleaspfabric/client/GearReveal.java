package org.codersoft.cleaspfabric.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Remembers the last equipment a player was seen wearing and re-applies it for the
 * render-state pass while the player is invisible.
 *
 * <p>Vanilla sends equipment even for invisible players. Servers that want invisible
 * players to look empty send the same packet with empty stacks instead. A client can
 * only show gear it has seen, so the cache is filled while the player is visible and
 * is never cleared by an empty stack received while the player is invisible.
 */
public final class GearReveal {

    private static final EquipmentSlot[] SLOTS = {
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET,
        EquipmentSlot.MAINHAND,
        EquipmentSlot.OFFHAND
    };

    private static final Map<UUID, EnumMap<EquipmentSlot, ItemStack>> REMEMBERED = new HashMap<>();
    private static ClientWorld trackedWorld;

    private GearReveal() {
    }

    public static void tick(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null) {
            clear();
            return;
        }
        if (world != trackedWorld) {
            REMEMBERED.clear();
            trackedWorld = world;
        }
        if (!ModConfig.showInvisibleGear) {
            REMEMBERED.clear();
            return;
        }

        for (PlayerEntity player : world.getPlayers()) {
            if (player == client.player || player.isSpectator()) {
                continue;
            }
            if (player.isInvisible()) {
                mergeNonEmpty(player);
            } else {
                REMEMBERED.put(player.getUuid(), snapshot(player));
            }
        }
    }

    /**
     * Temporarily equips remembered gear into slots the server left empty, so the
     * vanilla render-state extraction fills armour layers and hand item models.
     *
     * @return the stacks that were replaced, to be restored after extraction
     */
    public static Map<EquipmentSlot, ItemStack> equipMissing(PlayerEntity player) {
        EnumMap<EquipmentSlot, ItemStack> remembered = REMEMBERED.get(player.getUuid());
        if (remembered == null || remembered.isEmpty()) {
            return Map.of();
        }

        EnumMap<EquipmentSlot, ItemStack> replaced = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : SLOTS) {
            ItemStack live = player.getEquippedStack(slot);
            ItemStack cached = remembered.get(slot);
            if (live.isEmpty() && cached != null && !cached.isEmpty()) {
                replaced.put(slot, live);
                player.equipStack(slot, cached.copy());
            }
        }
        return replaced;
    }

    public static void restore(PlayerEntity player, Map<EquipmentSlot, ItemStack> replaced) {
        for (Map.Entry<EquipmentSlot, ItemStack> entry : replaced.entrySet()) {
            player.equipStack(entry.getKey(), entry.getValue());
        }
    }

    public static void clear() {
        REMEMBERED.clear();
        trackedWorld = null;
    }

    private static EnumMap<EquipmentSlot, ItemStack> snapshot(PlayerEntity player) {
        EnumMap<EquipmentSlot, ItemStack> snapshot = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : SLOTS) {
            snapshot.put(slot, player.getEquippedStack(slot).copy());
        }
        return snapshot;
    }

    private static void mergeNonEmpty(PlayerEntity player) {
        EnumMap<EquipmentSlot, ItemStack> remembered = REMEMBERED.computeIfAbsent(
            player.getUuid(), id -> new EnumMap<>(EquipmentSlot.class));
        for (EquipmentSlot slot : SLOTS) {
            ItemStack live = player.getEquippedStack(slot);
            if (!live.isEmpty()) {
                remembered.put(slot, live.copy());
            }
        }
    }
}
