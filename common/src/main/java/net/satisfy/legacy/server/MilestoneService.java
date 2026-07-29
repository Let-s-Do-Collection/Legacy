package net.satisfy.legacy.server;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.satisfy.legacy.core.data.LegacyMilestoneSavedData;
import net.satisfy.legacy.core.milestone.Milestone;
import net.satisfy.legacy.core.milestone.MilestoneManager;
import net.satisfy.legacy.core.milestone.MilestoneRecord;
import net.satisfy.legacy.network.LegacyNetworking;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class MilestoneService {
    private static final long GRACE_TICKS = 6000L;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    private MilestoneService() {
    }

    public static void onJoin(ServerPlayer player) {
        MinecraftServer server = player.server;
        MilestoneManager.INSTANCE.ensureLoaded(server);
        LegacyMilestoneSavedData data = LegacyMilestoneSavedData.get(server);
        boolean changed = false;
        for (Milestone milestone : MilestoneManager.INSTANCE.all()) {
            String adv = milestone.retroAdvancement();
            if (adv == null) {
                continue;
            }
            MilestoneRecord record = data.getOrCreate(milestone.getId());
            if (record.state == MilestoneRecord.State.UNCLAIMED && hasAdvancement(player, adv)) {
                record.state = MilestoneRecord.State.PRE_EXISTING;
                changed = true;
            }
        }
        if (changed) {
            data.setDirty();
        }
        sendTo(player, data);
    }

    public static void onDimension(ServerPlayer player, ResourceLocation dimensionId) {
        dispatch(player, "dimension", dimensionId.toString());
    }

    public static void onKill(ServerPlayer killer, ResourceLocation entityId) {
        dispatch(killer, "kill", entityId.toString());
    }

    public static void onAdvancement(ServerPlayer player, ResourceLocation advancementId) {
        dispatch(player, "advancement", advancementId.toString());
    }

    private static void dispatch(ServerPlayer player, String kind, String value) {
        MinecraftServer server = player.server;
        MilestoneManager.INSTANCE.ensureLoaded(server);
        for (Milestone milestone : MilestoneManager.INSTANCE.all()) {
            if (kind.equals(milestone.triggerKind) && value.equals(milestone.triggerValue)) {
                tryClaim(server, milestone, player);
            }
        }
    }

    private static void tryClaim(MinecraftServer server, Milestone milestone, ServerPlayer player) {
        LegacyMilestoneSavedData data = LegacyMilestoneSavedData.get(server);
        MilestoneRecord record = data.getOrCreate(milestone.getId());
        if (record.state == MilestoneRecord.State.PRE_EXISTING) {
            return;
        }
        ServerLevel overworld = server.overworld();
        long now = overworld.getGameTime();

        if (record.state == MilestoneRecord.State.UNCLAIMED) {
            record.state = MilestoneRecord.State.CLAIMED;
            record.firstPlayer = player.getUUID();
            record.firstName = player.getGameProfile().getName();
            record.worldDay = (int) (overworld.getDayTime() / 24000L);
            record.time = formatDayTime(overworld.getDayTime());
            record.date = LocalDate.now().format(DATE);
            record.graceUntil = milestone.coop ? now + GRACE_TICKS : now;
            record.recipients.add(new MilestoneRecord.Recipient(player.getUUID(), record.firstName));
            data.setDirty();
            announce(server, milestone, player, data);
        } else if (record.state == MilestoneRecord.State.CLAIMED
                && milestone.coop && now <= record.graceUntil && !record.hasRecipient(player.getUUID())) {
            record.recipients.add(new MilestoneRecord.Recipient(player.getUUID(), player.getGameProfile().getName()));
            data.setDirty();
            announce(server, milestone, player, data);
        }
    }

    private static void announce(MinecraftServer server, Milestone milestone, ServerPlayer recipient, LegacyMilestoneSavedData data) {
        TitleService.unlockTitle(recipient, "milestone_" + milestone.getId());
        LegacyNetworking.broadcastMilestoneToast(server, milestone.getId(), recipient.getGameProfile().getName());
        broadcast(server, data);
    }

    public static void broadcast(MinecraftServer server, LegacyMilestoneSavedData data) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendTo(player, data);
        }
    }

    private static void sendTo(ServerPlayer player, LegacyMilestoneSavedData data) {
        LegacyNetworking.sendMilestones(player, data.all());
    }

    private static String formatDayTime(long dayTime) {
        long t = ((dayTime % 24000L) + 24000L) % 24000L;
        int hours = (int) ((t / 1000L + 6L) % 24L);
        int minutes = (int) ((t % 1000L) * 60L / 1000L);
        return String.format(Locale.ROOT, "%02d:%02d", hours, minutes);
    }

    private static boolean hasAdvancement(ServerPlayer player, String advancementId) {
        ResourceLocation id = ResourceLocation.tryParse(advancementId);
        if (id == null) {
            return false;
        }
        AdvancementHolder holder = player.server.getAdvancements().get(id);
        return holder != null && player.getAdvancements().getOrStartProgress(holder).isDone();
    }
}
