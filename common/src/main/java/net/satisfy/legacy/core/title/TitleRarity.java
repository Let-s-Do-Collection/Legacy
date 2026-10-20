package net.satisfy.legacy.core.title;

import com.google.gson.annotations.SerializedName;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.satisfy.foundation.rarity.FoundationRarity;

public enum TitleRarity {
    @SerializedName("common")
    COMMON(FoundationRarity.COMMON),

    @SerializedName("uncommon")
    UNCOMMON(FoundationRarity.UNCOMMON),

    @SerializedName("rare")
    RARE(FoundationRarity.RARE),

    @SerializedName("epic")
    EPIC(FoundationRarity.EPIC),

    @SerializedName("legendary")
    LEGENDARY(FoundationRarity.LEGENDARY),

    @SerializedName("mythic")
    MYTHIC(FoundationRarity.ETERNAL);

    private static final TitleRarity[] VALUES = values();

    private final FoundationRarity foundation;

    TitleRarity(FoundationRarity foundation) {
        this.foundation = foundation;
    }

    public FoundationRarity foundation() {
        return foundation;
    }

    public int baseColor() {
        return foundation.baseColor();
    }

    public MutableComponent apply(Component text) {
        return foundation.apply(text);
    }

    public static TitleRarity byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : COMMON;
    }
}
