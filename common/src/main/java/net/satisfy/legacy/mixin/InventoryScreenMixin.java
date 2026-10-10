package net.satisfy.legacy.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.satisfy.legacy.client.JournalHolder;
import net.satisfy.legacy.client.JournalPanel;
import net.satisfy.legacy.client.LegacyRenderState;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractContainerScreen<InventoryMenu> implements JournalHolder {
    @Shadow
    public abstract RecipeBookComponent getRecipeBookComponent();

    @Shadow
    private boolean widthTooNarrow;

    @Unique
    private final JournalPanel legacy$journal = new JournalPanel();

    private InventoryScreenMixin(InventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public JournalPanel legacy$journal() {
        return this.legacy$journal;
    }

    @Redirect(method = "init", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;updateScreenPosition(II)I"))
    private int legacy$journalShift(RecipeBookComponent comp, int width, int imageWidth) {
        if (this.legacy$journal.isVisible() && !comp.isVisible() && !this.widthTooNarrow) {
            return JournalPanel.clampHostLeft(177 + (width - imageWidth - 200) / 2, width, imageWidth);
        }
        return comp.updateScreenPosition(width, imageWidth);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void legacy$initJournal(CallbackInfo ci) {
        if (this.legacy$journal.isVisible()) {
            this.addWidget(this.legacy$journal.getOrCreateSearch(this.font));
            this.addWidget(new net.satisfy.legacy.client.JournalScrollListener(this.legacy$journal));
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void legacy$renderJournal(GuiGraphics g, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (this.getRecipeBookComponent().isVisible()) {
            this.legacy$journal.setVisible(false);
        }
        this.legacy$journal.render(g, mouseX, mouseY, partialTick, this.font, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
    }

    @WrapMethod(method = "renderEntityInInventory")
    private static void legacy$suppressPreviewName(GuiGraphics guiGraphics, float x, float y, float scale, Vector3f translate,
                                                   Quaternionf pose, Quaternionf cameraOrientation, LivingEntity entity, Operation<Void> original) {
        LegacyRenderState.renderingInventoryEntity = true;
        try {
            original.call(guiGraphics, x, y, scale, translate, pose, cameraOrientation, entity);
        } finally {
            LegacyRenderState.renderingInventoryEntity = false;
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void legacy$journalClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button == 0 && this.legacy$journal.isOverButton(mouseX, mouseY)) {
            boolean nowVisible = !this.legacy$journal.isVisible();
            this.legacy$journal.setVisible(nowVisible);
            if (nowVisible && this.getRecipeBookComponent().isVisible()) {
                this.getRecipeBookComponent().toggleVisibility();
            }
            this.rebuildWidgets();
            cir.setReturnValue(true);
            return;
        }
        if (this.legacy$journal.clickContent(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }
}
