package net.satisfy.legacy.fabric.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "legacy")
@Config.Gui.Background("minecraft:textures/block/bookshelf.png")
public class LegacyFabricConfig implements ConfigData {
    @ConfigEntry.Gui.CollapsibleObject
    public TitlesSettings titles = new TitlesSettings();

    @ConfigEntry.Gui.CollapsibleObject
    public NotificationsSettings notifications = new NotificationsSettings();

    @ConfigEntry.Gui.CollapsibleObject
    public JournalSettings journal = new JournalSettings();

    public static class TitlesSettings {
        @ConfigEntry.Gui.Tooltip
        public boolean enableOwnNameTag = true;

        @ConfigEntry.Gui.Tooltip
        public boolean enableOtherPlayersTitles = true;

        @ConfigEntry.Gui.Tooltip
        public boolean enableRarityColors = true;
    }

    public static class NotificationsSettings {
        @ConfigEntry.Gui.Tooltip
        public boolean enableUnlockToasts = true;

        @ConfigEntry.Gui.Tooltip
        public boolean enableMilestoneToasts = true;
    }

    public static class JournalSettings {
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = -500, max = 500)
        public int journalButtonOffsetX = 0;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = -500, max = 500)
        public int journalButtonOffsetY = 0;
    }
}
