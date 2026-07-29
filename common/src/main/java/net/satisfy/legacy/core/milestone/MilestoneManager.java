package net.satisfy.legacy.core.milestone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.satisfy.legacy.Legacy;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class MilestoneManager {
    public static final MilestoneManager INSTANCE = new MilestoneManager();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();

    private Map<String, Milestone> milestones = Collections.emptyMap();
    private boolean loaded = false;

    private MilestoneManager() {
    }

    public void ensureLoaded(MinecraftServer server) {
        if (!loaded) {
            reload(server.getResourceManager());
        }
    }

    public int reload(ResourceManager resourceManager) {
        Map<String, Milestone> map = new LinkedHashMap<>();
        Optional<Resource> resource = resourceManager.getResource(Legacy.identifier("milestones.json"));
        if (resource.isPresent()) {
            try (BufferedReader reader = resource.get().openAsReader()) {
                JsonElement root = JsonParser.parseReader(reader);
                if (root.isJsonObject() && root.getAsJsonObject().has("milestones")) {
                    root = root.getAsJsonObject().get("milestones");
                }
                if (root.isJsonArray()) {
                    for (JsonElement element : root.getAsJsonArray()) {
                        Milestone milestone = GSON.fromJson(element, Milestone.class);
                        if (milestone != null && milestone.id != null && !milestone.id.isBlank()) {
                            map.put(milestone.id, milestone);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("[{}] Failed to load milestones.json", Legacy.MOD_ID, e);
            }
        }
        this.milestones = Collections.unmodifiableMap(map);
        this.loaded = true;
        LOGGER.info("[{}] Loaded {} milestone(s).", Legacy.MOD_ID, this.milestones.size());
        return this.milestones.size();
    }

    public List<Milestone> all() {
        return new ArrayList<>(milestones.values());
    }

    public Milestone get(String id) {
        return milestones.get(id);
    }
}
