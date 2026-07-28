package net.satisfy.legacy.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.satisfy.legacy.core.title.Placement;
import net.satisfy.legacy.core.title.Title;
import net.satisfy.legacy.core.title.TitleManager;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class TitleClientHandler {
    private TitleClientHandler() {
    }

    public static Component decorateName(Entity entity, Component original) {
        if (!(entity instanceof Player player)) {
            return original;
        }

        String activeId = ClientTitleData.getActiveTitle(player.getUUID());
        if (activeId == null || activeId.isEmpty()) {
            return original;
        }

        Optional<Title> maybeTitle = TitleManager.INSTANCE.get(activeId);
        if (maybeTitle.isEmpty()) {
            return original;
        }

        Title title = maybeTitle.get();
        ChatFormatting color = title.getRarity().getColor();

        MutableComponent nameComponent = original.copy().withStyle(color);
        MutableComponent titleComponent = TitleNames.styled(title, ClientTitleData.getForm(player.getUUID()));

        if (title.getPlacement() == Placement.PREFIX) {
            return Component.empty().append(titleComponent).append(Component.literal(" ")).append(nameComponent);
        }
        return Component.empty().append(nameComponent).append(Component.literal(" ")).append(titleComponent);
    }
}
