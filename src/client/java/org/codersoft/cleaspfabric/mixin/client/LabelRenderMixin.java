package org.codersoft.cleaspfabric.mixin.client;

import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.codersoft.cleaspfabric.client.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public class LabelRenderMixin {

    private static final ThreadLocal<Boolean> savedInvisible = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> savedInvisibleToPlayer = new ThreadLocal<>();

    @Inject(
        method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At("HEAD")
    )
    private void forceFullOpacityForInvisible(
        AvatarRenderState state,
        com.mojang.blaze3d.vertex.PoseStack matrices,
        net.minecraft.client.renderer.SubmitNodeCollector collector,
        net.minecraft.client.renderer.state.level.CameraRenderState cameraState,
        CallbackInfo ci
    ) {
        if (!ModConfig.showInvisibleNametag) return;
        if (!state.isInvisibleToPlayer && !state.isInvisible) return;

        savedInvisible.set(state.isInvisible);
        savedInvisibleToPlayer.set(state.isInvisibleToPlayer);
        state.isInvisible = false;
        state.isInvisibleToPlayer = false;
    }

    @Inject(
        method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At("RETURN")
    )
    private void restoreInvisible(
        AvatarRenderState state,
        com.mojang.blaze3d.vertex.PoseStack matrices,
        net.minecraft.client.renderer.SubmitNodeCollector collector,
        net.minecraft.client.renderer.state.level.CameraRenderState cameraState,
        CallbackInfo ci
    ) {
        Boolean prev = savedInvisible.get();
        Boolean prevToPlayer = savedInvisibleToPlayer.get();
        if (prev != null) {
            state.isInvisible = prev;
        }
        if (prevToPlayer != null) {
            state.isInvisibleToPlayer = prevToPlayer;
        }
        savedInvisible.remove();
        savedInvisibleToPlayer.remove();
    }
}
