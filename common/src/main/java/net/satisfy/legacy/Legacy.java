package net.satisfy.legacy;

import com.mojang.logging.LogUtils;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.satisfy.legacy.api.LegacyAPI;
import net.satisfy.legacy.core.journey.JourneyManager;
import net.satisfy.legacy.core.milestone.MilestoneManager;
import net.satisfy.legacy.core.title.TitleManager;
import net.satisfy.legacy.core.trigger.LegacyCollectors;
import net.satisfy.legacy.network.LegacyNetworking;
import net.satisfy.legacy.server.LegacyCommands;
import net.satisfy.legacy.server.TitleService;
import org.slf4j.Logger;

public final class Legacy {
    public static final String MOD_ID = "legacy";

    public static final Logger LOGGER = LogUtils.getLogger();

    private Legacy() {
    }

    public static ResourceLocation identifier(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void init() {
        ReloadListenerRegistry.register(PackType.SERVER_DATA, TitleManager.INSTANCE);
        ReloadListenerRegistry.register(PackType.SERVER_DATA, JourneyManager.INSTANCE);
        ReloadListenerRegistry.register(PackType.SERVER_DATA, MilestoneManager.INSTANCE);
        LegacyNetworking.init();

        PlayerEvent.PLAYER_JOIN.register(TitleService::onJoin);
        PlayerEvent.PLAYER_QUIT.register(TitleService::onQuit);
        TickEvent.SERVER_POST.register(TitleService::onServerTick);
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> LegacyCommands.register(dispatcher));

        LegacyCollectors.init();
        registerBuiltinTriggers();

        LOGGER.info("[{}] common init complete (reload listener, networking and server hooks registered)", MOD_ID);
    }

    private static void registerBuiltinTriggers() {
        LegacyAPI.registerCustomTrigger("legacy:fate",
                (player, title) -> player.getInventory().contains(new ItemStack(Items.NETHER_STAR)));
        LegacyAPI.registerCustomTrigger("legacy:sunrise", (player, title) -> {
            long timeOfDay = player.level().getDayTime() % 24000L;
            return timeOfDay >= 22000L && timeOfDay <= 23500L;
        });
    }
}
