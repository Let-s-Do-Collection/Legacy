package net.satisfy.legacy.core.trigger;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.StatType;
import net.satisfy.legacy.core.data.PlayerTitleData;
import net.satisfy.legacy.core.title.Title;
import net.satisfy.legacy.core.title.TitleRequirement;
import net.satisfy.legacy.core.title.TriggerType;

public final class TitleProgress {

    private TitleProgress() {
    }

    public static int target(Title title) {
        TriggerType trigger = title.getTrigger();
        if (trigger == TriggerType.MINECRAFT_STAT || trigger == TriggerType.EVENT) {
            return Math.max(1, title.requirement().amount);
        }
        return 1;
    }

    public static int current(ServerPlayer player, PlayerTitleData playerData, Title title, boolean unlocked) {
        int target = target(title);
        if (unlocked) {
            return target;
        }
        return switch (title.getTrigger()) {
            case MINECRAFT_STAT -> clamp(statValue(player, title.requirement()), target);
            case EVENT -> clamp(counterValue(playerData, title.requirement()), target);
            default -> 0;
        };
    }

    private static int clamp(int value, int target) {
        return Math.max(0, Math.min(value, target));
    }

    private static int counterValue(PlayerTitleData playerData, TitleRequirement req) {
        return req == null ? 0 : playerData.getCounter(req.counter);
    }

    private static int statValue(ServerPlayer player, TitleRequirement req) {
        if (req == null || req.stat == null) {
            return 0;
        }
        ResourceLocation typeId = ResourceLocation.tryParse(req.statType == null ? "minecraft:custom" : req.statType);
        ResourceLocation statId = ResourceLocation.tryParse(req.stat);
        if (typeId == null || statId == null) {
            return 0;
        }
        StatType<?> type = BuiltInRegistries.STAT_TYPE.getOptional(typeId).orElse(null);
        if (type == null) {
            return 0;
        }
        return valueOf(player, type, statId);
    }

    private static <T> int valueOf(ServerPlayer player, StatType<T> type, ResourceLocation id) {
        T obj = type.getRegistry().getOptional(id).orElse(null);
        return obj == null ? 0 : player.getStats().getValue(type.get(obj));
    }
}
