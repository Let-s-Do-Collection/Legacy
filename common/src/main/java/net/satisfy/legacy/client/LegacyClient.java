package net.satisfy.legacy.client;

import dev.architectury.event.events.client.ClientPlayerEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.satisfy.legacy.core.title.Title;
import net.satisfy.legacy.core.title.TitleManager;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Environment(EnvType.CLIENT)
public final class LegacyClient {
    private static boolean remoteRegistry;
    private LegacyClient() {
    }

    public static void init() {
        LegacyClientConfig.load();
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> resetClientState());
    }

    private static void resetClientState() {
        ClientTitleData.setSelf(Set.of(), "", null);
        ClientTitleData.setActiveTitles(Map.of(), Map.of());
        ClientTitleData.setProgress(Map.of());
        ClientMilestoneData.set(List.of());
        ClientJourneyData.set(List.of());
        if (remoteRegistry) {
            TitleManager.INSTANCE.replaceAll(List.of());
            remoteRegistry = false;
        }
    }

    public static void applyRemoteRegistry(List<Title> titles) {
        if (Minecraft.getInstance().getSingleplayerServer() == null) {
            TitleManager.INSTANCE.replaceAll(titles);
            remoteRegistry = true;
        }
    }
}
