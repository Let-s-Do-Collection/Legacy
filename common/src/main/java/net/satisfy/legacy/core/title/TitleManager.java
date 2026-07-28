package net.satisfy.legacy.core.title;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.satisfy.legacy.Legacy;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class TitleManager extends SimplePreparableReloadListener<List<Title>> {
    public static final TitleManager INSTANCE = new TitleManager();

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();

    private static final ResourceLocation TITLES = Legacy.identifier("titles.json");

    private Map<String, Title> titles = Collections.emptyMap();

    private TitleManager() {
    }

    @Override
    protected List<Title> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        List<Title> loaded = new ArrayList<>();

        Optional<Resource> resource = resourceManager.getResource(TITLES);
        if (resource.isEmpty()) {
            LOGGER.info("[{}] No titles.json found - starting with an empty title list.", Legacy.MOD_ID);
            return loaded;
        }

        try (BufferedReader reader = resource.get().openAsReader()) {
            JsonElement root = JsonParser.parseReader(reader);
            if (root.isJsonObject() && root.getAsJsonObject().has("titles")) {
                root = root.getAsJsonObject().get("titles");
            }
            if (!root.isJsonArray()) {
                LOGGER.warn("[{}] titles.json must contain a 'titles' array.", Legacy.MOD_ID);
                return loaded;
            }
            Set<String> seenIds = new HashSet<>();
            for (JsonElement element : root.getAsJsonArray()) {
                if (!element.isJsonObject()) {
                    LOGGER.warn("[{}] Skipping non-object entry in titles.json: {}", Legacy.MOD_ID, element);
                    continue;
                }
                JsonObject object = element.getAsJsonObject();
                Title title;
                try {
                    title = GSON.fromJson(object, Title.class);
                } catch (RuntimeException e) {
                    LOGGER.warn("[{}] Skipping malformed title entry {}: {}", Legacy.MOD_ID, object, e.getMessage());
                    continue;
                }
                if (TitleValidator.validate(object, title, seenIds)) {
                    loaded.add(title);
                }
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.error("[{}] Failed to load titles.json", Legacy.MOD_ID, e);
        }

        return loaded;
    }

    public int reload(ResourceManager resourceManager) {
        List<Title> prepared = prepare(resourceManager, InactiveProfiler.INSTANCE);
        apply(prepared, resourceManager, InactiveProfiler.INSTANCE);
        return this.titles.size();
    }

    @Override
    protected void apply(List<Title> prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
        replaceAll(prepared);
        LOGGER.info("[{}] Loaded {} title(s).", Legacy.MOD_ID, this.titles.size());
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
