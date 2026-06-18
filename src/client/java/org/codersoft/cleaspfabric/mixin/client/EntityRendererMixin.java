package org.codersoft.cleaspfabric.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.codersoft.cleaspfabric.client.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class EntityRendererMixin<T extends LivingEntity> {

    @Inject(
        method = "hasLabel",
        at = @At("RETURN"),
        cancellable = true
    )
    private void forceShowInvisPlayerLabel(
        T entity,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (!ModConfig.showInvisibleNametag) return;
        if (!(entity instanceof PlayerEntity player)) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        if (player == client.player) return;

        if (player.isInvisible()) {
            cir.setReturnValue(true);
        }
    }
}
