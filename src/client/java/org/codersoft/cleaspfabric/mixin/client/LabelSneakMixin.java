package org.codersoft.cleaspfabric.mixin.client;

import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class LabelSneakMixin {

    @Inject(
        method = "renderLabelIfPresent",
        at = @At("HEAD")
    )
    private void fixSneakingForInvisible(
        PlayerEntityRenderState state,
        MatrixStack matrices,
        OrderedRenderCommandQueue queue,
        CameraRenderState cameraState,
        CallbackInfo ci
    ) {
        if (state.invisible) {
            state.sneaking = false;
        }
    }
}
