package net.satisfy.legacy.core.title;

import com.google.gson.annotations.SerializedName;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.satisfy.legacy.core.trigger.Trigger;

public class Title {
    @SerializedName("id")
    public String id;

    @SerializedName("translation_key")
    public String translationKey;

    /** Optional literal name, for datapack authors who do not need localisation or gendered forms. */
    @SerializedName("title")
    public String title;

    @SerializedName("placement")
    public Placement placement = Placement.SUFFIX;

    @SerializedName("trigger")
    public Trigger trigger = new Trigger();

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

    public boolean hasExplicitTranslationKey() {
        return translationKey != null && !translationKey.isBlank();
    }

    /** The literal name, or {@code null} when this title relies on {@code translation_key}. */
    public String literalName() {
        return title == null || title.isBlank() ? null : title;
    }

    public Placement getPlacement() {
        return placement == null ? Placement.SUFFIX : placement;
    }

    public TriggerType getTrigger() {
        return trigger == null ? TriggerType.CUSTOM : trigger.type();
    }

    public TitleRequirement requirement() {
        return trigger == null ? new TitleRequirement() : trigger.requirement();
    }

    public TitleRarity getRarity() {
        return rarity == null ? TitleRarity.COMMON : rarity;
    }

    public Component displayName() {
        if (!hasExplicitTranslationKey() && literalName() != null) {
            return Component.literal(literalName());
        }
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
