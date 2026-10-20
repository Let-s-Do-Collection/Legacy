package net.satisfy.legacy.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.satisfy.legacy.Legacy;
import net.satisfy.legacy.platform.PlatformHelper;

import java.nio.file.Files;
import java.nio.file.Path;

@Environment(EnvType.CLIENT)
final class LegacyConfigMigration {
    private LegacyConfigMigration() {
    }

    static void run() {
        Path file = Platform.getConfigFolder().resolve("legacy-client.json");
        if (!Files.exists(file)) {
            return;
        }
        try {
            JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            if (json.has("showOwnName")) {
                PlatformHelper.setOwnNameTagEnabled(json.get("showOwnName").getAsBoolean());
            }
            if (json.has("showOthersTitles")) {
                PlatformHelper.setOtherPlayersTitlesEnabled(json.get("showOthersTitles").getAsBoolean());
            }
            if (json.has("titleColors")) {
                PlatformHelper.setRarityColorsEnabled(json.get("titleColors").getAsBoolean());
            }
            if (json.has("titleUnlockToasts")) {
                PlatformHelper.setUnlockToastsEnabled(json.get("titleUnlockToasts").getAsBoolean());
            }
            if (json.has("milestoneNotifications")) {
                PlatformHelper.setMilestoneToastsEnabled(json.get("milestoneNotifications").getAsBoolean());
            }
            if (json.has("buttonOffsetX")) {
                PlatformHelper.setJournalButtonOffsetX(Math.max(-500, Math.min(500, json.get("buttonOffsetX").getAsInt())));
            }
            if (json.has("buttonOffsetY")) {
                PlatformHelper.setJournalButtonOffsetY(Math.max(-500, Math.min(500, json.get("buttonOffsetY").getAsInt())));
            }
            PlatformHelper.saveConfig();
            Files.delete(file);
            Legacy.LOGGER.info("[{}] Moved settings from legacy-client.json into the new config", Legacy.MOD_ID);
        } catch (Exception e) {
            Legacy.LOGGER.warn("[{}] Could not move settings from legacy-client.json: {}", Legacy.MOD_ID, e.getMessage());
        }
    }
}
