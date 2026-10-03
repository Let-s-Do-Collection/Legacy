package net.satisfy.legacy.server;

import net.minecraft.server.level.ServerPlayer;
import net.satisfy.legacy.core.data.PlayerTitleData;
import net.satisfy.legacy.core.journey.Journey;
import net.satisfy.legacy.core.journey.JourneyManager;
import net.satisfy.legacy.core.trigger.TitleTriggers;
import net.satisfy.legacy.network.LegacyNetworking;

public final class JourneyService {
    private JourneyService() {
    }

    public static boolean evaluate(ServerPlayer player, PlayerTitleData playerData) {
        int worldDay = (int) (player.server.overworld().getDayTime() / 24000L);
        boolean changed = false;
        for (Journey journey : JourneyManager.INSTANCE.all()) {
            if (playerData.isJourneyDone(journey.getId())) {
                continue;
            }
            if (TitleTriggers.evaluate(player, playerData, journey.triggerType(), journey.requirement(), null)
                    && playerData.completeJourney(journey.getId(), worldDay)) {
                changed = true;
            }
        }
        return changed;
    }

    public static void sync(ServerPlayer player, PlayerTitleData playerData) {
        LegacyNetworking.sendJourneys(player, playerData);
    }
}
