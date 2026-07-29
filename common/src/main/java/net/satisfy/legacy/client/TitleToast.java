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
    public static final ResourceLocation TITLE_BG = ResourceLocation.fromNamespaceAndPath("legacy", "toast/background");
    public static final ResourceLocation MILESTONE_BG = ResourceLocation.fromNamespaceAndPath("legacy", "toast/milestone");
    private static final long DISPLAY_TIME = 5000L;

    private final ResourceLocation background;
    private final Component header;
    private final Component titleName;
    private final Component subtitle;
    private final ItemStack icon;

    public TitleToast(Component titleName, ItemStack icon) {
        this(TITLE_BG, Component.translatable("toast.legacy.title_unlocked").withStyle(ChatFormatting.YELLOW), titleName, null, icon);
    }

    public TitleToast(ResourceLocation background, Component header, Component titleName, Component subtitle, ItemStack icon) {
        this.background = background;
        this.header = header;
        this.titleName = titleName;
        this.subtitle = subtitle;
        this.icon = icon;
    }

    @Override
    public int width() {
        Font font = Minecraft.getInstance().font;
        int text = font.width(this.header);
        if (this.subtitle != null) {
            text = Math.max(text, font.width(this.subtitle));
        }
        text = Math.max(text, font.width(this.titleName));
        return Math.max(160, 30 + text + 8);
    }

    @Override
    public Toast.Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long timeSinceLastVisible) {
        guiGraphics.blitSprite(this.background, 0, 0, this.width(), this.height());

        Font font = Minecraft.getInstance().font;
        if (this.subtitle == null) {
            guiGraphics.drawString(font, this.header, 30, 7, 0xFFFF00, false);
            guiGraphics.drawString(font, this.titleName, 30, 18, 0xFFFFFFFF, false);
        } else {
            guiGraphics.drawString(font, this.header, 30, 3, 0xFFFF00, false);
            guiGraphics.drawString(font, this.subtitle, 30, 12, 0xFFFFFFFF, false);
            guiGraphics.drawString(font, this.titleName, 30, 21, 0xFFFFFFFF, false);
        }

        guiGraphics.renderFakeItem(this.icon, 8, 8);

        return timeSinceLastVisible >= DISPLAY_TIME ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }
}
