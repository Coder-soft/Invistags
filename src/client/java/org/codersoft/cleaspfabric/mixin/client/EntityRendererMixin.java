package org.codersoft.cleaspfabric.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.codersoft.cleaspfabric.client.ModConfig;
import org.codersoft.cleaspfabric.client.VanishedPlayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class EntityRendererMixin<T extends LivingEntity> {

    @Inject(
        method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z",
        at = @At("RETURN"),
        cancellable = true
    )
    private void forceShowInvisPlayerLabel(
        LivingEntity entity,
        double squaredDistanceToCamera,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (!ModConfig.showInvisibleNametag && !ModConfig.showInvisibleMobs
            && !ModConfig.flagVanishedPlayers) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        if (entity instanceof Player player) {
            // Vanish-flagged players always get a label, even when the invisibility
            // nametag toggle is off.
            if (VanishedPlayers.isFlagged(player)) {
                cir.setReturnValue(true);
                return;
            }
            if (!ModConfig.showInvisibleNametag) return;
            // Never show your own nametag through this.
            if (player == client.player) return;
            if (player.isInvisible()) {
                cir.setReturnValue(true);
            }
            return;
        }

        // Only custom-named mobs have a meaningful label; forcing vanilla mobs
        // would spam names like "Zombie" for every invisible entity.
        if (ModConfig.showInvisibleMobs && entity.isInvisible() && entity.hasCustomName()) {
            cir.setReturnValue(true);
        }
    }
}
