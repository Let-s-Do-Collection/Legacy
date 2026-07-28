package net.satisfy.legacy.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.satisfy.legacy.api.LegacyAPI;
import net.satisfy.legacy.core.data.LegacyTitleSavedData;
import net.satisfy.legacy.core.data.PlayerTitleData;
import net.satisfy.legacy.core.title.Title;
import net.satisfy.legacy.core.title.TitleManager;
import net.satisfy.legacy.core.title.TitleRequirement;

public final class LegacyCommands {
    private static final SuggestionProvider<CommandSourceStack> TITLE_IDS = (context, builder) ->
            SharedSuggestionProvider.suggest(TitleManager.INSTANCE.all().stream().map(t -> t.getId()), builder);

    private LegacyCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("legacy")
                .then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
                .then(Commands.literal("list").executes(ctx -> list(ctx.getSource())))
                .then(Commands.literal("titles").executes(ctx -> titles(ctx.getSource())))
                .then(Commands.literal("info")
                        .then(Commands.argument("id", StringArgumentType.string()).suggests(TITLE_IDS)
                                .executes(ctx -> info(ctx.getSource(), StringArgumentType.getString(ctx, "id")))))
                .then(Commands.literal("eval").executes(ctx -> eval(ctx.getSource())))
                .then(Commands.literal("clear").executes(ctx -> clear(ctx.getSource())))
                .then(Commands.literal("reload").requires(source -> source.hasPermission(2))
                        .executes(ctx -> reload(ctx.getSource())))
                .then(Commands.literal("grant").requires(source -> source.hasPermission(2))
                        .then(Commands.argument("id", StringArgumentType.string()).suggests(TITLE_IDS)
                                .executes(ctx -> grant(ctx.getSource(), StringArgumentType.getString(ctx, "id")))))
                .then(Commands.literal("revoke").requires(source -> source.hasPermission(2))
                        .then(Commands.argument("id", StringArgumentType.string()).suggests(TITLE_IDS)
                                .executes(ctx -> revoke(ctx.getSource(), StringArgumentType.getString(ctx, "id"))))));
    }

    private static int status(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        PlayerTitleData data = LegacyTitleSavedData.get(player.server).getOrCreate(player.getUUID());
        source.sendSuccess(() -> Component.literal(
                "[legacy] titles loaded: " + TitleManager.INSTANCE.all().size()
                        + " | your unlocked: " + data.getUnlocked().size()
                        + " | active: '" + data.getActive() + "'"
                        + " | dimension: " + player.level().dimension().location()), false);
        source.sendSuccess(() -> Component.literal("[legacy] unlocked = " + data.getUnlocked()), false);
        return 1;
    }

    private static int list(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("[legacy] defined titles: "
                + TitleManager.INSTANCE.all().stream().map(t -> t.getId()).toList()), false);
        return 1;
    }

    private static int titles(CommandSourceStack source) {
        var all = TitleManager.INSTANCE.allSorted();
        source.sendSuccess(() -> Component.literal("[legacy] " + all.size() + " title(s):").withStyle(ChatFormatting.GOLD), false);
        for (Title title : all) {
            source.sendSuccess(() -> Component.literal(" - " + title.getId())
                    .withStyle(title.getRarity().getColor())
                    .append(Component.literal(" [" + title.getRarity().name().toLowerCase(java.util.Locale.ROOT)
                            + ", " + title.getTrigger().name().toLowerCase(java.util.Locale.ROOT)
                            + (title.hidden ? ", hidden" : "") + "]").withStyle(ChatFormatting.DARK_GRAY)), false);
        }
        return all.size();
    }

    private static int info(CommandSourceStack source, String id) {
        Title title = TitleManager.INSTANCE.get(id).orElse(null);
        if (title == null) {
            source.sendFailure(Component.literal("[legacy] unknown title id '" + id + "'"));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("[legacy] " + title.getId()).withStyle(title.getRarity().getColor()), false);
        source.sendSuccess(() -> Component.literal("  name: ").append(title.styledDisplayName())
                .append(Component.literal(" (" + title.getTranslationKey() + ")").withStyle(ChatFormatting.DARK_GRAY)), false);
        source.sendSuccess(() -> Component.literal("  placement: " + title.getPlacement().name().toLowerCase(java.util.Locale.ROOT)
                + " | rarity: " + title.getRarity().name().toLowerCase(java.util.Locale.ROOT)
                + " | category: " + (title.category == null || title.category.isEmpty() ? "-" : title.category)
                + " | hidden: " + title.hidden), false);
        source.sendSuccess(() -> Component.literal("  trigger: " + title.getTrigger().name().toLowerCase(java.util.Locale.ROOT)
                + " | requirement: " + describeRequirement(title.requirement)), false);
        return 1;
    }

    private static String describeRequirement(TitleRequirement req) {
        if (req == null) {
            return "none";
        }
        StringBuilder sb = new StringBuilder();
        if (req.stat != null) sb.append("stat=").append(req.statType).append(':').append(req.stat).append(" x").append(req.amount);
        if (req.counter != null) sb.append("counter=").append(req.counter).append(" x").append(req.amount);
        if (req.advancement != null) sb.append("advancement=").append(req.advancement);
        if (req.id != null) sb.append("id=").append(req.id);
        return sb.isEmpty() ? "none" : sb.toString();
    }

    private static int reload(CommandSourceStack source) {
        int count = TitleService.reloadTitles(source.getServer());
        source.sendSuccess(() -> Component.literal("[legacy] reloaded titles.json - " + count + " title(s) loaded").withStyle(ChatFormatting.GREEN), true);
        return count;
    }

    private static int eval(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        int unlocked = TitleService.forceEvaluate(player);
        source.sendSuccess(() -> Component.literal("[legacy] evaluation done, newly unlocked: " + unlocked), false);
        return 1;
    }

    private static int clear(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        TitleService.setActive(player, "");
        source.sendSuccess(() -> Component.literal("[legacy] active title cleared"), false);
        return 1;
    }

    private static int grant(CommandSourceStack source, String id) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        boolean ok = LegacyAPI.grant(player, id);
        source.sendSuccess(() -> Component.literal("[legacy] grant '" + id + "': " + (ok ? "unlocked" : "no change / unknown id")), false);
        return ok ? 1 : 0;
    }

    private static int revoke(CommandSourceStack source, String id) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        boolean ok = LegacyAPI.revoke(player, id);
        source.sendSuccess(() -> Component.literal("[legacy] revoke '" + id + "': " + (ok ? "removed" : "was not unlocked")), false);
        return ok ? 1 : 0;
    }
}
