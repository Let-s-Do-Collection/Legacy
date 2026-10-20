package net.satisfy.legacy.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.satisfy.legacy.client.JournalHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void legacy$journalKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this instanceof JournalHolder holder && holder.legacy$journal().handleSearchKey(keyCode, scanCode, modifiers)) {
            cir.setReturnValue(true);
        }
    }
}
