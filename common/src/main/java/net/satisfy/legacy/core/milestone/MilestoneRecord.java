package net.satisfy.legacy.core.milestone;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MilestoneRecord {
    public enum State {
        UNCLAIMED,
        PRE_EXISTING,
        CLAIMED
    }

    public State state = State.UNCLAIMED;
    public UUID firstPlayer;
    public String firstName = "";
    public int worldDay;
    public String time = "";
    public String date = "";
    public long graceUntil;
    public final List<Recipient> recipients = new ArrayList<>();

    public boolean hasRecipient(UUID id) {
        for (Recipient r : recipients) {
            if (r.uuid.equals(id)) {
                return true;
            }
        }
        return false;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("state", state.name());
        if (firstPlayer != null) {
            tag.putUUID("first", firstPlayer);
        }
        tag.putString("firstName", firstName);
        tag.putInt("worldDay", worldDay);
        tag.putString("time", time);
        tag.putString("date", date);
        tag.putLong("graceUntil", graceUntil);
        ListTag list = new ListTag();
        for (Recipient r : recipients) {
            CompoundTag rt = new CompoundTag();
            rt.putUUID("uuid", r.uuid);
            rt.putString("name", r.name);
            list.add(rt);
        }
        tag.put("recipients", list);
        return tag;
    }

    public static MilestoneRecord load(CompoundTag tag) {
        MilestoneRecord record = new MilestoneRecord();
        try {
            record.state = State.valueOf(tag.getString("state"));
        } catch (IllegalArgumentException ignored) {
            record.state = State.UNCLAIMED;
        }
        if (tag.hasUUID("first")) {
            record.firstPlayer = tag.getUUID("first");
        }
        record.firstName = tag.getString("firstName");
        record.worldDay = tag.getInt("worldDay");
        record.time = tag.getString("time");
        record.date = tag.getString("date");
        record.graceUntil = tag.getLong("graceUntil");
        ListTag list = tag.getList("recipients", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag rt = list.getCompound(i);
            if (rt.hasUUID("uuid")) {
                record.recipients.add(new Recipient(rt.getUUID("uuid"), rt.getString("name")));
            }
        }
        return record;
    }

    public record Recipient(UUID uuid, String name) {
    }
}
