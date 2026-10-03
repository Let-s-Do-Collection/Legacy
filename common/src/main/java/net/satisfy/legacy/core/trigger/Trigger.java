package net.satisfy.legacy.core.trigger;

import com.google.gson.annotations.SerializedName;
import net.satisfy.legacy.core.title.TitleRequirement;
import net.satisfy.legacy.core.title.TriggerType;

public class Trigger {
    @SerializedName("type")
    public String type = "custom";
    @SerializedName("id")
    public String id;
    @SerializedName("counter")
    public String counter;
    @SerializedName("event")
    public String event;
    @SerializedName("stat")
    public String stat;
    @SerializedName("stat_type")
    public String statType = "minecraft:custom";
    @SerializedName("value")
    public int value = 1;

    public TriggerType type() {
        String t = type == null ? "" : type;
        return switch (t) {
            case "advancement" -> TriggerType.ADVANCEMENT;
            case "counter", "event" -> TriggerType.EVENT;
            case "statistic", "stat", "minecraft_stat" -> TriggerType.MINECRAFT_STAT;
            case "item" -> TriggerType.ITEM;
            default -> TriggerType.CUSTOM;
        };
    }

    public TitleRequirement requirement() {
        TitleRequirement req = new TitleRequirement();
        req.amount = Math.max(1, value);
        switch (type()) {
            case ADVANCEMENT -> req.advancement = id;
            case EVENT -> req.counter = counter != null ? counter : event;
            case MINECRAFT_STAT -> {
                req.stat = stat;
                req.statType = statType;
            }
            case ITEM, CUSTOM -> req.id = id;
        }
        return req;
    }
}
