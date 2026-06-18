package org.codersoft.cleaspfabric.mixin.client;

import net.minecraft.client.render.command.LabelCommandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LabelCommandRenderer.class)
public class LabelRenderMixin {

    @ModifyArg(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)V",
            remap = false
        ),
        index = 3,
        remap = false
    )
    private int fixLabelOpacity(int color) {
        if (color == 0x80FFFFFF) {
            return 0xFFFFFFFF;
        }
        return color;
    }
}
