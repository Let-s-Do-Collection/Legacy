package net.satisfy.legacy.neoforge;

import dev.architectury.platform.hooks.EventBusesHooks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.satisfy.legacy.Legacy;

@Mod(Legacy.MOD_ID)
public class LegacyNeoForge {
    public LegacyNeoForge(final IEventBus modEventBus, ModContainer modContainer) {
        EventBusesHooks.whenAvailable(Legacy.MOD_ID, IEventBus::start);
        Legacy.init();
    }
}
