package net.satisfy.legacy.neoforge.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.satisfy.legacy.client.TitleClientHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @ModifyVariable(method = "renderNameTag", at = @At("HEAD"), argsOnly = true)
    private Component legacy$decorateNameTag(Component name, @Local(argsOnly = true) Entity entity) {
        return TitleClientHandler.decorateName(entity, name);
    }
}
