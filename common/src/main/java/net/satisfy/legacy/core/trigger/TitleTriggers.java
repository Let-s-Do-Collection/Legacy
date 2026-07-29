package net.satisfy.legacy.core.trigger;

import com.mojang.logging.LogUtils;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.satisfy.legacy.Legacy;
import net.satisfy.legacy.core.data.PlayerTitleData;
import net.satisfy.legacy.core.title.Title;
import net.satisfy.legacy.core.title.TitleRequirement;
import org.slf4j.Logger;

public final class TitleTriggers {
    private static final Logger LOGGER = LogUtils.getLogger();

    private TitleTriggers() {
    }

    public static boolean isSatisfied(ServerPlayer player, PlayerTitleData playerData, Title title) {
        if (title.milestone) {
            return false;
        }
        return evaluate(player, playerData, title.getTrigger(), title.requirement(), title);
    }

    /**
     * Shared trigger engine used by titles, journeys and any future JSON-driven system.
     * {@code customTitle} may be {@code null} for non-title consumers (journeys) — custom
     * predicates that need a title simply won't fire in that context.
     */
    public static boolean evaluate(ServerPlayer player, PlayerTitleData playerData,
                                   net.satisfy.legacy.core.title.TriggerType type,
                                   TitleRequirement req, Title customTitle) {
        return switch (type) {
            case MINECRAFT_STAT -> statSatisfied(player, req);
            case EVENT -> eventSatisfied(playerData, req);
            case ADVANCEMENT -> advancementSatisfied(player, req);
            case CUSTOM -> CustomTriggers.isSatisfied(req.id, player, customTitle);
        };
    }

    private static boolean eventSatisfied(PlayerTitleData playerData, TitleRequirement req) {
        return req.counter != null && playerData.getCounter(req.counter) >= Math.max(1, req.amount);
    }

    private static boolean statSatisfied(ServerPlayer player, TitleRequirement req) {
        if (req.stat == null) {
            return false;
        }
        ResourceLocation typeId = ResourceLocation.tryParse(req.statType == null ? "minecraft:custom" : req.statType);
        ResourceLocation statId = ResourceLocation.tryParse(req.stat);
        if (typeId == null || statId == null) {
            return false;
        }
        StatType<?> statType = BuiltInRegistries.STAT_TYPE.getOptional(typeId).orElse(null);
        if (statType == null) {
            LOGGER.warn("[{}] Unknown stat_type '{}'", Legacy.MOD_ID, req.statType);
            return false;
        }
        Stat<?> stat = resolveStat(statType, statId);
        if (stat == null) {
            LOGGER.warn("[{}] Unknown stat '{}' for type '{}'", Legacy.MOD_ID, req.stat, req.statType);
            return false;
        }
        return player.getStats().getValue(stat) >= Math.max(1, req.amount);
    }

    private static <T> Stat<T> resolveStat(StatType<T> statType, ResourceLocation statId) {
        T value = statType.getRegistry().getOptional(statId).orElse(null);
        return value == null ? null : statType.get(value);
    }

    private static boolean advancementSatisfied(ServerPlayer player, TitleRequirement req) {
        if (req.advancement == null) {
            return false;
        }
        ResourceLocation id = ResourceLocation.tryParse(req.advancement);
        if (id == null) {
            return false;
        }
        AdvancementHolder holder = player.server.getAdvancements().get(id);
        return holder != null && player.getAdvancements().getOrStartProgress(holder).isDone();
    }
}
