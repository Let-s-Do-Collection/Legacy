package net.satisfy.legacy.neoforge.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.satisfy.legacy.client.JournalHolder;
import net.satisfy.legacy.client.JournalPanel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void legacy$journalKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!(this instanceof JournalHolder holder)) {
            return;
        }
        JournalPanel journal = holder.legacy$journal();
        if (!journal.isVisible() || !journal.isSearchFocused()) {
            return;
        }
        if (keyCode == 256) {
            journal.unfocusSearch();
            cir.setReturnValue(true);
            return;
        }
        journal.searchKeyPressed(keyCode, scanCode, modifiers);
        cir.setReturnValue(true);
    }
}
