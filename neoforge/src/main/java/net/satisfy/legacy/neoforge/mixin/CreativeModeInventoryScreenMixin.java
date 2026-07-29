package net.satisfy.legacy.neoforge.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.satisfy.legacy.client.JournalHolder;
import net.satisfy.legacy.client.JournalPanel;
import net.satisfy.legacy.client.JournalScrollListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin
        extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> implements JournalHolder {

    @Unique
    private final JournalPanel legacy$journal = new JournalPanel();

    private CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public JournalPanel legacy$journal() {
        return this.legacy$journal;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void legacy$initJournal(CallbackInfo ci) {
        if (this.legacy$journal.isVisible()) {
            this.addWidget(this.legacy$journal.getOrCreateSearch(this.font));
            this.addWidget(new JournalScrollListener(this.legacy$journal));
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void legacy$renderJournal(GuiGraphics g, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        this.legacy$journal.render(g, mouseX, mouseY, partialTick, this.font, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void legacy$journalClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button == 0 && this.legacy$journal.isOverButton(mouseX, mouseY)) {
            this.legacy$journal.setVisible(!this.legacy$journal.isVisible());
            this.rebuildWidgets();
            cir.setReturnValue(true);
            return;
        }
        if (this.legacy$journal.clickContent(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }
}
