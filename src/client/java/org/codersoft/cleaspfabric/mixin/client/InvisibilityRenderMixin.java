package org.codersoft.cleaspfabric.mixin.client;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.codersoft.cleaspfabric.client.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class InvisibilityRenderMixin<T extends LivingEntity> {

    @Unique
    private boolean wasInvisible;

    @Inject(
        method = "render",
        at = @At("HEAD")
    )
    private void showSkinForInvisiblePlayers(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        wasInvisible = false;
        if (!ModConfig.showInvisibleSkin) return;
        if (!(livingEntity instanceof PlayerEntity)) return;
        if (livingEntity.isInvisible()) {
            wasInvisible = true;
            livingEntity.setInvisible(false);
        }
    }

    @Inject(
        method = "render",
        at = @At("RETURN")
    )
    private void restoreInvisibility(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (wasInvisible) {
            livingEntity.setInvisible(true);
        }
    }
}
