package net.satisfy.legacy.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.nio.file.Files;
import java.nio.file.Path;

@Environment(EnvType.CLIENT)
public final class LegacyClientConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean showOwnName = true;
    public static boolean milestoneNotifications = true;
    public static boolean showOthersTitles = true;
    public static boolean titleUnlockToasts = true;
    public static boolean titleColors = true;

    private LegacyClientConfig() {
    }

    private static Path file() {
        return Platform.getConfigFolder().resolve("legacy-client.json");
    }

    public static void load() {
        try {
            Path f = file();
            if (Files.exists(f)) {
                Data d = GSON.fromJson(Files.readString(f), Data.class);
                if (d != null) {
                    showOwnName = d.showOwnName;
                    milestoneNotifications = d.milestoneNotifications;
                    showOthersTitles = d.showOthersTitles;
                    titleUnlockToasts = d.titleUnlockToasts;
                    titleColors = d.titleColors;
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        try {
            Path f = file();
            Files.createDirectories(f.getParent());
            Data d = new Data();
            d.showOwnName = showOwnName;
            d.milestoneNotifications = milestoneNotifications;
            d.showOthersTitles = showOthersTitles;
            d.titleUnlockToasts = titleUnlockToasts;
            d.titleColors = titleColors;
            Files.writeString(f, GSON.toJson(d));
        } catch (Exception ignored) {
        }
    }

    private static final class Data {
        boolean showOwnName = true;
        boolean milestoneNotifications = true;
        boolean showOthersTitles = true;
        boolean titleUnlockToasts = true;
        boolean titleColors = true;
    }
}
