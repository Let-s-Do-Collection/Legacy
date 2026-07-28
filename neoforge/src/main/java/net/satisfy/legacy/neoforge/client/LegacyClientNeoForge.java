package net.satisfy.legacy.neoforge.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.satisfy.legacy.Legacy;
import net.satisfy.legacy.client.LegacyClient;

@EventBusSubscriber(modid = Legacy.MOD_ID, value = Dist.CLIENT)
public class LegacyClientNeoForge {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        LegacyClient.init();
    }
}
