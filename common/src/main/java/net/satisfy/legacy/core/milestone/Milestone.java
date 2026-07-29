package net.satisfy.legacy.core.milestone;

import com.google.gson.annotations.SerializedName;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.satisfy.legacy.core.title.TitleRarity;

public class Milestone {
    @SerializedName("id")
    public String id;

    @SerializedName("translation_key")
    public String translationKey;

    @SerializedName("coop")
    public boolean coop = false;

    @SerializedName("trigger_kind")
    public String triggerKind = "advancement";

    @SerializedName("trigger_value")
    public String triggerValue = "";

    @SerializedName("retro_advancement")
    public String retroAdvancement;

    @SerializedName("icon")
    public String icon = "minecraft:paper";

    @SerializedName("rarity")
    public TitleRarity rarity = TitleRarity.EPIC;

    public String getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey != null ? translationKey : "milestone.legacy." + id;
    }

    public TitleRarity getRarity() {
        return rarity == null ? TitleRarity.EPIC : rarity;
    }

    public String retroAdvancement() {
        if (retroAdvancement != null) {
            return retroAdvancement;
        }
        return "advancement".equals(triggerKind) ? triggerValue : null;
    }

    public ItemStack iconStack() {
        ResourceLocation itemId = ResourceLocation.tryParse(icon == null ? "" : icon);
        Item item = itemId == null ? Items.PAPER : BuiltInRegistries.ITEM.getOptional(itemId).orElse(Items.PAPER);
        return new ItemStack(item);
    }
}
