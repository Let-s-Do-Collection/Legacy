package net.satisfy.legacy.core.trigger;

import net.minecraft.server.level.ServerPlayer;
import net.satisfy.legacy.core.title.Title;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;

public final class CustomTriggers {
    private static final Map<String, BiPredicate<ServerPlayer, Title>> REGISTRY = new ConcurrentHashMap<>();

    private CustomTriggers() {
    }

    public static void register(String id, BiPredicate<ServerPlayer, Title> predicate) {
        REGISTRY.put(id, predicate);
    }

    public static boolean isSatisfied(String id, ServerPlayer player, Title title) {
        if (id == null) {
            return false;
        }
        BiPredicate<ServerPlayer, Title> predicate = REGISTRY.get(id);
        return predicate != null && predicate.test(player, title);
    }
}
