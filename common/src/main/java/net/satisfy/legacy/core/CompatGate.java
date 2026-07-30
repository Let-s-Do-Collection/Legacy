package net.satisfy.legacy.core;

import dev.architectury.platform.Platform;

import java.util.List;

/**
 * Gate for cross-mod (compat) definitions. A title, journey or milestone that lists
 * {@code required_mods} is only loaded when every listed mod is present.
 */
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
