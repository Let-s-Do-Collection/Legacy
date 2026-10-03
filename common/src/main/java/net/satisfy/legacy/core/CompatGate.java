package net.satisfy.legacy.core;

import dev.architectury.platform.Platform;

import java.util.List;

public final class CompatGate {
    private CompatGate() {
    }

    public static boolean present(List<String> requiredMods) {
        if (requiredMods == null || requiredMods.isEmpty()) {
            return true;
        }
        for (String id : requiredMods) {
            if (id != null && !id.isBlank() && !Platform.isModLoaded(id)) {
                return false;
            }
        }
        return true;
    }
}
