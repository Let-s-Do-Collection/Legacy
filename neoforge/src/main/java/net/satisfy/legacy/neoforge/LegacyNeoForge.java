package net.satisfy.legacy.neoforge;

import dev.architectury.platform.hooks.EventBusesHooks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.satisfy.legacy.Legacy;
import net.satisfy.legacy.neoforge.config.LegacyNeoForgeConfig;

@Mod(Legacy.MOD_ID)
public class LegacyNeoForge {
    public LegacyNeoForge(final IEventBus modEventBus, ModContainer modContainer) {
        EventBusesHooks.whenAvailable(Legacy.MOD_ID, IEventBus::start);
        modContainer.registerConfig(ModConfig.Type.CLIENT, LegacyNeoForgeConfig.CLIENT_CONFIG);
        modEventBus.addListener(LegacyNeoForgeConfig::onLoad);
        modEventBus.addListener(LegacyNeoForgeConfig::onReload);
        Legacy.init();
    }
}
