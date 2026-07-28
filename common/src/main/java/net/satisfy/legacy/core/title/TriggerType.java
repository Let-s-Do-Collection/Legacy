package net.satisfy.legacy.core.title;

import com.google.gson.annotations.SerializedName;

public enum TriggerType {
    @SerializedName("minecraft_stat")
    MINECRAFT_STAT,

    @SerializedName("event")
    EVENT,

    @SerializedName("advancement")
    ADVANCEMENT,

    @SerializedName("custom")
    CUSTOM
}
