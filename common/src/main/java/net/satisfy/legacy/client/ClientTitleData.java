package net.satisfy.legacy.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.satisfy.legacy.core.title.TitleForm;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Environment(EnvType.CLIENT)
public final class ClientTitleData {
    private static final Set<String> UNLOCKED = new LinkedHashSet<>();
    private static String active = "";
    private static TitleForm form = TitleForm.MASCULINE;
    private static final Map<UUID, String> ACTIVE_BY_PLAYER = new ConcurrentHashMap<>();
    private static final Map<UUID, TitleForm> FORM_BY_PLAYER = new ConcurrentHashMap<>();
    private static final Map<String, Integer> PROGRESS = new ConcurrentHashMap<>();

    private ClientTitleData() {
    }

    public static void setProgress(Map<String, Integer> map) {
        PROGRESS.clear();
        PROGRESS.putAll(map);
    }

    public static int getProgress(String id) {
        return PROGRESS.getOrDefault(id, 0);
    }

    public static void setSelf(Set<String> unlocked, String activeId, TitleForm ownForm) {
        UNLOCKED.clear();
        UNLOCKED.addAll(unlocked);
        active = activeId == null ? "" : activeId;
        form = ownForm == null ? TitleForm.MASCULINE : ownForm;
    }

    public static TitleForm getForm() {
        return form;
    }

    public static void setFormLocally(TitleForm ownForm) {
        form = ownForm == null ? TitleForm.MASCULINE : ownForm;
    }

    public static TitleForm getForm(UUID playerId) {
        return FORM_BY_PLAYER.getOrDefault(playerId, TitleForm.MASCULINE);
    }

    public static Set<String> getUnlocked() {
        return UNLOCKED;
    }

    public static boolean isUnlocked(String id) {
        return UNLOCKED.contains(id);
    }

    public static String getActive() {
        return active;
    }

    public static void setActiveLocally(String id) {
        active = id == null ? "" : id;
    }

    public static void setActiveTitles(Map<UUID, String> map, Map<UUID, TitleForm> forms) {
        ACTIVE_BY_PLAYER.clear();
        ACTIVE_BY_PLAYER.putAll(map);
        FORM_BY_PLAYER.clear();
        FORM_BY_PLAYER.putAll(forms);
    }

    public static String getActiveTitle(UUID playerId) {
        return ACTIVE_BY_PLAYER.getOrDefault(playerId, "");
    }
}
