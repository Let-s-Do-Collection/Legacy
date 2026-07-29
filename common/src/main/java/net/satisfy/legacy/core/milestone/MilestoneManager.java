package net.satisfy.legacy.core.milestone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.satisfy.legacy.Legacy;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads server milestones per file from {@code data/<namespace>/legacy/milestones/*.json},
 * the same discovery model as titles and journeys — one milestone, one file.
 */
public final class MilestoneManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private static final String DIRECTORY = "legacy/milestones";

    // Must be declared AFTER GSON/DIRECTORY: the constructor passes them to super(),
    // and static fields initialize in declaration order.
    public static final MilestoneManager INSTANCE = new MilestoneManager();

    private Map<String, Milestone> milestones = Collections.emptyMap();
    private boolean loaded = false;

    private MilestoneManager() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<String, Milestone> map = new LinkedHashMap<>();
        List<ResourceLocation> keys = new ArrayList<>(object.keySet());
        keys.sort(Comparator.comparing(ResourceLocation::toString));
        for (ResourceLocation file : keys) {
            try {
                Milestone milestone = GSON.fromJson(object.get(file), Milestone.class);
                if (milestone == null) {
                    continue;
                }
                if (milestone.id == null || milestone.id.isBlank()) {
                    milestone.id = file.getPath();
                }
                map.putIfAbsent(milestone.id, milestone);
            } catch (Exception e) {
                LOGGER.warn("[{}] Failed to parse milestone '{}': {}", Legacy.MOD_ID, file, e.getMessage());
            }
        }
        this.milestones = Collections.unmodifiableMap(map);
        this.loaded = true;
        LOGGER.info("[{}] Loaded {} milestone(s).", Legacy.MOD_ID, this.milestones.size());
    }

    public void ensureLoaded(MinecraftServer server) {
        if (!loaded) {
            reload(server.getResourceManager());
        }
    }

    public int reload(ResourceManager resourceManager) {
        apply(prepare(resourceManager, InactiveProfiler.INSTANCE), resourceManager, InactiveProfiler.INSTANCE);
        return this.milestones.size();
    }

    public List<Milestone> all() {
        return new ArrayList<>(milestones.values());
    }

    public Milestone get(String id) {
        return milestones.get(id);
    }
}
