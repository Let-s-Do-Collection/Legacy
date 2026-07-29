package net.satisfy.legacy.fabric.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.satisfy.legacy.client.LegacyClientConfig;
import net.satisfy.legacy.client.LegacyRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void legacy$alwaysShowOwnName(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (LegacyClientConfig.showOwnName && !entity.isCrouching() && !LegacyRenderState.renderingInventoryEntity
                && entity == mc.player && !mc.options.getCameraType().isFirstPerson() && !mc.options.hideGui) {
            cir.setReturnValue(true);
        }
    }
}
