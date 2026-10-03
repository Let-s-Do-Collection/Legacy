package net.satisfy.legacy.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

@Environment(EnvType.CLIENT)
public final class ClientJourneyData {
    private static List<Entry> entries = List.of();

    private ClientJourneyData() {
    }

    public record Entry(String id, String category, String icon, String translationKey, String title,
                        String description, boolean done, int day) {
        public ItemStack iconStack() {
            ResourceLocation itemId = ResourceLocation.tryParse(icon == null ? "" : icon);
            Item item = itemId == null ? Items.PAPER : BuiltInRegistries.ITEM.getOptional(itemId).orElse(Items.PAPER);
            return new ItemStack(item);
        }

        public String titleString() {
            if (translationKey != null && !translationKey.isEmpty() && I18n.exists(translationKey)) {
                return I18n.get(translationKey);
            }
            return title;
        }

        public String descriptionString() {
            if (translationKey != null && !translationKey.isEmpty() && I18n.exists(translationKey + ".desc")) {
                return I18n.get(translationKey + ".desc");
            }
            return description;
        }
    }

    public static void set(List<Entry> list) {
        entries = list == null ? List.of() : list;
    }

    public static List<Entry> all() {
        return entries;
    }
}
