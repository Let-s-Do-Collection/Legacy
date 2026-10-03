package net.satisfy.legacy.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.satisfy.legacy.core.title.Title;
import net.satisfy.legacy.core.title.TitleForm;
import net.satisfy.legacy.core.title.TitleManager;
import net.satisfy.legacy.network.LegacyNetworking;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class JournalPanel {
    private enum Tab { TITLES, JOURNAL, JOURNEY, MILESTONES, HISTORY, OPTIONS }

    private static final String[] JOURNEY_CATEGORIES = {
            "equipment", "exploration", "survival", "farming", "villagers", "combat", "magic", "misc"
    };

    private record JourneyRow(String header, ClientJourneyData.Entry entry) {
    }

    private static final ResourceLocation BUTTON = ResourceLocation.fromNamespaceAndPath("legacy", "widget/journal_button");
    private static final ResourceLocation BUTTON_HL = ResourceLocation.fromNamespaceAndPath("legacy", "widget/journal_button_highlighted");
    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath("legacy", "textures/gui/background.png");
    private static final ResourceLocation LENS = ResourceLocation.fromNamespaceAndPath("legacy", "widget/lens");
    private static final int LENS_SIZE = 12;
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
    private static final int HISTORY_HEADER = 19;
    private static final int BAR_W = 40;
    private static final int BAR_H = 5;

    private static boolean visible;
    private static Tab tab = Tab.TITLES;
    private static String expandedMilestone;
    private static String expandedHistory;
    private boolean showAll;
    private int scrollOffset;
    private EditBox search;
    private final List<Entry> entries = new ArrayList<>();

    private int iconX, iconY;
    private int panelX, panelY;
    private int contentLeft, contentRight, listTop, listBottom;
    private int filterX, filterY;
    private int firstLockedIndex, firstHiddenIndex;
    private int[] milestoneHeaderY = new int[0];
    private int milestoneContentHeight;
    private int[] historyHeaderY = new int[0];
    private String[] historyOrder = new String[0];
    private int historyContentHeight;
    private int journeyContentHeight;
    private static final java.util.Set<String> journeyCollapsed = new java.util.HashSet<>();
    private final List<JourneyRow> journeyRows = new ArrayList<>();

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean v) {
        visible = v;
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
        int maxScroll = Math.max(0, contentHeight() - (listBottom - listTop));
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - scrollY * ROW_HEIGHT));
        return true;
    }

    private int contentHeight() {
        return switch (tab) {
            case MILESTONES -> milestoneContentHeight;
            case HISTORY -> historyContentHeight;
            case JOURNEY -> journeyContentHeight;
            case OPTIONS -> OPTION_KEYS.length * ROW_HEIGHT;
            default -> entries.size() * ROW_HEIGHT;
        };
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
        this.iconX = leftPos + imageWidth - 22 + LegacyClientConfig.buttonOffsetX;
        this.iconY = topPos + 4 + LegacyClientConfig.buttonOffsetY;
        boolean overIcon = isOverButton(mouseX, mouseY);
        g.blitSprite(overIcon || visible ? BUTTON_HL : BUTTON, iconX, iconY, 20, 18);

        if (!visible) {
            if (overIcon) {
                g.renderTooltip(font, Component.translatable("gui.legacy.button"), mouseX, mouseY);
            }
            return;
        }

        this.panelX = leftPos - BG_W - 3;
        this.panelY = topPos - 1;
        this.contentLeft = panelX + 8;
        this.contentRight = panelX + BG_W - 8;
        this.listTop = panelY + 30;
        this.listBottom = panelY + BG_H - 8;

        g.blit(BG, panelX, panelY, 0, 0, BG_W, BG_H);
        renderTabs(g);

        if (search != null) {
            boolean showSearch = tab == Tab.TITLES || tab == Tab.JOURNAL;
            search.setVisible(showSearch);
            if (showSearch) {
                search.setPosition(panelX + 26, panelY + 14);
                search.setWidth(81);
            }
        }

        if (tab == Tab.TITLES) {
            renderTitles(g, font, mouseX, mouseY);
        } else if (tab == Tab.JOURNAL) {
            renderJournal(g, font, mouseX, mouseY);
        } else if (tab == Tab.JOURNEY) {
            renderJourney(g, font, mouseX, mouseY);
        } else if (tab == Tab.MILESTONES) {
            renderPageHeader(g, font, Component.translatable("gui.legacy.tab.milestones"));
            renderMilestones(g, font, mouseX, mouseY);
        } else if (tab == Tab.HISTORY) {
            renderPageHeader(g, font, Component.translatable("gui.legacy.tab.history"));
            renderHistory(g, font, mouseX, mouseY);
        } else {
            renderPageHeader(g, font, Component.translatable("gui.legacy.tab.options"));
            renderOptions(g, font, mouseX, mouseY);
        }

        if (search != null && search.isVisible()) {
            search.render(g, mouseX, mouseY, partialTick);
            g.blitSprite(LENS, panelX + 11, panelY + 15, LENS_SIZE, LENS_SIZE);
        }
        if (tab == Tab.TITLES) {
            renderFilterButton(g, font, mouseX, mouseY);
        }

        if (overIcon) {
            g.renderTooltip(font, Component.translatable("gui.legacy.button"), mouseX, mouseY);
        }
        for (int i = 0; i < TAB_KEYS.length; i++) {
            if (overTab(mouseX, mouseY, i)) {
                g.renderTooltip(font, Component.translatable(TAB_KEYS[i]), mouseX, mouseY);
                break;
            }
        }
    }

    private static final String[] TAB_KEYS = {
            "gui.legacy.tab.titles", "gui.legacy.tab.journal", "gui.legacy.tab.journey",
            "gui.legacy.tab.milestones", "gui.legacy.tab.history", "gui.legacy.tab.options"
    };

    private void renderTabs(GuiGraphics g) {
        ItemStack[] icons = { new ItemStack(Items.NAME_TAG), new ItemStack(Items.WRITABLE_BOOK), new ItemStack(Items.WRITTEN_BOOK), new ItemStack(Items.NETHER_STAR), new ItemStack(Items.CLOCK), new ItemStack(Items.COMPARATOR) };
        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            boolean sel = tab == tabs[i];
            int y = tabY(i);
            g.blitSprite(sel ? TAB_SELECTED : TAB, tabX(sel), y, TAB_W, TAB_H);
            g.renderFakeItem(icons[i], panelX - 23, y + 5);
        }
    }

    private int tabX(boolean selected) {
        return panelX - (selected ? 31 : 29);
    }

    private int tabY(int i) {
        return panelY + 4 + i * TAB_H;
    }

    private boolean overTab(double mx, double my, int i) {
        boolean sel = tab == Tab.values()[i];
        int y = tabY(i);
        return mx >= tabX(sel) && mx < tabX(sel) + TAB_W && my >= y && my < y + TAB_H;
    }

    private void renderFilterButton(GuiGraphics g, Font font, int mouseX, int mouseY) {
        this.filterX = panelX + 111;
        this.filterY = panelY + 13;
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
        if (!entries.isEmpty() && entries.get(0).title == null) {
            drawGroupDivider(g, 1);
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
        Map<String, Integer> seriesCount = new HashMap<>();
        for (Title a : TitleManager.INSTANCE.all()) {
            if (a.series != null && !a.series.isEmpty()) {
                seriesCount.merge(a.series, 1, Integer::sum);
            }
        }
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
                Integer c = LegacyClientConfig.titleColors ? t.getRarity().getColor().getColor() : null;
                nameColor = c == null ? 0xFFFFFF : c;
            } else {
                nameColor = 0xAAAAAA;
            }
            String stageMark = "";
            if (t.series != null && !t.series.isEmpty() && seriesCount.getOrDefault(t.series, 0) >= 2) {
                stageMark = toRoman(t.stage);
            }
            int markW = stageMark.isEmpty() ? 0 : font.width(stageMark) + 4;
            String name = trimToWidth(font, TitleNames.string(t, ClientTitleData.getForm()), nameRight - nameLeft - markW);
            g.drawString(font, name, nameLeft, rowY + 5, nameColor, true);
            if (!stageMark.isEmpty()) {
                g.drawString(font, stageMark, nameLeft + font.width(name) + 4, rowY + 5, 0xC8A45A, true);
            }

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
            lines.add(Component.translatable("gui.legacy.reward.label").withStyle(ChatFormatting.GRAY));
            lines.add(TitleNames.styled(t, form).withStyle(ChatFormatting.BOLD));
        }
        return lines;
    }

    private static String toRoman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            default -> n > 0 ? Integer.toString(n) : "";
        };
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
        int maxScroll = Math.max(0, contentHeight() - (listBottom - listTop));
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
    }

    private void renderPageHeader(GuiGraphics g, Font font, Component titleText) {
        Component title = titleText.copy().withStyle(ChatFormatting.BOLD);
        int tw = font.width(title);
        int cx = panelX + BG_W / 2;
        g.drawString(font, title, cx - tw / 2, panelY + 13, 0xC8A45A, true);
        g.fill(contentLeft, panelY + 27, contentRight, panelY + 28, 0x40000000);
        g.fill(contentLeft, panelY + 28, contentRight, panelY + 29, 0x18FFFFFF);
    }

    private static final String[] OPTION_KEYS = {
            "gui.legacy.option.form", "gui.legacy.option.name",
            "gui.legacy.option.notifications", "gui.legacy.option.others",
            "gui.legacy.option.unlock_toasts", "gui.legacy.option.colors",
            "gui.legacy.option.button_x", "gui.legacy.option.button_y"
    };

    private void renderOptions(GuiGraphics g, Font font, int mouseX, int mouseY) {
        List<Component> tooltip = null;
        g.enableScissor(contentLeft, listTop, contentRight, listBottom);
        for (int i = 0; i < OPTION_KEYS.length; i++) {
            int rowY = listTop + i * ROW_HEIGHT - scrollOffset;
            if (rowY + ROW_HEIGHT <= listTop || rowY >= listBottom) {
                continue;
            }
            boolean hov = mouseY >= listTop && mouseY < listBottom && mouseX >= contentLeft && mouseX < contentRight && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
            if (hov) {
                g.fill(contentLeft, rowY, contentRight, rowY + ROW_HEIGHT, 0x33201810);
                tooltip = List.of(
                        Component.translatable(OPTION_KEYS[i]).withStyle(ChatFormatting.WHITE),
                        Component.translatable(OPTION_KEYS[i] + ".desc").withStyle(ChatFormatting.GRAY));
            }
            g.drawString(font, Component.translatable(OPTION_KEYS[i]), contentLeft + 4, rowY + 6, 0xE0E0E0, false);
            Component value;
            int color;
            if (i == 0) {
                boolean fem = ClientTitleData.getForm() == TitleForm.FEMININE;
                value = Component.translatable(fem ? "gui.legacy.option.form.female" : "gui.legacy.option.form.male");
                color = 0xC8A45A;
            } else if (i >= 6) {
                int offset = i == 6 ? LegacyClientConfig.buttonOffsetX : LegacyClientConfig.buttonOffsetY;
                value = Component.literal(offset > 0 ? "+" + offset : String.valueOf(offset));
                color = 0xC8A45A;
            } else {
                boolean on = optionValue(i);
                value = Component.translatable(on ? "gui.legacy.option.on" : "gui.legacy.option.off");
                color = on ? 0x5FDB5F : 0x9A5A5A;
            }
            g.drawString(font, value, contentRight - 4 - font.width(value), rowY + 6, color, false);
        }
        g.disableScissor();
        if (tooltip != null) {
            g.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }
    }

    private static boolean optionValue(int i) {
        return switch (i) {
            case 1 -> LegacyClientConfig.showOwnName;
            case 2 -> LegacyClientConfig.milestoneNotifications;
            case 3 -> LegacyClientConfig.showOthersTitles;
            case 4 -> LegacyClientConfig.titleUnlockToasts;
            case 5 -> LegacyClientConfig.titleColors;
            default -> false;
        };
    }

    private void adjustButtonOffset(int i, int button) {
        int step = net.minecraft.client.gui.screens.Screen.hasShiftDown() ? 10 : 1;
        int current = i == 6 ? LegacyClientConfig.buttonOffsetX : LegacyClientConfig.buttonOffsetY;
        int next = switch (button) {
            case 0 -> current + step;
            case 1 -> current - step;
            default -> 0;
        };
        next = Math.max(-500, Math.min(500, next));
        if (i == 6) {
            LegacyClientConfig.buttonOffsetX = next;
        } else {
            LegacyClientConfig.buttonOffsetY = next;
        }
        LegacyClientConfig.save();
    }

    private void toggleOption(int i) {
        switch (i) {
            case 0 -> {
                TitleForm next = ClientTitleData.getForm().toggled();
                ClientTitleData.setFormLocally(next);
                LegacyNetworking.sendSetForm(next);
            }
            case 1 -> LegacyClientConfig.showOwnName = !LegacyClientConfig.showOwnName;
            case 2 -> LegacyClientConfig.milestoneNotifications = !LegacyClientConfig.milestoneNotifications;
            case 3 -> LegacyClientConfig.showOthersTitles = !LegacyClientConfig.showOthersTitles;
            case 4 -> LegacyClientConfig.titleUnlockToasts = !LegacyClientConfig.titleUnlockToasts;
            case 5 -> LegacyClientConfig.titleColors = !LegacyClientConfig.titleColors;
        }
        if (i != 0) {
            LegacyClientConfig.save();
        }
    }

    private void renderJourney(GuiGraphics g, Font font, int mouseX, int mouseY) {
        String name = Minecraft.getInstance().player != null
                ? Minecraft.getInstance().player.getGameProfile().getName() : "";
        renderPageHeader(g, font, Component.translatable("gui.legacy.journey.header", name));

        List<JourneyRow> rows = buildJourneyRows();
        journeyContentHeight = rows.size() * ROW_HEIGHT;
        clampScroll();

        Map<String, int[]> counts = new HashMap<>();
        for (ClientJourneyData.Entry e : ClientJourneyData.all()) {
            String cat = e.category() == null || e.category().isBlank() ? "misc" : e.category();
            int[] c = counts.computeIfAbsent(cat, k -> new int[2]);
            c[1]++;
            if (e.done()) {
                c[0]++;
            }
        }

        List<Component> tooltip = null;
        g.enableScissor(contentLeft, listTop, contentRight, listBottom);
        int y = listTop - scrollOffset;
        for (JourneyRow row : rows) {
            if (y + ROW_HEIGHT > listTop && y < listBottom) {
                if (row.header() != null) {
                    boolean collapsed = journeyCollapsed.contains(row.header());
                    boolean hov = mouseX >= contentLeft && mouseX < contentRight
                            && mouseY >= Math.max(y, listTop) && mouseY < Math.min(y + ROW_HEIGHT, listBottom);
                    if (hov) {
                        g.fill(contentLeft, y, contentRight, y + ROW_HEIGHT, 0x22FFFFFF);
                    }
                    g.drawString(font, Component.literal(collapsed ? "▸" : "▾"), contentLeft + 2, y + 6, 0xF0C24E, true);
                    g.drawString(font, categoryLabel(row.header()).copy().withStyle(ChatFormatting.BOLD),
                            contentLeft + 12, y + 6, 0xC8A45A, true);
                    int[] c = counts.getOrDefault(row.header(), new int[2]);
                    String count = c[0] + "/" + c[1];
                    g.drawString(font, count, contentRight - 2 - font.width(count), y + 6, 0x8A7A55, true);
                    g.fill(contentLeft, y + ROW_HEIGHT - 1, contentRight, y + ROW_HEIGHT, 0x30201810);
                } else {
                    ClientJourneyData.Entry e = row.entry();
                    boolean hov = mouseX >= contentLeft && mouseX < contentRight
                            && mouseY >= Math.max(y, listTop) && mouseY < Math.min(y + ROW_HEIGHT, listBottom);
                    if (hov) {
                        g.fill(contentLeft, y, contentRight, y + ROW_HEIGHT, 0x33201810);
                    }
                    if (e.done()) {
                        g.drawString(font, Component.literal("✔"), contentLeft + 4, y + 5, 0x2E8B2E, false);
                    } else {
                        g.drawString(font, Component.literal("☐"), contentLeft + 4, y + 5, 0x585858, false);
                    }
                    g.renderFakeItem(e.iconStack(), contentLeft + 16, y + 1);
                    if (!e.done()) {
                        g.fill(contentLeft + 16, y + 1, contentLeft + 32, y + 17, 0x80101010);
                    }
                    int color = e.done() ? 0xE8E8E8 : 0x808080;
                    g.drawString(font, trimToWidth(font, e.titleString(), contentRight - 4 - (contentLeft + 36)),
                            contentLeft + 36, y + 5, color, true);
                    if (hov) {
                        tooltip = new ArrayList<>();
                        tooltip.add(Component.literal(e.titleString()).withStyle(e.done() ? ChatFormatting.WHITE : ChatFormatting.GRAY));
                        if (!e.descriptionString().isEmpty()) {
                            tooltip.add(Component.literal(e.descriptionString()).withStyle(ChatFormatting.DARK_GRAY));
                        }
                        tooltip.add(Component.empty());
                        tooltip.add(e.done()
                                ? Component.translatable("gui.legacy.journey.completed", e.day()).withStyle(ChatFormatting.GREEN)
                                : Component.translatable("gui.legacy.journey.open").withStyle(ChatFormatting.DARK_GRAY));
                    }
                }
            }
            y += ROW_HEIGHT;
        }
        g.disableScissor();
        if (tooltip != null) {
            g.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }
    }

    private static Component categoryLabel(String category) {
        String key = "gui.legacy.journey.category." + category;
        if (I18n.exists(key)) {
            return Component.translatable(key);
        }
        String pretty = category.isEmpty() ? category
                : Character.toUpperCase(category.charAt(0)) + category.substring(1);
        return Component.literal(pretty);
    }

    private List<JourneyRow> buildJourneyRows() {
        Map<String, List<ClientJourneyData.Entry>> byCategory = new LinkedHashMap<>();
        for (String category : JOURNEY_CATEGORIES) {
            byCategory.put(category, new ArrayList<>());
        }
        for (ClientJourneyData.Entry e : ClientJourneyData.all()) {
            String category = e.category() == null || e.category().isBlank() ? "misc" : e.category();
            byCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(e);
        }
        List<JourneyRow> rows = new ArrayList<>();
        for (Map.Entry<String, List<ClientJourneyData.Entry>> group : byCategory.entrySet()) {
            List<ClientJourneyData.Entry> list = group.getValue();
            if (list.isEmpty()) {
                continue;
            }
            list.sort(Comparator.comparingInt((ClientJourneyData.Entry e) -> e.done() ? 0 : 1)
                    .thenComparing(ClientJourneyData.Entry::titleString, String.CASE_INSENSITIVE_ORDER));
            rows.add(new JourneyRow(group.getKey(), null));
            if (!journeyCollapsed.contains(group.getKey())) {
                for (ClientJourneyData.Entry e : list) {
                    rows.add(new JourneyRow(null, e));
                }
            }
        }
        journeyRows.clear();
        journeyRows.addAll(rows);
        return rows;
    }

    private void renderMilestones(GuiGraphics g, Font font, int mouseX, int mouseY) {
        List<ClientMilestoneData.Entry> list = ClientMilestoneData.all();
        int detailH = 0;
        ClientMilestoneData.Entry expanded = null;
        if (expandedMilestone != null) {
            for (ClientMilestoneData.Entry e : list) {
                if (e.id().equals(expandedMilestone)) {
                    expanded = e;
                    break;
                }
            }
            if (expanded != null) {
                detailH = detailBlock(g, font, expanded, 0, false);
            }
        }
        milestoneContentHeight = list.size() * ROW_HEIGHT + detailH;
        clampScroll();

        milestoneHeaderY = new int[list.size()];
        g.enableScissor(contentLeft, listTop, contentRight, listBottom);
        int y = listTop - scrollOffset;
        for (int i = 0; i < list.size(); i++) {
            ClientMilestoneData.Entry e = list.get(i);
            milestoneHeaderY[i] = y;
            boolean open = e.id().equals(expandedMilestone);
            if (y + ROW_HEIGHT > listTop && y < listBottom) {
                boolean hov = mouseX >= contentLeft && mouseX < contentRight
                        && mouseY >= Math.max(y, listTop) && mouseY < Math.min(y + ROW_HEIGHT, listBottom);
                if (hov || open) {
                    g.fill(contentLeft, y, contentRight, y + ROW_HEIGHT, open ? 0x44201810 : 0x33201810);
                }
                g.renderFakeItem(e.iconStack(), contentLeft, y + 1);
                int nameColor = e.state() == ClientMilestoneData.UNCLAIMED ? 0xFFFFFF : 0xFFC33D;
                g.drawString(font, trimToWidth(font, Component.translatable(e.nameKey()).getString(), contentRight - 12 - (contentLeft + 20)),
                        contentLeft + 20, y + 5, nameColor, true);
                g.drawString(font, Component.literal(open ? "▾" : "▸"), contentRight - 10, y + 5, 0xF0C24E, true);
            }
            y += ROW_HEIGHT;
            if (open) {
                if (y < listBottom && y + detailH > listTop) {
                    detailBlock(g, font, e, y, true);
                }
                y += detailH;
            }
        }
        g.disableScissor();
    }

    private int detailBlock(GuiGraphics g, Font font, ClientMilestoneData.Entry e, int startY, boolean draw) {
        int left = contentLeft + 2;
        int width = contentRight - 2 - left;
        int y = startY + 3;
        for (FormattedCharSequence line : font.split(Component.translatable("milestone.legacy." + e.id() + ".desc"), width)) {
            if (draw) {
                g.drawString(font, line, left, y, 0xB0A48A, false);
            }
            y += 10;
        }
        y += 3;
        if (e.coop()) {
            if (draw) {
                g.drawString(font, Component.translatable("gui.legacy.milestone.shared"), left, y, 0x4FD0C8, false);
            }
            y += 10;
            for (FormattedCharSequence line : font.split(Component.translatable("gui.legacy.milestone.shared.desc"), width)) {
                if (draw) {
                    g.drawString(font, line, left, y, 0x808080, false);
                }
                y += 9;
            }
            y += 3;
        }
        if (draw) {
            g.fill(contentLeft, y, contentRight, y + 1, 0x50201810);
        }
        y += 6;
        if (draw) {
            g.drawString(font, Component.translatable("gui.legacy.milestone.status"), left, y, 0x8A8A8A, false);
        }
        y += 10;
        int st = e.state();
        String statusKey = st == ClientMilestoneData.CLAIMED ? "claimed" : st == ClientMilestoneData.PRE_EXISTING ? "pre_existing" : "unclaimed";
        int statusColor = st == ClientMilestoneData.CLAIMED ? 0x5FDB5F : st == ClientMilestoneData.PRE_EXISTING ? 0x999999 : 0xDD5555;
        if (draw) {
            g.drawString(font, Component.translatable("gui.legacy.milestone.status." + statusKey), left, y, statusColor, false);
        }
        y += 13;
        if (st == ClientMilestoneData.CLAIMED) {
            if (draw) {
                g.drawString(font, Component.translatable("gui.legacy.milestone.recipients"), left, y, 0x8A8A8A, false);
            }
            y += 10;
            int shown = 0;
            for (String r : e.recipients()) {
                if (shown++ >= 8) {
                    break;
                }
                if (draw) {
                    g.drawString(font, Component.literal("• " + r), left + 4, y, 0xE0E0E0, false);
                }
                y += 10;
            }
        } else if (st != ClientMilestoneData.PRE_EXISTING) {
            if (draw) {
                g.drawString(font, Component.translatable("gui.legacy.milestone.nobody"), left, y, 0x8A8A8A, false);
            }
            y += 10;
        }
        return y - startY + 3;
    }

    private void renderHistory(GuiGraphics g, Font font, int mouseX, int mouseY) {
        List<ClientMilestoneData.Entry> list = new ArrayList<>(ClientMilestoneData.all());
        list.sort(java.util.Comparator.comparingInt((ClientMilestoneData.Entry e) -> e.state() == ClientMilestoneData.UNCLAIMED ? 1 : 0)
                .thenComparingInt(ClientMilestoneData.Entry::worldDay)
                .thenComparing(e -> e.time() == null ? "" : e.time()));
        int detailH = 0;
        for (ClientMilestoneData.Entry e : list) {
            if (e.id().equals(expandedHistory)) {
                detailH = historyDetail(g, font, e, 0, false);
                break;
            }
        }
        historyContentHeight = list.size() * HISTORY_HEADER + detailH;
        clampScroll();
        historyHeaderY = new int[list.size()];
        historyOrder = new String[list.size()];
        g.enableScissor(contentLeft, listTop, contentRight, listBottom);
        int y = listTop - scrollOffset;
        for (int i = 0; i < list.size(); i++) {
            ClientMilestoneData.Entry e = list.get(i);
            historyHeaderY[i] = y;
            historyOrder[i] = e.id();
            boolean open = e.id().equals(expandedHistory);
            int st = e.state();
            if (y + HISTORY_HEADER > listTop && y < listBottom) {
                boolean hov = mouseX >= contentLeft && mouseX < contentRight
                        && mouseY >= Math.max(y, listTop) && mouseY < Math.min(y + HISTORY_HEADER, listBottom);
                if (hov || open) {
                    g.fill(contentLeft, y, contentRight, y + HISTORY_HEADER, open ? 0x44201810 : 0x33201810);
                }
                if (i > 0) {
                    g.fill(contentLeft + 2, y, contentRight - 2, y + 1, 0x2AFFFFFF);
                }
                g.renderFakeItem(e.iconStack(), contentLeft, y + 1);
                int nameColor = st == ClientMilestoneData.CLAIMED ? 0xFFC33D
                        : st == ClientMilestoneData.PRE_EXISTING ? 0x9A9A9A : 0x6E6E6E;
                g.drawString(font, Component.translatable("history.legacy." + e.id()), contentLeft + 20, y + 5, nameColor, false);
                g.drawString(font, Component.literal(open ? "▾" : "▸"), contentRight - 10, y + 5, 0xF0C24E, true);
            }
            y += HISTORY_HEADER;
            if (open) {
                if (y < listBottom && y + detailH > listTop) {
                    historyDetail(g, font, e, y, true);
                }
                y += detailH;
            }
        }
        g.disableScissor();
    }

    private int historyDetail(GuiGraphics g, Font font, ClientMilestoneData.Entry e, int startY, boolean draw) {
        int left = contentLeft + 26;
        int y = startY + 2;
        int st = e.state();
        if (st == ClientMilestoneData.CLAIMED) {
            if (draw) {
                g.drawString(font, Component.translatable("gui.legacy.milestone.day")
                        .copy().append(Component.literal(" " + e.worldDay())), left, y, 0xE8DFC8, false);
            }
            y += 15;
            if (draw) {
                g.drawString(font, Component.literal(e.time()), left, y, 0xC0B89E, false);
            }
            y += 15;
            if (draw) {
                g.drawString(font, Component.literal(e.date()), left, y, 0xC0B89E, false);
            }
            y += 13;
        } else if (st == ClientMilestoneData.PRE_EXISTING) {
            if (draw) {
                g.drawString(font, Component.translatable("gui.legacy.milestone.status.pre_existing"), left, y, 0x9A9A9A, false);
            }
            y += 13;
        } else {
            if (draw) {
                g.drawString(font, Component.translatable("gui.legacy.history.not_yet"), left, y, 0x8A8A8A, false);
            }
            y += 13;
        }
        return y - startY + 3;
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
        if (!visible) {
            return false;
        }
        if (button != 0) {
            if (tab == Tab.OPTIONS && mouseX >= contentLeft && mouseX < contentRight && mouseY >= listTop && mouseY < listBottom) {
                int index = (int) ((mouseY - listTop + scrollOffset) / ROW_HEIGHT);
                if (index >= 6 && index < OPTION_KEYS.length) {
                    adjustButtonOffset(index, button);
                    return true;
                }
            }
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
            } else if (tab == Tab.MILESTONES) {
                List<ClientMilestoneData.Entry> mlist = ClientMilestoneData.all();
                for (int i = 0; i < milestoneHeaderY.length && i < mlist.size(); i++) {
                    int ry = milestoneHeaderY[i];
                    if (mouseY >= ry && mouseY < ry + ROW_HEIGHT) {
                        String id = mlist.get(i).id();
                        expandedMilestone = id.equals(expandedMilestone) ? null : id;
                        break;
                    }
                }
            } else if (tab == Tab.HISTORY) {
                for (int i = 0; i < historyHeaderY.length && i < historyOrder.length; i++) {
                    int ry = historyHeaderY[i];
                    if (mouseY >= ry && mouseY < ry + HISTORY_HEADER) {
                        String id = historyOrder[i];
                        expandedHistory = id.equals(expandedHistory) ? null : id;
                        break;
                    }
                }
            } else if (tab == Tab.JOURNEY) {
                int index = (int) ((mouseY - listTop + scrollOffset) / ROW_HEIGHT);
                if (index >= 0 && index < journeyRows.size()) {
                    String header = journeyRows.get(index).header();
                    if (header != null) {
                        if (!journeyCollapsed.remove(header)) {
                            journeyCollapsed.add(header);
                        }
                    }
                }
            } else if (tab == Tab.OPTIONS) {
                int index = (int) ((mouseY - listTop + scrollOffset) / ROW_HEIGHT);
                if (index >= 6 && index < OPTION_KEYS.length) {
                    adjustButtonOffset(index, button);
                } else if (index >= 0 && index < OPTION_KEYS.length) {
                    toggleOption(index);
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
                if (title.milestone) {
                    continue;
                }
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
            if (title.milestone && !unlocked) {
                continue;
            }
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
