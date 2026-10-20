package net.satisfy.legacy.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;

public final class PlatformHelper {
    private PlatformHelper() {
    }

    @ExpectPlatform
    public static boolean isOwnNameTagEnabled() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void setOwnNameTagEnabled(boolean value) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isOtherPlayersTitlesEnabled() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void setOtherPlayersTitlesEnabled(boolean value) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isRarityColorsEnabled() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void setRarityColorsEnabled(boolean value) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isUnlockToastsEnabled() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void setUnlockToastsEnabled(boolean value) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isMilestoneToastsEnabled() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void setMilestoneToastsEnabled(boolean value) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int getJournalButtonOffsetX() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void setJournalButtonOffsetX(int value) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int getJournalButtonOffsetY() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void setJournalButtonOffsetY(int value) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void saveConfig() {
        throw new AssertionError();
    }
}
