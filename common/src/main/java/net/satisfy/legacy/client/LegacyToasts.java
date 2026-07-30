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
        if (!LegacyClientConfig.titleUnlockToasts) {
            return;
        }
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

    public static void showMilestone(String milestoneId, String playerName) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!LegacyClientConfig.milestoneNotifications
                && (minecraft.player == null || !minecraft.player.getGameProfile().getName().equals(playerName))) {
            return;
        }
        ClientMilestoneData.Entry entry = ClientMilestoneData.all().stream()
                .filter(e -> e.id().equals(milestoneId)).findFirst().orElse(null);
        Component name = entry != null
                ? Component.translatable(entry.nameKey()).withStyle(entry.rarityValue().getColor())
                : Component.translatable("milestone.legacy." + milestoneId);
        net.minecraft.world.item.ItemStack icon = entry != null ? entry.iconStack() : net.minecraft.world.item.ItemStack.EMPTY;

        minecraft.getToasts().addToast(new TitleToast(
                TitleToast.MILESTONE_BG,
                Component.translatable("toast.legacy.milestone").withStyle(net.minecraft.ChatFormatting.GOLD),
                name,
                Component.translatable("toast.legacy.milestone.earned", playerName).withStyle(net.minecraft.ChatFormatting.WHITE),
                icon));

        // Same fanfare as a title unlock, pitched down so a world-first feels heavier and grander.
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.8F));
    }
}
