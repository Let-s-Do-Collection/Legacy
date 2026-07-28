package net.satisfy.legacy.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class LegacyRenderState {
    public static boolean renderingInventoryEntity = false;

    private LegacyRenderState() {
    }
}
