package net.satisfy.legacy.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.satisfy.legacy.client.LegacyClient;

public class LegacyClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LegacyClient.init();
    }
}
