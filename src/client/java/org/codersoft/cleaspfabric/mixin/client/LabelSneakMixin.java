package org.codersoft.cleaspfabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.command.OrderedRenderCommandQueue;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.codersoft.cleaspfabric.client.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class LabelSneakMixin {

    @Inject(
        method = "renderLabelIfPresent",
        at = @At("HEAD")
    )
    private void fixSneakingForInvisible(
        PlayerEntityRenderState state,
        PoseStack matrices,
        OrderedRenderCommandQueue queue,
        CameraRenderState cameraState,
        CallbackInfo ci
    ) {
        if (!ModConfig.showInvisibleNametag) return;
        if (state.invisible) {
            state.sneaking = false;
        }
    }
}
