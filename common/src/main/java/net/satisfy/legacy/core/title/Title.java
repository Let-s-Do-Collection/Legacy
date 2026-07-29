package net.satisfy.legacy.core.title;

import com.google.gson.annotations.SerializedName;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class Title {
    @SerializedName("id")
    public String id;

    @SerializedName("translation_key")
    public String translationKey;

    @SerializedName("placement")
    public Placement placement = Placement.SUFFIX;

    @SerializedName("trigger")
    public TriggerType trigger = TriggerType.CUSTOM;

    @SerializedName("requirement")
    public TitleRequirement requirement = new TitleRequirement();

    @SerializedName("category")
    public String category = "";

    @SerializedName("series")
    public String series = "";

    @SerializedName("stage")
    public int stage = 0;

    @SerializedName("display_priority")
    public int displayPriority = 100;

    @SerializedName("hidden")
    public boolean hidden = false;

    @SerializedName("milestone")
    public boolean milestone = false;

    @SerializedName("icon")
    public String icon = "minecraft:paper";

    @SerializedName("rarity")
    public TitleRarity rarity = TitleRarity.COMMON;

    public transient int syncedTarget = 1;

    public Title() {
    }

    public String getCategory() {
        return category == null || category.isBlank() ? "misc" : category;
    }

    public String progressKey() {
        return getTranslationKey() + ".progress";
    }

    public String getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey != null ? translationKey : "title.legacy." + id;
    }

    public Placement getPlacement() {
        return placement == null ? Placement.SUFFIX : placement;
    }

    public TriggerType getTrigger() {
        return trigger == null ? TriggerType.CUSTOM : trigger;
    }

    public TitleRarity getRarity() {
        return rarity == null ? TitleRarity.COMMON : rarity;
    }

    public Component displayName() {
        return Component.translatable(getTranslationKey());
    }

    public MutableComponent styledDisplayName() {
        return displayName().copy().withStyle(getRarity().getColor());
    }

    public ItemStack iconStack() {
        ResourceLocation itemId = ResourceLocation.tryParse(icon == null ? "" : icon);
        Item item = itemId == null ? Items.PAPER : BuiltInRegistries.ITEM.getOptional(itemId).orElse(Items.PAPER);
        return new ItemStack(item);
    }
}
