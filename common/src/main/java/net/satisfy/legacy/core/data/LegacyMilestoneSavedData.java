package net.satisfy.legacy.core.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.satisfy.legacy.Legacy;
import net.satisfy.legacy.core.milestone.MilestoneRecord;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class LegacyMilestoneSavedData extends SavedData {
    public static final String DATA_NAME = Legacy.MOD_ID + "_milestones";

    private final Map<String, MilestoneRecord> records = new LinkedHashMap<>();

    public static LegacyMilestoneSavedData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(LegacyMilestoneSavedData::new, LegacyMilestoneSavedData::load, null),
                DATA_NAME
        );
    }

    public MilestoneRecord getOrCreate(String milestoneId) {
        return records.computeIfAbsent(milestoneId, id -> {
            setDirty();
            return new MilestoneRecord();
        });
    }

    public Map<String, MilestoneRecord> all() {
        return records;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag recordsTag = new CompoundTag();
        for (Map.Entry<String, MilestoneRecord> entry : records.entrySet()) {
            recordsTag.put(entry.getKey(), entry.getValue().save());
        }
        tag.put("records", recordsTag);
        return tag;
    }

    public static LegacyMilestoneSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        LegacyMilestoneSavedData data = new LegacyMilestoneSavedData();
        CompoundTag recordsTag = tag.getCompound("records");
        for (String key : recordsTag.getAllKeys()) {
            data.records.put(key, MilestoneRecord.load(recordsTag.getCompound(key)));
        }
        return data;
    }
}
