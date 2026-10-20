package net.satisfy.legacy.neoforge.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.satisfy.foundation.client.gui.LetsDoConfigScreen;
import net.satisfy.legacy.Legacy;

@Mod(value = Legacy.MOD_ID, dist = Dist.CLIENT)
public class LegacyNeoForgeClientMod {
    public LegacyNeoForgeClientMod(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, parent) -> new LetsDoConfigScreen(parent, Legacy.MOD_ID, screen -> new ConfigurationScreen(modContainer, screen)));
    }
}
