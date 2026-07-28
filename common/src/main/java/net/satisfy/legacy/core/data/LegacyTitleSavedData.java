package net.satisfy.legacy.core.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.satisfy.legacy.Legacy;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class LegacyTitleSavedData extends SavedData {
    public static final String DATA_NAME = Legacy.MOD_ID + "_titles";

    private final Map<UUID, PlayerTitleData> players = new LinkedHashMap<>();

    public static LegacyTitleSavedData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(LegacyTitleSavedData::new, LegacyTitleSavedData::load, null),
                DATA_NAME
        );
    }

    public PlayerTitleData getOrCreate(UUID playerId) {
        return players.computeIfAbsent(playerId, id -> {
            setDirty();
            return new PlayerTitleData();
        });
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag playersTag = new CompoundTag();
        for (Map.Entry<UUID, PlayerTitleData> entry : players.entrySet()) {
            playersTag.put(entry.getKey().toString(), entry.getValue().save());
        }
        tag.put("players", playersTag);
        return tag;
    }

    public static LegacyTitleSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        LegacyTitleSavedData data = new LegacyTitleSavedData();
        CompoundTag playersTag = tag.getCompound("players");
        for (String key : playersTag.getAllKeys()) {
            try {
                data.players.put(UUID.fromString(key), PlayerTitleData.load(playersTag.getCompound(key)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return data;
    }
}
