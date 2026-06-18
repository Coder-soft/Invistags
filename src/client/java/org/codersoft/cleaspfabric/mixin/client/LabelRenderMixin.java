package org.codersoft.cleaspfabric.mixin.client;

import net.minecraft.client.renderer.command.LabelCommandRenderer;
import org.codersoft.cleaspfabric.client.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LabelCommandRenderer.class)
public class LabelRenderMixin {

    @ModifyArg(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Font;draw(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)V",
            remap = false
        ),
        index = 3,
        remap = false
    )
    private int fixLabelOpacity(int color) {
        if (!ModConfig.showInvisibleNametag) return color;
        if (color == 0x80FFFFFF) {
            return 0xFFFFFFFF;
        }
        return color;
    }
}
