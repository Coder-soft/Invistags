package org.codersoft.cleaspfabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.codersoft.cleaspfabric.client.GearReveal;
import org.codersoft.cleaspfabric.client.ModConfig;
import org.codersoft.cleaspfabric.client.VanishedPlayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Gives the render-state extraction a temporary view of remembered gear, and marks
 * flagged players' labels.
 */
@Mixin(AvatarRenderer.class)
public class PlayerGearMixin {

    @Unique
    private Map<EquipmentSlot, ItemStack> invistags$replaced = Map.of();

    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
        at = @At("HEAD")
    )
    private void invistags$equipRememberedGear(
        Avatar entity,
        AvatarRenderState state,
        float tickDelta,
        CallbackInfo ci
    ) {
        invistags$replaced = Map.of();
        if (!ModConfig.showInvisibleGear) return;
        if (!(entity instanceof Player player) || !player.isInvisible()) return;
        invistags$replaced = GearReveal.equipMissing(player);
    }

    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
        at = @At("RETURN")
    )
    private void invistags$restoreGearAndMark(
        Avatar entity,
        AvatarRenderState state,
        float tickDelta,
        CallbackInfo ci
    ) {
        if (entity instanceof Player player) {
            if (!invistags$replaced.isEmpty()) {
                GearReveal.restore(player, invistags$replaced);
            }
            if (VanishedPlayers.isFlagged(player) && state.nameTag != null) {
                state.nameTag = state.nameTag.copy().append(VanishedPlayers.marker());
            }
        }
        invistags$replaced = Map.of();
    }
}
