package net.satisfy.legacy.platform.neoforge;

import net.satisfy.legacy.neoforge.config.LegacyNeoForgeConfig;

public class PlatformHelperImpl {
    public static boolean isOwnNameTagEnabled() {
        return LegacyNeoForgeConfig.enableOwnNameTag;
    }

    public static void setOwnNameTagEnabled(boolean value) {
        LegacyNeoForgeConfig.enableOwnNameTag = value;
        LegacyNeoForgeConfig.ENABLE_OWN_NAME_TAG.set(value);
    }

    public static boolean isOtherPlayersTitlesEnabled() {
        return LegacyNeoForgeConfig.enableOtherPlayersTitles;
    }

    public static void setOtherPlayersTitlesEnabled(boolean value) {
        LegacyNeoForgeConfig.enableOtherPlayersTitles = value;
        LegacyNeoForgeConfig.ENABLE_OTHER_PLAYERS_TITLES.set(value);
    }

    public static boolean isRarityColorsEnabled() {
        return LegacyNeoForgeConfig.enableRarityColors;
    }

    public static void setRarityColorsEnabled(boolean value) {
        LegacyNeoForgeConfig.enableRarityColors = value;
        LegacyNeoForgeConfig.ENABLE_RARITY_COLORS.set(value);
    }

    public static boolean isUnlockToastsEnabled() {
        return LegacyNeoForgeConfig.enableUnlockToasts;
    }

    public static void setUnlockToastsEnabled(boolean value) {
        LegacyNeoForgeConfig.enableUnlockToasts = value;
        LegacyNeoForgeConfig.ENABLE_UNLOCK_TOASTS.set(value);
    }

    public static boolean isMilestoneToastsEnabled() {
        return LegacyNeoForgeConfig.enableMilestoneToasts;
    }

    public static void setMilestoneToastsEnabled(boolean value) {
        LegacyNeoForgeConfig.enableMilestoneToasts = value;
        LegacyNeoForgeConfig.ENABLE_MILESTONE_TOASTS.set(value);
    }

    public static int getJournalButtonOffsetX() {
        return LegacyNeoForgeConfig.journalButtonOffsetX;
    }

    public static void setJournalButtonOffsetX(int value) {
        LegacyNeoForgeConfig.journalButtonOffsetX = value;
        LegacyNeoForgeConfig.JOURNAL_BUTTON_OFFSET_X.set(value);
    }

    public static int getJournalButtonOffsetY() {
        return LegacyNeoForgeConfig.journalButtonOffsetY;
    }

    public static void setJournalButtonOffsetY(int value) {
        LegacyNeoForgeConfig.journalButtonOffsetY = value;
        LegacyNeoForgeConfig.JOURNAL_BUTTON_OFFSET_Y.set(value);
    }

    public static void saveConfig() {
        LegacyNeoForgeConfig.CLIENT_CONFIG.save();
    }
}
