package org.codersoft.cleaspfabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.command.OrderedRenderCommandQueue;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.codersoft.cleaspfabric.client.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class InvisibilityRenderMixin {

    @Inject(
        method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/command/OrderedRenderCommandQueue;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
        at = @At("HEAD")
    )
    private void showSkinForInvisiblePlayers(
        LivingEntityRenderState state,
        PoseStack matrices,
        OrderedRenderCommandQueue queue,
        CameraRenderState cameraState,
        CallbackInfo ci
    ) {
        if (!ModConfig.showInvisibleSkin) return;
        if (state instanceof PlayerEntityRenderState && state.invisible) {
            state.invisible = false;
        }
    }
}
