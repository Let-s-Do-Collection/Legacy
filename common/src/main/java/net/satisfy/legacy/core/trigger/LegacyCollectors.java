package net.satisfy.legacy.core.trigger;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.satisfy.legacy.Legacy;
import net.satisfy.legacy.core.data.LegacyTitleSavedData;
import net.satisfy.legacy.core.data.PlayerTitleData;

import java.util.List;
import java.util.function.Consumer;

public final class LegacyCollectors {
    private static final List<TagKey<Block>> MINE_TAGS = List.of(
            BlockTags.LOGS,
            BlockTags.SMALL_FLOWERS,
            TagKey.create(Registries.BLOCK, Legacy.identifier("stones")),
            TagKey.create(Registries.BLOCK, Legacy.identifier("ores"))
    );

    private LegacyCollectors() {
    }

    public static void init() {
        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            if (player == null) {
                return EventResult.pass();
            }
            boolean crop = isMatureCrop(state);
            boolean tagged = false;
            for (TagKey<Block> tag : MINE_TAGS) {
                if (state.is(tag)) {
                    tagged = true;
                    break;
                }
            }
            if (crop || tagged) {
                withData(player, data -> {
                    if (crop) {
                        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                        data.addCounter(Counters.harvest(id), 1);
                        data.addCounter(Counters.HARVEST_ANY, 1);
                    }
                    for (TagKey<Block> tag : MINE_TAGS) {
                        if (state.is(tag)) {
                            data.addCounter(Counters.mineTag(tag.location()), 1);
                        }
                    }
                });
            }
            return EventResult.pass();
        });

        BlockEvent.PLACE.register((level, pos, state, placer) -> {
            if (placer instanceof ServerPlayer player) {
                Block block = state.getBlock();
                ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
                withData(player, data -> {
                    data.addCounter(Counters.place(id), 1);
                    if (block instanceof SaplingBlock) {
                        data.addCounter(Counters.PLACE_SAPLINGS, 1);
                    }
                    if (block instanceof CropBlock) {
                        data.addCounter(Counters.PLACE_CROPS, 1);
                    }
                });
            }
            return EventResult.pass();
        });

        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, pos, face) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                ItemStack stack = player.getItemInHand(hand);
                if (stack.is(Items.BONE_MEAL)) {
                    ServerLevel level = serverPlayer.serverLevel();
                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof BonemealableBlock bonemealable
                            && bonemealable.isValidBonemealTarget(level, pos, state)) {
                        withData(serverPlayer, data -> data.addCounter(Counters.BONEMEAL_USED, 1));
                    }
                }
            }
            return EventResult.pass();
        });

        PlayerEvent.CHANGE_DIMENSION.register((player, oldLevel, newLevel) ->
                withData(player, data -> recordDimension(data, newLevel.location())));
    }

    public static void onJoin(ServerPlayer player, PlayerTitleData data) {
        recordDimension(data, player.level().dimension().location());
        sampleLocation(player, data);
    }

    public static void sampleLocation(ServerPlayer player, PlayerTitleData data) {
        ServerLevel level = player.serverLevel();
        BlockPos pos = player.blockPosition();

        Holder<Biome> biome = level.getBiome(pos);
        biome.unwrapKey().map(ResourceKey::location).ifPresent(id -> {
            if (data.markVisited(Counters.biome(id))) {
                data.addCounter(Counters.BIOMES_DISTINCT, 1);
            }
        });

        Registry<Structure> structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (Structure structure : level.structureManager().getAllStructuresAt(pos).keySet()) {
            StructureStart start = level.structureManager().getStructureAt(pos, structure);
            if (!start.isValid()) {
                continue;
            }
            ResourceLocation id = structures.getKey(structure);
            if (id != null && data.markVisited(Counters.structure(id))) {
                data.addCounter(Counters.STRUCTURES_DISTINCT, 1);
            }
        }
    }

    private static void recordDimension(PlayerTitleData data, ResourceLocation id) {
        if (id != null && data.markVisited(Counters.dimension(id))) {
            data.addCounter(Counters.DIMENSIONS_DISTINCT, 1);
        }
    }

    private static boolean isMatureCrop(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }
        if (block instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) >= 3;
        }
        return false;
    }

    private static void withData(ServerPlayer player, Consumer<PlayerTitleData> action) {
        LegacyTitleSavedData saved = LegacyTitleSavedData.get(player.server);
        PlayerTitleData data = saved.getOrCreate(player.getUUID());
        action.accept(data);
        saved.setDirty();
    }
}
