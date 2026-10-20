package net.satisfy.legacy.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.satisfy.legacy.core.title.Title;
import net.satisfy.legacy.core.title.TitleManager;
import net.satisfy.legacy.platform.PlatformHelper;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class LegacyToasts {
    private LegacyToasts() {
    }

    public static void showUnlock(String titleId) {
        if (!PlatformHelper.isUnlockToastsEnabled()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Optional<Title> maybeTitle = TitleManager.INSTANCE.get(titleId);

        Component name = maybeTitle.<Component>map(title -> TitleNames.styled(title, ClientTitleData.getForm()))
                .orElse(Component.literal(titleId));

        minecraft.getToasts().addToast(new TitleToast(
                name,
                maybeTitle.map(Title::iconStack).orElse(net.minecraft.world.item.ItemStack.EMPTY)
        ));
    }

    public static void showMilestone(String milestoneId, String playerName) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!PlatformHelper.isMilestoneToastsEnabled()
                && (minecraft.player == null || !minecraft.player.getGameProfile().getName().equals(playerName))) {
            return;
        }
        ClientMilestoneData.Entry entry = ClientMilestoneData.all().stream()
                .filter(e -> e.id().equals(milestoneId)).findFirst().orElse(null);
        Component name = entry != null
                ? entry.rarityValue().apply(Component.translatable(entry.nameKey()))
                : Component.translatable("milestone.legacy." + milestoneId);
        net.minecraft.world.item.ItemStack icon = entry != null ? entry.iconStack() : net.minecraft.world.item.ItemStack.EMPTY;

        minecraft.getToasts().addToast(new TitleToast(
                TitleToast.MILESTONE_BG,
                Component.translatable("toast.legacy.milestone").withStyle(net.minecraft.ChatFormatting.GOLD),
                name,
                Component.translatable("toast.legacy.milestone.earned", playerName).withStyle(net.minecraft.ChatFormatting.WHITE),
                icon));

        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.8F));
    }
}
