package net.satisfy.legacy.core.milestone;

import com.google.gson.annotations.SerializedName;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.satisfy.legacy.core.title.TitleRarity;
import net.satisfy.legacy.core.trigger.Trigger;

public class Milestone {
    @SerializedName("id")
    public String id;

    @SerializedName("translation_key")
    public String translationKey;

    /** A shared milestone can also be earned by others within {@link #gracePeriod}. */
    @SerializedName(value = "shared", alternate = {"coop"})
    public boolean coop = false;

    @SerializedName("trigger")
    public Trigger trigger = new Trigger();

    @SerializedName("grace_period")
    public int gracePeriod = 300;

    @SerializedName("retro_advancement")
    public String retroAdvancement;

    @SerializedName("icon")
    public String icon = "minecraft:paper";

    @SerializedName("rarity")
    public TitleRarity rarity = TitleRarity.EPIC;

    /**
     * Milestone trigger kind — {@code dimension}, {@code kill} or {@code advancement}. Uses the
     * same {@code trigger} block as titles and journeys; only the accepted {@code type} values differ.
     */
    public String triggerKind() {
        return trigger == null || trigger.type == null ? "advancement" : trigger.type;
    }

    public String triggerValue() {
        return trigger == null || trigger.id == null ? "" : trigger.id;
    }

    /** Grace window in game ticks (20 ticks = 1 second). */
    public long graceTicks() {
        return Math.max(0, gracePeriod) * 20L;
    }

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
        return "advancement".equals(triggerKind()) ? triggerValue() : null;
    }

    public ItemStack iconStack() {
        ResourceLocation itemId = ResourceLocation.tryParse(icon == null ? "" : icon);
        Item item = itemId == null ? Items.PAPER : BuiltInRegistries.ITEM.getOptional(itemId).orElse(Items.PAPER);
        return new ItemStack(item);
    }
}
