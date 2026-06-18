package org.codersoft.cleaspfabric.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.codersoft.cleaspfabric.client.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class EntityRendererMixin<T extends LivingEntity> {

    @Inject(
        method = "hasLabel(Lnet/minecraft/world/entity/LivingEntity;D)Z",
        at = @At("RETURN"),
        cancellable = true
    )
    private void forceShowInvisPlayerLabel(
        LivingEntity entity,
        double squaredDistanceToCamera,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (!ModConfig.showInvisibleNametag) return;
        if (!(entity instanceof Player player)) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        if (player == client.player) return;

        if (player.isInvisible()) {
            cir.setReturnValue(true);
        }
    }
}
