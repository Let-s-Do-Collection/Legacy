package net.satisfy.legacy.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.satisfy.legacy.Legacy;
import net.satisfy.legacy.core.data.LegacyTitleSavedData;
import net.satisfy.legacy.core.data.PlayerTitleData;
import net.satisfy.legacy.core.title.Title;
import net.satisfy.legacy.core.title.TitleForm;
import net.satisfy.legacy.core.title.TitleManager;
import net.satisfy.legacy.core.trigger.LegacyCollectors;
import net.satisfy.legacy.core.trigger.TitleProgress;
import net.satisfy.legacy.core.trigger.TitleTriggers;
import net.satisfy.legacy.network.LegacyNetworking;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class TitleService {
    private static final int EVALUATION_INTERVAL = 40;

    private static int tickCounter;

    private TitleService() {
    }

    public static void onJoin(ServerPlayer player) {
        MinecraftServer server = player.server;
        LegacyTitleSavedData data = LegacyTitleSavedData.get(server);
        PlayerTitleData playerData = data.getOrCreate(player.getUUID());

        LegacyCollectors.onJoin(player, playerData);
        evaluate(player, playerData, false);
        data.setDirty();

        LegacyNetworking.sendRegistry(player);
        LegacyNetworking.sendSelf(player, playerData);
        sendProgress(player, playerData);
        broadcastActiveTitles(server);
    }

    public static void onServerTick(MinecraftServer server) {
        if (++tickCounter < EVALUATION_INTERVAL) {
            return;
        }
        tickCounter = 0;

        LegacyTitleSavedData data = LegacyTitleSavedData.get(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlayerTitleData playerData = data.getOrCreate(player.getUUID());
            try {
                LegacyCollectors.sampleLocation(player, playerData);
            } catch (Exception e) {
                Legacy.LOGGER.error("[{}] Location sampling failed for {}", Legacy.MOD_ID, player.getName().getString(), e);
            }
            if (evaluate(player, playerData, true)) {
                LegacyNetworking.sendSelf(player, playerData);
            }
            sendProgress(player, playerData);
        }
        data.setDirty();
    }

    private static void sendProgress(ServerPlayer player, PlayerTitleData playerData) {
        Map<String, Integer> progress = new LinkedHashMap<>();
        for (Title title : TitleManager.INSTANCE.all()) {
            progress.put(title.getId(), TitleProgress.current(player, playerData, title, playerData.isUnlocked(title.getId())));
        }
        LegacyNetworking.sendProgress(player, progress);
    }

    public static int forceEvaluate(ServerPlayer player) {
        LegacyTitleSavedData data = LegacyTitleSavedData.get(player.server);
        PlayerTitleData playerData = data.getOrCreate(player.getUUID());
        int before = playerData.getUnlocked().size();
        if (evaluate(player, playerData, true)) {
            data.setDirty();
            LegacyNetworking.sendSelf(player, playerData);
        }
        return playerData.getUnlocked().size() - before;
    }

    public static void setActive(ServerPlayer player, String titleId) {
        LegacyTitleSavedData data = LegacyTitleSavedData.get(player.server);
        PlayerTitleData playerData = data.getOrCreate(player.getUUID());

        String normalized = titleId == null ? "" : titleId;
        if (!normalized.isEmpty() && !playerData.isUnlocked(normalized)) {
            LegacyNetworking.sendSelf(player, playerData);
            return;
        }

        playerData.setActive(normalized);
        data.setDirty();

        LegacyNetworking.sendSelf(player, playerData);
        broadcastActiveTitles(player.server);
    }

    private static boolean evaluate(ServerPlayer player, PlayerTitleData playerData, boolean announce) {
        boolean changed = false;
        for (Title title : TitleManager.INSTANCE.all()) {
            if (playerData.isUnlocked(title.getId())) {
                continue;
            }
            if (TitleTriggers.isSatisfied(player, playerData, title)) {
                playerData.unlock(title.getId());
                changed = true;
                if (announce) {
                    LegacyNetworking.sendUnlockToast(player, title.getId());
                }
            }
        }
        return changed;
    }

    public static int reloadTitles(MinecraftServer server) {
        int count = TitleManager.INSTANCE.reload(server.getResourceManager());
        LegacyTitleSavedData data = LegacyTitleSavedData.get(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlayerTitleData playerData = data.getOrCreate(player.getUUID());
            evaluate(player, playerData, false);
            LegacyNetworking.sendRegistry(player);
            LegacyNetworking.sendSelf(player, playerData);
        }
        data.setDirty();
        broadcastActiveTitles(server);
        return count;
    }

    public static void setForm(ServerPlayer player, TitleForm form) {
        LegacyTitleSavedData data = LegacyTitleSavedData.get(player.server);
        PlayerTitleData playerData = data.getOrCreate(player.getUUID());
        playerData.setForm(form);
        data.setDirty();
        LegacyNetworking.sendSelf(player, playerData);
        broadcastActiveTitles(player.server);
    }

    public static void broadcastActiveTitles(MinecraftServer server) {
        LegacyTitleSavedData data = LegacyTitleSavedData.get(server);
        Map<UUID, String> active = new LinkedHashMap<>();
        Map<UUID, Integer> forms = new LinkedHashMap<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlayerTitleData playerData = data.getOrCreate(player.getUUID());
            String activeId = playerData.getActive();
            if (activeId != null && !activeId.isEmpty()) {
                active.put(player.getUUID(), activeId);
                forms.put(player.getUUID(), playerData.getForm().ordinal());
            }
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            LegacyNetworking.sendActiveBroadcast(player, active, forms);
        }
    }
}
