package net.satisfy.legacy.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.satisfy.legacy.client.JournalHolder;
import net.satisfy.legacy.client.JournalPanel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Handles journal scrolling before other mods (e.g. EMI) can consume the scroll event.
@Environment(EnvType.CLIENT)
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void legacy$scrollJournal(long window, double xOffset, double yOffset, CallbackInfo ci) {
        if (window != this.minecraft.getWindow().getWindow() || !(this.minecraft.screen instanceof JournalHolder holder)) {
            return;
        }
        JournalPanel journal = holder.legacy$journal();
        MouseHandler self = (MouseHandler) (Object) this;
        double mx = self.xpos() * this.minecraft.getWindow().getGuiScaledWidth() / this.minecraft.getWindow().getScreenWidth();
        double my = self.ypos() * this.minecraft.getWindow().getGuiScaledHeight() / this.minecraft.getWindow().getScreenHeight();
        if (journal.isOverList(mx, my)) {
            double amount = (this.minecraft.options.discreteMouseScroll().get() ? Math.signum(yOffset) : yOffset)
                    * this.minecraft.options.mouseWheelSensitivity().get();
            journal.scroll(amount);
            ci.cancel();
        }
    }
}
