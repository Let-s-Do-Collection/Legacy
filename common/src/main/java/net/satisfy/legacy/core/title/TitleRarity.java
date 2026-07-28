package net.satisfy.legacy.core.title;

import com.google.gson.annotations.SerializedName;
import net.minecraft.ChatFormatting;

public enum TitleRarity {
    @SerializedName("common")
    COMMON(ChatFormatting.WHITE),

    @SerializedName("uncommon")
    UNCOMMON(ChatFormatting.GREEN),

    @SerializedName("rare")
    RARE(ChatFormatting.AQUA),

    @SerializedName("epic")
    EPIC(ChatFormatting.LIGHT_PURPLE),

    @SerializedName("legendary")
    LEGENDARY(ChatFormatting.GOLD),

    @SerializedName("mythic")
    MYTHIC(ChatFormatting.RED);

    private static final TitleRarity[] VALUES = values();

    private final ChatFormatting color;

    TitleRarity(ChatFormatting color) {
        this.color = color;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public static TitleRarity byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : COMMON;
    }
}
