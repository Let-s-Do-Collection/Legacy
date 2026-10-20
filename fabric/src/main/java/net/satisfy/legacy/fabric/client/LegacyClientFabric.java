package net.satisfy.legacy.fabric.client;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.satisfy.legacy.client.LegacyClient;
import net.satisfy.legacy.fabric.config.LegacyFabricConfig;

public class LegacyClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AutoConfig.register(LegacyFabricConfig.class, GsonConfigSerializer::new);
        LegacyClient.init();
    }
}
