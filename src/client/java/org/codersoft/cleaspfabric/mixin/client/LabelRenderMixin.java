package org.codersoft.cleaspfabric.mixin.client;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.codersoft.cleaspfabric.client.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public class LabelRenderMixin<T extends Entity> {

    @Redirect(
        method = "renderLabelIfPresent",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isInvisible()Z")
    )
    private boolean fixLabelOpacity(Entity entity) {
        if (ModConfig.showInvisibleNametag && entity instanceof PlayerEntity) {
            return false;
        }
        return entity.isInvisible();
    }
}
