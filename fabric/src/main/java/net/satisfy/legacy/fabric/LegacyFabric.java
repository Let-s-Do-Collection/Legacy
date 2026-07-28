package net.satisfy.legacy.fabric;

import net.fabricmc.api.ModInitializer;
import net.satisfy.legacy.Legacy;

public class LegacyFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Legacy.init();
    }
}
