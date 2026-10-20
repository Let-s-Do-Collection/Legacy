package net.satisfy.legacy.core.trigger;

import net.minecraft.resources.ResourceLocation;
import net.satisfy.legacy.core.journey.Journey;
import net.satisfy.legacy.core.journey.JourneyManager;
import net.satisfy.legacy.core.title.Title;
import net.satisfy.legacy.core.title.TitleManager;
import net.satisfy.legacy.core.title.TitleRequirement;
import net.satisfy.legacy.core.title.TriggerType;

import java.util.HashSet;
import java.util.Set;

public final class Counters {
    public static final String HARVEST_ANY = "harvest:crops";
    public static final String PLACE_SAPLINGS = "place:saplings";
    public static final String PLACE_CROPS = "place:crops";
    public static final String PLACE_BEDS = "place:beds";
    public static final String PLACE_BANNERS = "place:banners";
    public static final String BONEMEAL_USED = "bonemeal:used";
    public static final String DIMENSIONS_DISTINCT = "dimensions_distinct";
    public static final String BIOMES_DISTINCT = "biomes_distinct";
    public static final String STRUCTURES_DISTINCT = "structures_distinct";

    private static volatile Set<String> tracked;

    private Counters() {
    }

    public static void invalidate() {
        tracked = null;
    }

    public static boolean isTracked(String key) {
        Set<String> keys = tracked;
        if (keys == null) {
            keys = collectTracked();
            tracked = keys;
        }
        return keys.contains(key);
    }

    private static Set<String> collectTracked() {
        Set<String> keys = new HashSet<>();
        for (Title title : TitleManager.INSTANCE.all()) {
            addTracked(keys, title.getTrigger(), title.requirement());
        }
        for (Journey journey : JourneyManager.INSTANCE.all()) {
            addTracked(keys, journey.triggerType(), journey.requirement());
        }
        return Set.copyOf(keys);
    }

    private static void addTracked(Set<String> keys, TriggerType type, TitleRequirement req) {
        if (type == TriggerType.EVENT && req != null && req.counter != null) {
            keys.add(req.counter);
        }
    }

    public static String harvest(ResourceLocation block) {
        return "harvest:" + block;
    }

    public static String mineTag(ResourceLocation tag) {
        return "mine:#" + tag;
    }

    public static String place(ResourceLocation block) {
        return "place:" + block;
    }

    public static String dimension(ResourceLocation id) {
        return "dimension:" + id;
    }

    public static String biome(ResourceLocation id) {
        return "biome:" + id;
    }

    public static String structure(ResourceLocation id) {
        return "structure:" + id;
    }

    public static String craft(ResourceLocation item) {
        return "craft:" + item;
    }

    public static String use(ResourceLocation block) {
        return "use:" + block;
    }

    public static String consume(ResourceLocation item) {
        return "consume:" + item;
    }

    public static String consumeTag(ResourceLocation tag) {
        return "consume:#" + tag;
    }
}
