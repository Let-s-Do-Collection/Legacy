package net.satisfy.legacy.client;

import dev.architectury.event.events.client.ClientPlayerEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.satisfy.legacy.core.title.TitleManager;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Environment(EnvType.CLIENT)
public final class LegacyClient {
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
        if (Minecraft.getInstance().getSingleplayerServer() == null) {
            TitleManager.INSTANCE.replaceAll(List.of());
        }
    }
}
