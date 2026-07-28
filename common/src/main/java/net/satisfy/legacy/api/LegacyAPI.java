package net.satisfy.legacy.api;

import net.minecraft.server.level.ServerPlayer;
import net.satisfy.legacy.core.data.LegacyTitleSavedData;
import net.satisfy.legacy.core.data.PlayerTitleData;
import net.satisfy.legacy.core.title.Title;
import net.satisfy.legacy.core.title.TitleManager;
import net.satisfy.legacy.core.trigger.CustomTriggers;
import net.satisfy.legacy.network.LegacyNetworking;
import net.satisfy.legacy.server.TitleService;

import java.util.function.BiPredicate;

public final class LegacyAPI {
    private LegacyAPI() {
    }

    public static void registerCustomTrigger(String id, BiPredicate<ServerPlayer, Title> predicate) {
        CustomTriggers.register(id, predicate);
    }

    public static boolean grant(ServerPlayer player, String titleId) {
        if (!TitleManager.INSTANCE.has(titleId)) {
            return false;
        }
        LegacyTitleSavedData data = LegacyTitleSavedData.get(player.server);
        PlayerTitleData playerData = data.getOrCreate(player.getUUID());
        if (!playerData.unlock(titleId)) {
            return false;
        }
        data.setDirty();
        LegacyNetworking.sendUnlockToast(player, titleId);
        LegacyNetworking.sendSelf(player, playerData);
        return true;
    }

    public static boolean revoke(ServerPlayer player, String titleId) {
        LegacyTitleSavedData data = LegacyTitleSavedData.get(player.server);
        PlayerTitleData playerData = data.getOrCreate(player.getUUID());
        if (!playerData.getUnlocked().remove(titleId)) {
            return false;
        }
        if (titleId.equals(playerData.getActive())) {
            playerData.setActive("");
            TitleService.broadcastActiveTitles(player.server);
        }
        data.setDirty();
        LegacyNetworking.sendSelf(player, playerData);
        return true;
    }
}
