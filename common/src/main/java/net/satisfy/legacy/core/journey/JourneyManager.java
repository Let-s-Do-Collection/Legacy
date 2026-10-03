package net.satisfy.legacy.core.journey;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JourneyManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private static final String DIRECTORY = "legacy/journeys";

    public static final JourneyManager INSTANCE = new JourneyManager();

    private Map<String, Journey> journeys = Collections.emptyMap();

    private JourneyManager() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<String, Journey> loaded = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation file = entry.getKey();
            try {
                Journey journey = GSON.fromJson(entry.getValue(), Journey.class);
                if (journey == null) {
                    continue;
                }
                journey.id = file.toString();
                if (journey.trigger == null) {
                    LOGGER.warn("[{}] Journey '{}' has no trigger - skipping.", Legacy.MOD_ID, file);
                    continue;
                }
                if (!net.satisfy.legacy.core.CompatGate.present(journey.requiredMods)) {
                    continue;
                }
                loaded.put(journey.id, journey);
            } catch (Exception ex) {
                LOGGER.warn("[{}] Failed to parse journey '{}': {}", Legacy.MOD_ID, file, ex.getMessage());
            }
        }
        this.journeys = Collections.unmodifiableMap(loaded);
        LOGGER.info("[{}] Loaded {} journeys.", Legacy.MOD_ID, loaded.size());
    }

    public int reload(ResourceManager resourceManager) {
        apply(prepare(resourceManager, InactiveProfiler.INSTANCE), resourceManager, InactiveProfiler.INSTANCE);
        return this.journeys.size();
    }

    public List<Journey> all() {
        return new ArrayList<>(journeys.values());
    }

    public Journey get(String id) {
        return journeys.get(id);
    }
}
