package net.satisfy.legacy.core.title;

import com.google.gson.annotations.SerializedName;

public class TitleRequirement {
    @SerializedName("stat")
    public String stat;

    @SerializedName("stat_type")
    public String statType = "minecraft:custom";

    @SerializedName("amount")
    public int amount = 1;

    @SerializedName("counter")
    public String counter;

    @SerializedName("advancement")
    public String advancement;

    @SerializedName("id")
    public String id;
}
