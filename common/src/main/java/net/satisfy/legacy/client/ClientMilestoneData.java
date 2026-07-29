package net.satisfy.legacy.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.satisfy.legacy.core.title.TitleRarity;

import java.util.List;

@Environment(EnvType.CLIENT)
public final class ClientMilestoneData {
    public static final int UNCLAIMED = 0;
    public static final int PRE_EXISTING = 1;
    public static final int CLAIMED = 2;

    private static volatile List<Entry> entries = List.of();

    private ClientMilestoneData() {
    }

    public static void set(List<Entry> list) {
        entries = list;
    }

    public static List<Entry> all() {
        return entries;
    }

    public record Entry(String id, String translationKey, String icon, int rarity, boolean coop,
                        int state, String firstName, int worldDay, String time, String date, boolean owned,
                        List<String> recipients) {
        public TitleRarity rarityValue() {
            return TitleRarity.byOrdinal(rarity);
        }

        public String nameKey() {
            return translationKey;
        }

        public ItemStack iconStack() {
            ResourceLocation itemId = ResourceLocation.tryParse(icon == null ? "" : icon);
            Item item = itemId == null ? Items.PAPER : BuiltInRegistries.ITEM.getOptional(itemId).orElse(Items.PAPER);
            return new ItemStack(item);
        }
    }
}
