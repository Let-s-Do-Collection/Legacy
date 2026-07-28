package net.satisfy.legacy.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.satisfy.legacy.core.title.Title;
import net.satisfy.legacy.core.title.TitleManager;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class LegacyToasts {
    private LegacyToasts() {
    }

    public static void showUnlock(String titleId) {
        Minecraft minecraft = Minecraft.getInstance();
        Optional<Title> maybeTitle = TitleManager.INSTANCE.get(titleId);

        Component name = maybeTitle.map(Title::styledDisplayName)
                .map(Component.class::cast)
                .orElse(Component.literal(titleId));

        minecraft.getToasts().addToast(new TitleToast(
                name,
                maybeTitle.map(Title::iconStack).orElse(net.minecraft.world.item.ItemStack.EMPTY)
        ));

        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F));
    }
}
