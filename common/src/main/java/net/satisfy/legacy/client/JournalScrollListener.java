package net.satisfy.legacy.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;

@Environment(EnvType.CLIENT)
public class JournalScrollListener implements GuiEventListener, NarratableEntry {
    private final JournalPanel journal;

    public JournalScrollListener(JournalPanel journal) {
        this.journal = journal;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return journal.isOverList(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return journal.scroll(scrollY);
    }

    @Override
    public void setFocused(boolean focused) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }
}
