package org.codersoft.cleaspfabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.codersoft.cleaspfabric.client.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class InvisibilityRenderMixin {

    @Inject(
        method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At("HEAD")
    )
    private void showSkinForInvisibleEntities(
        LivingEntityRenderState state,
        PoseStack matrices,
        SubmitNodeCollector collector,
        CameraRenderState cameraState,
        CallbackInfo ci
    ) {
        if (!state.isInvisible) return;

        if (state instanceof AvatarRenderState) {
            if (ModConfig.showInvisibleSkin) {
                state.isInvisibleToPlayer = false;
                state.isInvisible = false;
            }
        } else if (ModConfig.showInvisibleMobs) {
            state.isInvisibleToPlayer = false;
            state.isInvisible = false;
        }
    }
}
