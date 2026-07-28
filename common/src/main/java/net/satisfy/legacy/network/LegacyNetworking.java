package net.satisfy.legacy.network;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.satisfy.legacy.Legacy;
import net.satisfy.legacy.client.ClientTitleData;
import net.satisfy.legacy.client.LegacyToasts;
import net.satisfy.legacy.core.data.PlayerTitleData;
import net.satisfy.legacy.core.title.Placement;
import net.satisfy.legacy.core.title.Title;
import net.satisfy.legacy.core.title.TitleForm;
import net.satisfy.legacy.core.title.TitleManager;
import net.satisfy.legacy.core.title.TitleRarity;
import net.satisfy.legacy.core.trigger.TitleProgress;
import net.satisfy.legacy.server.TitleService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("removal")
public final class LegacyNetworking {
    public static final ResourceLocation REGISTRY_SYNC = Legacy.identifier("registry_sync");
    public static final ResourceLocation SELF_SYNC = Legacy.identifier("self_sync");
    public static final ResourceLocation ACTIVE_BROADCAST = Legacy.identifier("active_broadcast");
    public static final ResourceLocation UNLOCK_TOAST = Legacy.identifier("unlock_toast");
    public static final ResourceLocation PROGRESS_SYNC = Legacy.identifier("progress_sync");
    public static final ResourceLocation SET_ACTIVE = Legacy.identifier("set_active");
    public static final ResourceLocation SET_FORM = Legacy.identifier("set_form");

    private LegacyNetworking() {
    }

    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.c2s(), SET_ACTIVE, (buffer, context) -> {
            String id = buffer.readUtf();
            context.queue(() -> {
                if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                    TitleService.setActive(serverPlayer, id);
                }
            });
        });

        NetworkManager.registerReceiver(NetworkManager.c2s(), SET_FORM, (buffer, context) -> {
            TitleForm form = TitleForm.byOrdinal(buffer.readVarInt());
            context.queue(() -> {
                if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                    TitleService.setForm(serverPlayer, form);
                }
            });
        });

        NetworkManager.registerReceiver(NetworkManager.s2c(), REGISTRY_SYNC, (buffer, context) -> {
            int count = buffer.readVarInt();
            List<Title> titles = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                Title title = new Title();
                title.id = buffer.readUtf();
                title.translationKey = buffer.readUtf();
                title.placement = buffer.readVarInt() == Placement.PREFIX.ordinal() ? Placement.PREFIX : Placement.SUFFIX;
                title.category = buffer.readUtf();
                title.displayPriority = buffer.readVarInt();
                title.hidden = buffer.readBoolean();
                title.icon = buffer.readUtf();
                title.rarity = TitleRarity.byOrdinal(buffer.readVarInt());
                title.syncedTarget = buffer.readVarInt();
                title.series = buffer.readUtf();
                title.stage = buffer.readVarInt();
                titles.add(title);
            }
            context.queue(() -> {
                if (Minecraft.getInstance().getSingleplayerServer() == null) {
                    TitleManager.INSTANCE.replaceAll(titles);
                }
            });
        });

        NetworkManager.registerReceiver(NetworkManager.s2c(), SELF_SYNC, (buffer, context) -> {
            int count = buffer.readVarInt();
            Set<String> unlocked = new LinkedHashSet<>();
            for (int i = 0; i < count; i++) {
                unlocked.add(buffer.readUtf());
            }
            String active = buffer.readUtf();
            TitleForm form = TitleForm.byOrdinal(buffer.readVarInt());
            context.queue(() -> ClientTitleData.setSelf(unlocked, active, form));
        });

        NetworkManager.registerReceiver(NetworkManager.s2c(), ACTIVE_BROADCAST, (buffer, context) -> {
            int count = buffer.readVarInt();
            Map<UUID, String> map = new LinkedHashMap<>();
            Map<UUID, TitleForm> forms = new LinkedHashMap<>();
            for (int i = 0; i < count; i++) {
                UUID id = buffer.readUUID();
                map.put(id, buffer.readUtf());
                forms.put(id, TitleForm.byOrdinal(buffer.readVarInt()));
            }
            context.queue(() -> ClientTitleData.setActiveTitles(map, forms));
        });

        NetworkManager.registerReceiver(NetworkManager.s2c(), UNLOCK_TOAST, (buffer, context) -> {
            String id = buffer.readUtf();
            context.queue(() -> LegacyToasts.showUnlock(id));
        });

        NetworkManager.registerReceiver(NetworkManager.s2c(), PROGRESS_SYNC, (buffer, context) -> {
            int count = buffer.readVarInt();
            Map<String, Integer> progress = new LinkedHashMap<>();
            for (int i = 0; i < count; i++) {
                progress.put(buffer.readUtf(), buffer.readVarInt());
            }
            context.queue(() -> ClientTitleData.setProgress(progress));
        });
    }

    public static void sendRegistry(ServerPlayer player) {
        RegistryFriendlyByteBuf buffer = serverBuffer(player);
        List<Title> titles = TitleManager.INSTANCE.all();
        buffer.writeVarInt(titles.size());
        for (Title title : titles) {
            title.syncedTarget = TitleProgress.target(title);
            buffer.writeUtf(title.getId());
            buffer.writeUtf(title.getTranslationKey());
            buffer.writeVarInt(title.getPlacement().ordinal());
            buffer.writeUtf(title.category == null ? "" : title.category);
            buffer.writeVarInt(title.displayPriority);
            buffer.writeBoolean(title.hidden);
            buffer.writeUtf(title.icon == null ? "minecraft:paper" : title.icon);
            buffer.writeVarInt(title.getRarity().ordinal());
            buffer.writeVarInt(title.syncedTarget);
            buffer.writeUtf(title.series == null ? "" : title.series);
            buffer.writeVarInt(title.stage);
        }
        NetworkManager.sendToPlayer(player, REGISTRY_SYNC, buffer);
    }

    public static void sendSelf(ServerPlayer player, PlayerTitleData data) {
        RegistryFriendlyByteBuf buffer = serverBuffer(player);
        Set<String> unlocked = data.getUnlocked();
        buffer.writeVarInt(unlocked.size());
        for (String id : unlocked) {
            buffer.writeUtf(id);
        }
        buffer.writeUtf(data.getActive() == null ? "" : data.getActive());
        buffer.writeVarInt(data.getForm().ordinal());
        NetworkManager.sendToPlayer(player, SELF_SYNC, buffer);
    }

    public static void sendActiveBroadcast(ServerPlayer player, Map<UUID, String> activeByPlayer, Map<UUID, Integer> formByPlayer) {
        RegistryFriendlyByteBuf buffer = serverBuffer(player);
        buffer.writeVarInt(activeByPlayer.size());
        for (Map.Entry<UUID, String> entry : activeByPlayer.entrySet()) {
            buffer.writeUUID(entry.getKey());
            buffer.writeUtf(entry.getValue());
            buffer.writeVarInt(formByPlayer.getOrDefault(entry.getKey(), 0));
        }
        NetworkManager.sendToPlayer(player, ACTIVE_BROADCAST, buffer);
    }

    public static void sendUnlockToast(ServerPlayer player, String titleId) {
        RegistryFriendlyByteBuf buffer = serverBuffer(player);
        buffer.writeUtf(titleId);
        NetworkManager.sendToPlayer(player, UNLOCK_TOAST, buffer);
    }

    public static void sendProgress(ServerPlayer player, Map<String, Integer> progress) {
        RegistryFriendlyByteBuf buffer = serverBuffer(player);
        buffer.writeVarInt(progress.size());
        for (Map.Entry<String, Integer> entry : progress.entrySet()) {
            buffer.writeUtf(entry.getKey());
            buffer.writeVarInt(entry.getValue());
        }
        NetworkManager.sendToPlayer(player, PROGRESS_SYNC, buffer);
    }

    public static void sendSetActive(String titleId) {
        RegistryFriendlyByteBuf buffer = clientBuffer();
        buffer.writeUtf(titleId == null ? "" : titleId);
        NetworkManager.sendToServer(SET_ACTIVE, buffer);
    }

    public static void sendSetForm(TitleForm form) {
        RegistryFriendlyByteBuf buffer = clientBuffer();
        buffer.writeVarInt(form.ordinal());
        NetworkManager.sendToServer(SET_FORM, buffer);
    }

    private static RegistryFriendlyByteBuf serverBuffer(ServerPlayer player) {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
    }

    private static RegistryFriendlyByteBuf clientBuffer() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() != null) {
            return new RegistryFriendlyByteBuf(Unpooled.buffer(), minecraft.getConnection().registryAccess());
        }
        if (minecraft.level != null) {
            return new RegistryFriendlyByteBuf(Unpooled.buffer(), minecraft.level.registryAccess());
        }
        throw new IllegalStateException("Missing client registry access");
    }
}
