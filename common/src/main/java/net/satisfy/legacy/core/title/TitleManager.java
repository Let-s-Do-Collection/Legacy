package net.satisfy.legacy.core.title;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.satisfy.legacy.Legacy;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Loads titles per file from {@code data/<namespace>/legacy/titles/*.json} — the same
 * discovery model as advancements and loot tables. One title = one file, so any datapack can
 * add or override a title with a single JSON file. Everything in Legacy is data-driven.
 */
public class TitleManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private static final String DIRECTORY = "legacy/titles";

    // Must be declared AFTER GSON/DIRECTORY: the constructor passes them to super(),
    // and static fields initialize in declaration order.
    public static final TitleManager INSTANCE = new TitleManager();

    private Map<String, Title> titles = Collections.emptyMap();

    private TitleManager() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<Title> loaded = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        // Deterministic order across datapacks/loaders.
        List<ResourceLocation> keys = new ArrayList<>(object.keySet());
        keys.sort(Comparator.comparing(ResourceLocation::toString));

        for (ResourceLocation file : keys) {
            JsonElement element = object.get(file);
            if (!element.isJsonObject()) {
                LOGGER.warn("[{}] Skipping non-object title file '{}'.", Legacy.MOD_ID, file);
                continue;
            }
            JsonObject json = element.getAsJsonObject();
            Title title;
            try {
                title = GSON.fromJson(json, Title.class);
            } catch (RuntimeException e) {
                LOGGER.warn("[{}] Skipping malformed title file '{}': {}", Legacy.MOD_ID, file, e.getMessage());
                continue;
            }
            if (title != null && (title.id == null || title.id.isBlank())) {
                title.id = file.getPath();
            }
            if (TitleValidator.validate(json, title, seenIds)) {
                loaded.add(title);
            }
        }
        replaceAll(loaded);
        LOGGER.info("[{}] Loaded {} title(s) from {} file(s).", Legacy.MOD_ID, this.titles.size(), object.size());
    }

    /** Used by {@code /legacy reload} to refresh titles without a full resource reload. */
    public int reload(ResourceManager resourceManager) {
        apply(prepare(resourceManager, InactiveProfiler.INSTANCE), resourceManager, InactiveProfiler.INSTANCE);
        return this.titles.size();
    }

    public void replaceAll(List<Title> newTitles) {
        Map<String, Title> map = new LinkedHashMap<>();
        for (Title title : newTitles) {
            map.putIfAbsent(title.getId(), title);
        }
        this.titles = Collections.unmodifiableMap(map);
    }

    public boolean has(String id) {
        return titles.containsKey(id);
    }

    public Optional<Title> get(String id) {
        return Optional.ofNullable(titles.get(id));
    }

    public List<Title> all() {
        return new ArrayList<>(titles.values());
    }

    public List<Title> allSorted() {
        List<Title> list = all();
        list.sort(Comparator.comparingInt((Title t) -> t.displayPriority).thenComparing(Title::getId));
        return list;
    }
}
