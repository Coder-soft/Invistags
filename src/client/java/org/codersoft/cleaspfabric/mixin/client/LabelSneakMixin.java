package org.codersoft.cleaspfabric.mixin.client;

import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.codersoft.cleaspfabric.client.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntityRenderer.class)
public class LabelSneakMixin {

    @Redirect(
        method = "renderLabelIfPresent",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isInvisible()Z")
    )
    private boolean fixSneakingForInvisible(Entity entity) {
        if (ModConfig.showInvisibleNametag && entity instanceof PlayerEntity) {
            return false;
        }
        return entity.isInvisible();
    }
}
