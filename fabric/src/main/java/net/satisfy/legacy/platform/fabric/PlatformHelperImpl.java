package net.satisfy.legacy.platform.fabric;

import me.shedaniel.autoconfig.AutoConfig;
import net.satisfy.legacy.fabric.config.LegacyFabricConfig;

public class PlatformHelperImpl {
    private static LegacyFabricConfig config() {
        return AutoConfig.getConfigHolder(LegacyFabricConfig.class).getConfig();
    }

    public static boolean isOwnNameTagEnabled() {
        return config().titles.enableOwnNameTag;
    }

    public static void setOwnNameTagEnabled(boolean value) {
        config().titles.enableOwnNameTag = value;
    }

    public static boolean isOtherPlayersTitlesEnabled() {
        return config().titles.enableOtherPlayersTitles;
    }

    public static void setOtherPlayersTitlesEnabled(boolean value) {
        config().titles.enableOtherPlayersTitles = value;
    }

    public static boolean isRarityColorsEnabled() {
        return config().titles.enableRarityColors;
    }

    public static void setRarityColorsEnabled(boolean value) {
        config().titles.enableRarityColors = value;
    }

    public static boolean isUnlockToastsEnabled() {
        return config().notifications.enableUnlockToasts;
    }

    public static void setUnlockToastsEnabled(boolean value) {
        config().notifications.enableUnlockToasts = value;
    }

    public static boolean isMilestoneToastsEnabled() {
        return config().notifications.enableMilestoneToasts;
    }

    public static void setMilestoneToastsEnabled(boolean value) {
        config().notifications.enableMilestoneToasts = value;
    }

    public static int getJournalButtonOffsetX() {
        return config().journal.journalButtonOffsetX;
    }

    public static void setJournalButtonOffsetX(int value) {
        config().journal.journalButtonOffsetX = value;
    }

    public static int getJournalButtonOffsetY() {
        return config().journal.journalButtonOffsetY;
    }

    public static void setJournalButtonOffsetY(int value) {
        config().journal.journalButtonOffsetY = value;
    }

    public static void saveConfig() {
        AutoConfig.getConfigHolder(LegacyFabricConfig.class).save();
    }
}
