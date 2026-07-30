package net.satisfy.legacy.core.trigger;

import net.minecraft.resources.ResourceLocation;

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

    private Counters() {
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
