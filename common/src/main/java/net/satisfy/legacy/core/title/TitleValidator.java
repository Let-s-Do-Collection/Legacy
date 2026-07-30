package net.satisfy.legacy.core.title;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.legacy.Legacy;
import org.slf4j.Logger;

import java.util.Set;

public final class TitleValidator {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Set<String> PLACEMENTS = Set.of("prefix", "suffix");
    private static final Set<String> TRIGGERS = Set.of(
            "minecraft_stat", "statistic", "stat", "event", "counter", "advancement", "item", "custom");
    private static final Set<String> RARITIES = Set.of("common", "uncommon", "rare", "epic", "legendary", "mythic");

    private TitleValidator() {
    }

    public static boolean validate(JsonObject raw, Title title, Set<String> seenIds) {
        if (title == null || title.id == null || title.id.isBlank()) {
            LOGGER.warn("[{}] Skipping title with a missing or blank 'id': {}", Legacy.MOD_ID, raw);
            return false;
        }
        String id = title.id;
        if (!seenIds.add(id)) {
            LOGGER.warn("[{}] Duplicate title id '{}' - keeping the first definition, ignoring the rest.", Legacy.MOD_ID, id);
            return false;
        }

        warnUnknownEnum(raw, "placement", PLACEMENTS, id);
        warnUnknownEnum(raw, "rarity", RARITIES, id);
        if (raw.has("trigger") && raw.get("trigger").isJsonObject()
                && raw.getAsJsonObject("trigger").has("type")
                && raw.getAsJsonObject("trigger").get("type").isJsonPrimitive()) {
            String type = raw.getAsJsonObject("trigger").get("type").getAsString();
            if (!TRIGGERS.contains(type.toLowerCase(java.util.Locale.ROOT))) {
                LOGGER.warn("[{}] Title '{}' has invalid 'trigger.type' = '{}'. Allowed: {}. Using 'custom'.",
                        Legacy.MOD_ID, id, type, TRIGGERS);
            }
        }

        if (!raw.has("translation_key") && (title.literalName() == null)) {
            LOGGER.warn("[{}] Title '{}' has neither 'translation_key' nor a literal 'title'; defaulting to 'title.legacy.{}'.", Legacy.MOD_ID, id, id);
        }

        validateIcon(title, id);
        validateRequirement(title, id);
        return true;
    }

    private static void warnUnknownEnum(JsonObject raw, String field, Set<String> allowed, String id) {
        if (raw.has(field) && raw.get(field).isJsonPrimitive()) {
            String value = raw.get(field).getAsString();
            if (!allowed.contains(value.toLowerCase(java.util.Locale.ROOT))) {
                LOGGER.warn("[{}] Title '{}' has invalid '{}' = '{}'. Allowed: {}. Using the default.",
                        Legacy.MOD_ID, id, field, value, allowed);
            }
        }
    }

    private static void validateIcon(Title title, String id) {
        if (title.icon == null || title.icon.isBlank()) {
            return;
        }
        ResourceLocation itemId = ResourceLocation.tryParse(title.icon);
        if (itemId == null) {
            LOGGER.warn("[{}] Title '{}' has a malformed 'icon' id '{}'; using minecraft:paper.", Legacy.MOD_ID, id, title.icon);
        } else if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
            LOGGER.warn("[{}] Title '{}' references unknown 'icon' item '{}'; using minecraft:paper.", Legacy.MOD_ID, id, title.icon);
        }
    }

    private static void validateRequirement(Title title, String id) {
        if (title.milestone) {
            return;
        }
        TitleRequirement req = title.requirement();
        switch (title.getTrigger()) {
            case MINECRAFT_STAT -> {
                if (req == null || req.stat == null) {
                    LOGGER.warn("[{}] Title '{}' uses trigger 'minecraft_stat' but has no 'requirement.stat'; it can never unlock.", Legacy.MOD_ID, id);
                }
            }
            case EVENT -> {
                if (req == null || req.counter == null) {
                    LOGGER.warn("[{}] Title '{}' uses trigger 'event' but has no 'requirement.counter'; it can never unlock.", Legacy.MOD_ID, id);
                }
            }
            case ADVANCEMENT -> {
                if (req == null || req.advancement == null) {
                    LOGGER.warn("[{}] Title '{}' uses trigger 'advancement' but has no 'requirement.advancement'; it can never unlock.", Legacy.MOD_ID, id);
                }
            }
            case ITEM -> {
                if (req == null || req.id == null) {
                    LOGGER.warn("[{}] Title '{}' uses trigger 'item' but has no item id; it can never unlock.", Legacy.MOD_ID, id);
                }
            }
            case CUSTOM -> {
                if (req == null || req.id == null) {
                    LOGGER.warn("[{}] Title '{}' uses trigger 'custom' but has no 'requirement.id'; make sure a mod registers a matching trigger.", Legacy.MOD_ID, id);
                }
            }
        }
    }
}
