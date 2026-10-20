package net.satisfy.legacy.neoforge.config;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class LegacyNeoForgeConfig {
    public static final ModConfigSpec CLIENT_CONFIG;
    public static final ModConfigSpec.BooleanValue ENABLE_OWN_NAME_TAG;
    public static final ModConfigSpec.BooleanValue ENABLE_OTHER_PLAYERS_TITLES;
    public static final ModConfigSpec.BooleanValue ENABLE_RARITY_COLORS;
    public static final ModConfigSpec.BooleanValue ENABLE_UNLOCK_TOASTS;
    public static final ModConfigSpec.BooleanValue ENABLE_MILESTONE_TOASTS;
    public static final ModConfigSpec.IntValue JOURNAL_BUTTON_OFFSET_X;
    public static final ModConfigSpec.IntValue JOURNAL_BUTTON_OFFSET_Y;

    public static boolean enableOwnNameTag = true;
    public static boolean enableOtherPlayersTitles = true;
    public static boolean enableRarityColors = true;
    public static boolean enableUnlockToasts = true;
    public static boolean enableMilestoneToasts = true;
    public static int journalButtonOffsetX = 0;
    public static int journalButtonOffsetY = 0;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Titles above player names and how they look. Only affects your own screen.").push("Titles");
        ENABLE_OWN_NAME_TAG = builder.comment("Shows your own name and title above your head in third person view.").define("enableOwnNameTag", true);
        ENABLE_OTHER_PLAYERS_TITLES = builder.comment("Shows the titles of other players above their names.").define("enableOtherPlayersTitles", true);
        ENABLE_RARITY_COLORS = builder.comment("Titles are colored by their rarity on name tags, in the journal and in toasts. Off shows them in plain white.").define("enableRarityColors", true);
        builder.pop();

        builder.comment("Toasts when you or other players reach something. Only affects your own screen.").push("Notifications");
        ENABLE_UNLOCK_TOASTS = builder.comment("Shows a toast when you unlock a new title.").define("enableUnlockToasts", true);
        ENABLE_MILESTONE_TOASTS = builder.comment("Shows a toast when another player claims a server milestone. Your own milestones always show.").define("enableMilestoneToasts", true);
        builder.pop();

        builder.comment("The journal in your inventory. Only affects your own screen.").push("Journal");
        JOURNAL_BUTTON_OFFSET_X = builder.comment("Moves the journal button in the inventory sideways, in pixels. Negative values move it left.").defineInRange("journalButtonOffsetX", 0, -500, 500);
        JOURNAL_BUTTON_OFFSET_Y = builder.comment("Moves the journal button in the inventory up or down, in pixels. Negative values move it up.").defineInRange("journalButtonOffsetY", 0, -500, 500);
        builder.pop();

        CLIENT_CONFIG = builder.build();
    }

    public static void onLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == CLIENT_CONFIG) {
            sync();
        }
    }

    public static void onReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == CLIENT_CONFIG) {
            sync();
        }
    }

    public static void sync() {
        enableOwnNameTag = ENABLE_OWN_NAME_TAG.get();
        enableOtherPlayersTitles = ENABLE_OTHER_PLAYERS_TITLES.get();
        enableRarityColors = ENABLE_RARITY_COLORS.get();
        enableUnlockToasts = ENABLE_UNLOCK_TOASTS.get();
        enableMilestoneToasts = ENABLE_MILESTONE_TOASTS.get();
        journalButtonOffsetX = JOURNAL_BUTTON_OFFSET_X.get();
        journalButtonOffsetY = JOURNAL_BUTTON_OFFSET_Y.get();
    }
}
