package net.satisfy.legacy.mixin;

import dev.architectury.platform.Platform;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import net.satisfy.legacy.client.JournalHolder;
import net.satisfy.legacy.client.JournalPanel;
import net.satisfy.legacy.client.JournalScrollListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin
        extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> implements JournalHolder {

    @Shadow
    private static CreativeModeTab selectedTab;

    @Unique
    private static final int LEGACY_SHIFT = Platform.isNeoForge() ? 40 : 75;

    @Unique
    private final JournalPanel legacy$journal = new JournalPanel();

    private CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public JournalPanel legacy$journal() {
        return this.legacy$journal;
    }

    @Unique
    private boolean legacy$onInventoryTab() {
        return selectedTab != null && selectedTab.getType() == CreativeModeTab.Type.INVENTORY;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void legacy$positionForJournal(GuiGraphics g, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        boolean onInventory = legacy$onInventoryTab();
        if (!onInventory) {
            this.legacy$journal.setVisible(false);
        }
        int base = (this.width - this.imageWidth) / 2;
        this.leftPos = (onInventory && this.legacy$journal.isVisible())
                ? JournalPanel.clampHostLeft(base + LEGACY_SHIFT, this.width, this.imageWidth) : base;
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
        if (!legacy$onInventoryTab()) {
            return;
        }
        this.legacy$journal.render(g, mouseX, mouseY, partialTick, this.font, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void legacy$journalClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!legacy$onInventoryTab()) {
            return;
        }
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

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void legacy$journalScroll(double mouseX, double mouseY, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        if (legacy$onInventoryTab() && this.legacy$journal.isVisible() && this.legacy$journal.isOverList(mouseX, mouseY)) {
            this.legacy$journal.scroll(scrollY);
            cir.setReturnValue(true);
        }
    }
}
