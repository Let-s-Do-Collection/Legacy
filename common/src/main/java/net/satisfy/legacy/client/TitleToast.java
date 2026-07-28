package net.satisfy.legacy.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class TitleToast implements Toast {
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("toast/advancement");
    private static final long DISPLAY_TIME = 5000L;

    private final Component header;
    private final Component titleName;
    private final ItemStack icon;

    public TitleToast(Component titleName, ItemStack icon) {
        this.header = Component.translatable("toast.legacy.title_unlocked").withStyle(ChatFormatting.YELLOW);
        this.titleName = titleName;
        this.icon = icon;
    }

    @Override
    public Toast.Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long timeSinceLastVisible) {
        guiGraphics.blitSprite(BACKGROUND_SPRITE, 0, 0, this.width(), this.height());

        Font font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, this.header, 30, 7, 0xFFFF00, false);
        guiGraphics.drawString(font, this.titleName, 30, 18, 0xFFFFFFFF, false);

        guiGraphics.renderFakeItem(this.icon, 8, 8);

        return timeSinceLastVisible >= DISPLAY_TIME ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }
}
