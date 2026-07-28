package net.satisfy.legacy.core.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.satisfy.legacy.core.title.TitleForm;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class PlayerTitleData {
    private final Set<String> unlocked = new LinkedHashSet<>();
    private final Map<String, Integer> counters = new LinkedHashMap<>();
    private String active = "";
    private TitleForm form = TitleForm.MASCULINE;

    public Set<String> getUnlocked() {
        return unlocked;
    }

    public boolean isUnlocked(String id) {
        return unlocked.contains(id);
    }

    public boolean unlock(String id) {
        return unlocked.add(id);
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active == null ? "" : active;
    }

    public TitleForm getForm() {
        return form;
    }

    public void setForm(TitleForm form) {
        this.form = form == null ? TitleForm.MASCULINE : form;
    }

    public int getCounter(String key) {
        return key == null ? 0 : counters.getOrDefault(key, 0);
    }

    public int addCounter(String key, int delta) {
        if (key == null || delta == 0) {
            return getCounter(key);
        }
        int value = Math.max(0, getCounter(key) + delta);
        counters.put(key, value);
        return value;
    }

    public boolean markVisited(String key) {
        if (key == null || counters.getOrDefault(key, 0) > 0) {
            return false;
        }
        counters.put(key, 1);
        return true;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (String id : unlocked) {
            list.add(StringTag.valueOf(id));
        }
        tag.put("unlocked", list);
        CompoundTag countersTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : counters.entrySet()) {
            countersTag.putInt(entry.getKey(), entry.getValue());
        }
        tag.put("counters", countersTag);
        tag.putString("active", active);
        tag.putInt("form", form.ordinal());
        return tag;
    }

    public static PlayerTitleData load(CompoundTag tag) {
        PlayerTitleData data = new PlayerTitleData();
        ListTag list = tag.getList("unlocked", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            data.unlocked.add(list.getString(i));
        }
        CompoundTag countersTag = tag.getCompound("counters");
        for (String key : countersTag.getAllKeys()) {
            data.counters.put(key, countersTag.getInt(key));
        }
        data.active = tag.getString("active");
        data.form = TitleForm.byOrdinal(tag.getInt("form"));
        return data;
    }
}
