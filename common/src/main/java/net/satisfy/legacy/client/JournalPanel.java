package net.satisfy.legacy.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.satisfy.legacy.core.title.Title;
import net.satisfy.legacy.core.title.TitleForm;
import net.satisfy.legacy.core.title.TitleManager;
import net.satisfy.legacy.network.LegacyNetworking;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class JournalPanel {
    private enum Tab { TITLES, JOURNAL }

    private static final ResourceLocation BUTTON = ResourceLocation.fromNamespaceAndPath("legacy", "widget/journal_button");
    private static final ResourceLocation BUTTON_HL = ResourceLocation.fromNamespaceAndPath("legacy", "widget/journal_button_highlighted");
    private static final ResourceLocation BG = ResourceLocation.withDefaultNamespace("textures/gui/recipe_book.png");
    private static final ResourceLocation TAB = ResourceLocation.withDefaultNamespace("recipe_book/tab");
    private static final ResourceLocation TAB_SELECTED = ResourceLocation.withDefaultNamespace("recipe_book/tab_selected");

    private static final ResourceLocation FILTER_ALL = ResourceLocation.fromNamespaceAndPath("legacy", "widget/filter_all");
    private static final ResourceLocation FILTER_ALL_HL = ResourceLocation.fromNamespaceAndPath("legacy", "widget/filter_all_highlighted");
    private static final ResourceLocation FILTER_OBTAINED = ResourceLocation.fromNamespaceAndPath("legacy", "widget/filter_enabled");
    private static final ResourceLocation FILTER_OBTAINED_HL = ResourceLocation.fromNamespaceAndPath("legacy", "widget/filter_enabled_highlighted");

    private static final int BG_W = 147;
    private static final int BG_H = 166;
    private static final int TAB_W = 35;
    private static final int TAB_H = 27;
    private static final int ROW_HEIGHT = 19;
    private static final int BAR_W = 40;
    private static final int BAR_H = 5;

    private boolean visible;
    private boolean showAll;
    private int scrollOffset;
    private Tab tab = Tab.TITLES;
    private EditBox search;
    private final List<Entry> entries = new ArrayList<>();

    private int iconX, iconY;
    private int panelX, panelY;
    private int contentLeft, contentRight, listTop, listBottom;
    private int filterX, filterY;
    private int formX, formY;
    private int firstLockedIndex, firstHiddenIndex;

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean v) {
        this.visible = v;
        if (!v && search != null) {
            search.setFocused(false);
        }
    }

    public boolean isOverButton(double mx, double my) {
        return mx >= iconX && mx < iconX + 20 && my >= iconY && my < iconY + 18;
    }

    public boolean isOverList(double mx, double my) {
        return visible && mx >= contentLeft && mx < contentRight && my >= listTop && my < listBottom;
    }

    public boolean scroll(double scrollY) {
        int maxScroll = Math.max(0, entries.size() * ROW_HEIGHT - (listBottom - listTop));
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - scrollY * ROW_HEIGHT));
        return true;
    }

    public EditBox getOrCreateSearch(Font font) {
        if (search == null) {
            search = new EditBox(font, 0, 0, 81, 9 + 5, Component.translatable("gui.legacy.search"));
            search.setMaxLength(50);
            search.setBordered(true);
            search.setHint(Component.translatable("gui.legacy.search").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
            search.setResponder(s -> rebuildEntries());
        }
        return search;
    }

    public boolean isSearchFocused() {
        return search != null && search.isFocused();
    }

    public void unfocusSearch() {
        if (search != null) {
            search.setFocused(false);
        }
    }

    public boolean searchKeyPressed(int keyCode, int scanCode, int modifiers) {
        return search != null && search.keyPressed(keyCode, scanCode, modifiers);
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick, Font font,
                       int leftPos, int topPos, int imageWidth, int imageHeight) {
        this.iconX = leftPos + 125;
        this.iconY = topPos + 61;
        boolean overIcon = isOverButton(mouseX, mouseY);
        g.blitSprite(overIcon || visible ? BUTTON_HL : BUTTON, iconX, iconY, 20, 18);

        if (!visible) {
            return;
        }

        this.panelX = leftPos - BG_W - 2;
        this.panelY = topPos;
        this.contentLeft = panelX + 8;
        this.contentRight = panelX + BG_W - 8;
        this.listTop = panelY + 30;
        this.listBottom = panelY + BG_H - 8;

        g.blit(BG, panelX, panelY, 1, 1, BG_W, BG_H);
        renderTabs(g);

        if (search != null) {
            search.setPosition(panelX + 25, panelY + 13);
            search.setWidth(81);
            search.setVisible(true);
        }

        if (tab == Tab.TITLES) {
            renderTitles(g, font, mouseX, mouseY);
        } else {
            renderJournal(g, font, mouseX, mouseY);
        }

        if (search != null) {
            search.render(g, mouseX, mouseY, partialTick);
        }
        if (tab == Tab.TITLES) {
            renderFilterButton(g, font, mouseX, mouseY);
            renderFormToggle(g, font, mouseX, mouseY);
        }
    }

    private void renderFormToggle(GuiGraphics g, Font font, int mouseX, int mouseY) {
        this.formX = contentRight - 12;
        this.formY = panelY + 1;
        boolean hov = mouseX >= formX && mouseX < formX + 12 && mouseY >= formY && mouseY < formY + 11;
        g.fill(formX, formY, formX + 12, formY + 11, hov ? 0xFF6E5A3C : 0xFF2A1F12);
        String glyph = ClientTitleData.getForm() == TitleForm.FEMININE ? "♀" : "♂";
        g.drawString(font, glyph, formX + (12 - font.width(glyph)) / 2, formY + 2, 0xFFE8D8B0, false);
        if (hov) {
            g.renderComponentTooltip(font, java.util.List.of(
                    Component.translatable("gui.legacy.form").withStyle(ChatFormatting.WHITE),
                    Component.translatable("gui.legacy.form.desc").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
        }
    }

    private boolean overForm(double mx, double my) {
        return mx >= formX && mx < formX + 12 && my >= formY && my < formY + 11;
    }

    private void renderTabs(GuiGraphics g) {
        ItemStack[] icons = { new ItemStack(Items.NAME_TAG), new ItemStack(Items.WRITABLE_BOOK) };
        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            boolean sel = tab == tabs[i];
            int y = tabY(i);
            g.blitSprite(sel ? TAB_SELECTED : TAB, tabX(sel), y, TAB_W, TAB_H);
            g.renderFakeItem(icons[i], panelX - 24, y + 5);
        }
    }

    private int tabX(boolean selected) {
        return panelX - (selected ? 32 : 30);
    }

    private int tabY(int i) {
        return panelY + 3 + i * TAB_H;
    }

    private boolean overTab(double mx, double my, int i) {
        boolean sel = tab == Tab.values()[i];
        int y = tabY(i);
        return mx >= tabX(sel) && mx < tabX(sel) + TAB_W && my >= y && my < y + TAB_H;
    }

    private void renderFilterButton(GuiGraphics g, Font font, int mouseX, int mouseY) {
        this.filterX = panelX + 110;
        this.filterY = panelY + 12;
        boolean hov = mouseX >= filterX && mouseX < filterX + 26 && mouseY >= filterY && mouseY < filterY + 16;
        ResourceLocation sprite = showAll
                ? (hov ? FILTER_ALL_HL : FILTER_ALL)
                : (hov ? FILTER_OBTAINED_HL : FILTER_OBTAINED);
        g.blitSprite(sprite, filterX, filterY, 26, 16);
        if (hov) {
            g.renderTooltip(font, Component.translatable(showAll ? "gui.legacy.filter.all" : "gui.legacy.filter.obtained"), mouseX, mouseY);
        }
    }

    private boolean overFilter(double mx, double my) {
        return mx >= filterX && mx < filterX + 26 && my >= filterY && my < filterY + 16;
    }

    private void renderTitles(GuiGraphics g, Font font, int mouseX, int mouseY) {
        rebuildEntries();
        clampScroll();
        String active = ClientTitleData.getActive();
        List<Component> tooltip = null;
        g.enableScissor(contentLeft, listTop, contentRight, listBottom);
        for (int i = 0; i < entries.size(); i++) {
            int rowY = listTop - scrollOffset + i * ROW_HEIGHT;
            if (rowY + ROW_HEIGHT <= listTop || rowY >= listBottom) {
                continue;
            }
            Entry e = entries.get(i);
            boolean secret = e.title != null && !e.unlocked && e.title.hidden;
            boolean hov = hovered(mouseX, mouseY, rowY);
            if (hov) {
                g.fill(contentLeft, rowY, contentRight, rowY + ROW_HEIGHT, 0x33201810);
                if (secret) {
                    tooltip = List.of(
                            Component.literal("???").withStyle(ChatFormatting.DARK_GRAY),
                            Component.translatable("gui.legacy.title.undiscovered").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
                } else if (e.title != null && !e.unlocked) {
                    tooltip = List.of(
                            TitleNames.styled(e.title, ClientTitleData.getForm()),
                            Component.translatable(e.title.getTranslationKey() + ".desc").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
                }
            }
            boolean isActive = e.title == null ? (active == null || active.isEmpty()) : e.title.getId().equals(active);
            if (isActive) {
                g.drawString(font, Component.literal("✔"), contentLeft + 4, rowY + 5, 0x2E8B2E, false);
            }
            if (e.title != null && !secret) {
                g.renderFakeItem(e.title.iconStack(), contentLeft + 14, rowY + 1);
                if (!e.unlocked) {
                    g.fill(contentLeft + 14, rowY + 1, contentLeft + 30, rowY + 17, 0xA0202020);
                }
            }
            Component name;
            if (e.title == null) {
                name = Component.translatable("gui.legacy.no_title");
            } else if (secret) {
                name = Component.literal("???").withStyle(ChatFormatting.DARK_GRAY);
            } else if (e.unlocked) {
                name = TitleNames.styled(e.title, ClientTitleData.getForm());
            } else {
                name = TitleNames.plain(e.title, ClientTitleData.getForm()).withStyle(ChatFormatting.DARK_GRAY);
            }
            g.drawString(font, name, secret ? contentLeft + 14 : contentLeft + 34, rowY + 5, 0xFFFFFF, true);
        }
        drawGroupDivider(g, firstLockedIndex);
        if (firstHiddenIndex > firstLockedIndex) {
            drawGroupDivider(g, firstHiddenIndex);
        }
        g.disableScissor();
        if (tooltip != null) {
            g.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }
    }

    private void renderJournal(GuiGraphics g, Font font, int mouseX, int mouseY) {
        rebuildEntries();
        clampScroll();
        List<Component> tooltip = null;
        int barX = contentRight - BAR_W - 2;
        g.enableScissor(contentLeft, listTop, contentRight, listBottom);
        for (int i = 0; i < entries.size(); i++) {
            int rowY = listTop - scrollOffset + i * ROW_HEIGHT;
            if (rowY + ROW_HEIGHT <= listTop || rowY >= listBottom) {
                continue;
            }
            Entry e = entries.get(i);
            boolean secret = e.title != null && !e.unlocked && e.title.hidden;
            boolean hov = hovered(mouseX, mouseY, rowY);
            if (hov) {
                g.fill(contentLeft, rowY, contentRight, rowY + ROW_HEIGHT, 0x33201810);
                tooltip = journalTooltip(e, secret);
            }
            if (secret) {
                g.fill(contentLeft, rowY + 1, contentLeft + 16, rowY + 17, 0xFF0B0B0B);
                g.fill(contentLeft, rowY + 1, contentLeft + 16, rowY + 2, 0xFF000000);
                g.fill(contentLeft, rowY + 16, contentLeft + 16, rowY + 17, 0xFF000000);
                g.drawString(font, Component.literal("?"), contentLeft + 6, rowY + 5, 0xFF3A3A3A, false);
                g.drawString(font, Component.literal("???").withStyle(ChatFormatting.DARK_GRAY), contentLeft + 20, rowY + 5, 0xFFFFFF, true);
                continue;
            }
            Title t = e.title;
            g.renderFakeItem(t.iconStack(), contentLeft, rowY + 1);
            int nameLeft = contentLeft + 20;
            int nameRight = e.unlocked ? barX - 11 : barX - 3;
            int nameColor;
            if (e.unlocked) {
                Integer c = t.getRarity().getColor().getColor();
                nameColor = c == null ? 0xFFFFFF : c;
            } else {
                nameColor = 0xAAAAAA;
            }
            g.drawString(font, trimToWidth(font, TitleNames.string(t, ClientTitleData.getForm()), nameRight - nameLeft), nameLeft, rowY + 5, nameColor, true);

            int target = Math.max(1, t.syncedTarget);
            int current = e.unlocked ? target : Math.min(ClientTitleData.getProgress(t.getId()), target);
            float frac = Math.max(0f, Math.min(1f, current / (float) target));
            int barY = rowY + 7;
            if (e.unlocked) {
                g.drawString(font, Component.literal("✔"), barX - 9, rowY + 5, 0x6DC257, false);
            }
            g.fill(barX - 1, barY - 1, barX + BAR_W + 1, barY + BAR_H + 1, 0xFF2A1F12);
            g.fill(barX, barY, barX + BAR_W, barY + BAR_H, 0xFF5A4A34);
            int fillW = Math.round(BAR_W * frac);
            int color = e.unlocked ? 0xFF6DC257 : (frac >= 0.9f ? 0xFFB4DE72 : 0xFF8FB35C);
            if (fillW > 0) {
                g.fill(barX, barY, barX + fillW, barY + BAR_H, color);
            }
        }
        g.disableScissor();
        if (tooltip != null) {
            g.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }
    }

    private List<Component> journalTooltip(Entry e, boolean secret) {
        if (secret) {
            return List.of(
                    Component.literal("???").withStyle(ChatFormatting.DARK_GRAY),
                    Component.translatable("gui.legacy.journal.undiscovered").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
        }
        Title t = e.title;
        TitleForm form = ClientTitleData.getForm();
        List<Component> lines = new ArrayList<>();
        lines.add(TitleNames.styled(t, form));
        lines.add(Component.translatable(t.getTranslationKey() + ".desc").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
        lines.add(Component.empty());
        if (e.unlocked) {
            lines.add(Component.translatable("gui.legacy.completed").withStyle(ChatFormatting.GREEN));
            lines.add(Component.empty());
            lines.add(Component.translatable("gui.legacy.reward").withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("gui.legacy.reward.unlocked").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        } else {
            int target = Math.max(1, t.syncedTarget);
            int current = Math.min(ClientTitleData.getProgress(t.getId()), target);
            lines.add(Component.translatable("gui.legacy.progress").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal(current + " / " + target + " ")
                    .append(Component.translatable(t.progressKey())).withStyle(ChatFormatting.WHITE));
            lines.add(Component.empty());
            lines.add(Component.translatable("gui.legacy.reward").withStyle(ChatFormatting.GRAY));
            lines.add(TitleNames.styled(t, form).withStyle(ChatFormatting.BOLD));
        }
        return lines;
    }

    private static String trimToWidth(Font font, String text, int maxWidth) {
        if (maxWidth <= 0) {
            return "";
        }
        if (font.width(text) <= maxWidth) {
            return text;
        }
        int ellipsis = font.width("…");
        return font.plainSubstrByWidth(text, Math.max(0, maxWidth - ellipsis)) + "…";
    }

    private boolean hovered(int mouseX, int mouseY, int rowY) {
        return mouseX >= contentLeft && mouseX < contentRight && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT
                && mouseY >= listTop && mouseY < listBottom;
    }

    private void clampScroll() {
        int maxScroll = Math.max(0, entries.size() * ROW_HEIGHT - (listBottom - listTop));
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
    }

    private void drawGroupDivider(GuiGraphics g, int boundaryIndex) {
        if (boundaryIndex <= 0 || boundaryIndex >= entries.size()) {
            return;
        }
        int y = listTop - scrollOffset + boundaryIndex * ROW_HEIGHT - 1;
        if (y > listTop && y < listBottom) {
            g.fill(contentLeft, y, contentRight, y + 1, 0x50201810);
        }
    }

    public boolean clickContent(double mouseX, double mouseY, int button) {
        if (button != 0 || !visible) {
            return false;
        }
        for (int i = 0; i < Tab.values().length; i++) {
            if (overTab(mouseX, mouseY, i)) {
                tab = Tab.values()[i];
                scrollOffset = 0;
                unfocusSearch();
                return true;
            }
        }
        if (tab == Tab.TITLES && overForm(mouseX, mouseY)) {
            TitleForm next = ClientTitleData.getForm().toggled();
            ClientTitleData.setFormLocally(next);
            LegacyNetworking.sendSetForm(next);
            unfocusSearch();
            return true;
        }
        if (tab == Tab.TITLES && overFilter(mouseX, mouseY)) {
            showAll = !showAll;
            unfocusSearch();
            return true;
        }
        if (search != null && search.isVisible() && search.isMouseOver(mouseX, mouseY)) {
            return false;
        }
        if (mouseX >= contentLeft && mouseX < contentRight && mouseY >= listTop && mouseY < listBottom) {
            unfocusSearch();
            if (tab == Tab.TITLES) {
                int index = (int) ((mouseY - listTop + scrollOffset) / ROW_HEIGHT);
                if (index >= 0 && index < entries.size()) {
                    Entry e = entries.get(index);
                    if (e.title == null || e.unlocked) {
                        String id = e.title == null ? "" : e.title.getId();
                        ClientTitleData.setActiveLocally(id);
                        LegacyNetworking.sendSetActive(id);
                    }
                }
            }
            return true;
        }
        return mouseX >= panelX - TAB_W && mouseX < panelX + BG_W && mouseY >= panelY && mouseY < panelY + BG_H;
    }

    private void rebuildEntries() {
        String query = search == null ? "" : search.getValue().trim().toLowerCase(Locale.ROOT);
        entries.clear();
        firstLockedIndex = -1;
        firstHiddenIndex = -1;
        Map<String, Integer> nextStage = computeNextStages();

        if (tab == Tab.JOURNAL) {
            List<Entry> normal = new ArrayList<>();
            List<Entry> hidden = new ArrayList<>();
            for (Title title : TitleManager.INSTANCE.allSorted()) {
                boolean unlocked = ClientTitleData.isUnlocked(title.getId());
                if (isFutureStage(title, unlocked, nextStage)) {
                    continue;
                }
                boolean secret = !unlocked && title.hidden;
                if (secret && !query.isEmpty()) {
                    continue;
                }
                if (!secret && !query.isEmpty() && !matches(title, query)) {
                    continue;
                }
                if (secret) {
                    hidden.add(new Entry(title, false));
                } else {
                    normal.add(new Entry(title, unlocked));
                }
            }
            entries.addAll(normal);
            firstHiddenIndex = entries.size();
            entries.addAll(hidden);
            return;
        }

        List<Entry> obtained = new ArrayList<>();
        List<Entry> locked = new ArrayList<>();
        List<Entry> hidden = new ArrayList<>();
        if (query.isEmpty()) {
            obtained.add(new Entry(null, true));
        }
        for (Title title : TitleManager.INSTANCE.allSorted()) {
            boolean unlocked = ClientTitleData.isUnlocked(title.getId());
            if (isFutureStage(title, unlocked, nextStage)) {
                continue;
            }
            if (!unlocked && !showAll) {
                continue;
            }
            boolean secret = !unlocked && title.hidden;
            if (secret && !query.isEmpty()) {
                continue;
            }
            if (!secret && !query.isEmpty() && !matches(title, query)) {
                continue;
            }
            if (unlocked) {
                obtained.add(new Entry(title, true));
            } else if (secret) {
                hidden.add(new Entry(title, false));
            } else {
                locked.add(new Entry(title, false));
            }
        }
        entries.addAll(obtained);
        firstLockedIndex = entries.size();
        entries.addAll(locked);
        firstHiddenIndex = entries.size();
        entries.addAll(hidden);
    }

    private static Map<String, Integer> computeNextStages() {
        Map<String, Integer> next = new HashMap<>();
        for (Title t : TitleManager.INSTANCE.all()) {
            if (t.series == null || t.series.isEmpty() || ClientTitleData.isUnlocked(t.getId())) {
                continue;
            }
            next.merge(t.series, t.stage, Math::min);
        }
        return next;
    }

    private static boolean isFutureStage(Title title, boolean unlocked, Map<String, Integer> nextStage) {
        if (unlocked || title.series == null || title.series.isEmpty()) {
            return false;
        }
        Integer next = nextStage.get(title.series);
        return next != null && title.stage > next;
    }

    private static boolean matches(Title title, String query) {
        return title.displayName().getString().toLowerCase(Locale.ROOT).contains(query)
                || title.getCategory().toLowerCase(Locale.ROOT).contains(query);
    }

    private record Entry(@Nullable Title title, boolean unlocked) {
    }
}
