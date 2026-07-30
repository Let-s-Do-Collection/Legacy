package net.satisfy.legacy.core.journey;

import com.google.gson.annotations.SerializedName;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.satisfy.legacy.core.title.TitleRequirement;
import net.satisfy.legacy.core.title.TriggerType;
import net.satisfy.legacy.core.trigger.Trigger;

public class Journey {
    @SerializedName("id")
    public String id;

    @SerializedName("category")
    public String category = "misc";

    @SerializedName("icon")
    public String icon = "minecraft:paper";

    /** Optional language key. When set, the client localises the name/description via {@code <key>} and {@code <key>.desc}. */
    @SerializedName("translation_key")
    public String translationKey;

    @SerializedName("title")
    public String title = "";

    @SerializedName("description")
    public String description = "";

    @SerializedName("trigger")
    public Trigger trigger = new Trigger();

    /** Only load this journey when every listed mod is present. */
    @SerializedName("required_mods")
    public java.util.List<String> requiredMods;

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category == null || category.isBlank() ? "misc" : category;
    }

    public String translationKey() {
        return translationKey == null || translationKey.isBlank() ? "" : translationKey;
    }

    public String title() {
        return title == null || title.isBlank() ? id : title;
    }

    public String description() {
        return description == null ? "" : description;
    }

    public TriggerType triggerType() {
        return trigger == null ? TriggerType.CUSTOM : trigger.type();
    }

    public TitleRequirement requirement() {
        return trigger == null ? new TitleRequirement() : trigger.requirement();
    }

    public ItemStack iconStack() {
        ResourceLocation itemId = ResourceLocation.tryParse(icon == null ? "" : icon);
        Item item = itemId == null ? Items.PAPER : BuiltInRegistries.ITEM.getOptional(itemId).orElse(Items.PAPER);
        return new ItemStack(item);
    }
}
