package com.bigbeakenergy.block;

import com.bigbeakenergy.ModBlocksRegistry;
import com.bigbeakenergy.entity.Cassowary;
import com.bigbeakenergy.entity.ModEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CassowaryEggBlock extends Block {
    public static final MapCodec<CassowaryEggBlock> CODEC = simpleCodec(CassowaryEggBlock::new);
    public static final IntegerProperty HATCH = BlockStateProperties.HATCH;
    public static final IntegerProperty EGGS = BlockStateProperties.EGGS;
    private static final VoxelShape SHAPE_SINGLE = Block.box(3.0, 0.0, 3.0, 12.0, 7.0, 12.0);
    private static final VoxelShape SHAPE_MULTIPLE = Block.column(14.0, 0.0, 7.0);

    @Override
    public @NonNull MapCodec<CassowaryEggBlock> codec() {
        return CODEC;
    }

    public CassowaryEggBlock(final BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, 0).setValue(EGGS, 1));
    }

    @Override
    public void stepOn(final @NonNull Level level, final @NonNull BlockPos pos, final @NonNull BlockState onState, final Entity entity) {
        if (!entity.isSteppingCarefully()) {
            this.destroyEgg(level, onState, pos, entity, 100);
        }

        super.stepOn(level, pos, onState, entity);
    }

    @Override
    public void fallOn(final @NonNull Level level, final @NonNull BlockState state, final @NonNull BlockPos pos, final @NonNull Entity entity, final double fallDistance) {
        if (!(entity instanceof Zombie)) {
            this.destroyEgg(level, state, pos, entity, 3);
        }

        super.fallOn(level, state, pos, entity, fallDistance);
    }

    private void destroyEgg(final Level level, final BlockState state, final BlockPos pos, final Entity entity, final int randomness) {
        if (state.is(ModBlocksRegistry.CASSOWARY_EGG)
                && level instanceof ServerLevel serverLevel
                && this.canDestroyEgg(serverLevel, entity)
                && level.getRandom().nextInt(randomness) == 0) {
            this.decreaseEggs(serverLevel, pos, state);
        }
    }

    private void decreaseEggs(final Level level, final BlockPos pos, final BlockState state) {
        level.playSound(null, pos, SoundEvents.TURTLE_EGG_BREAK, SoundSource.BLOCKS, 0.7F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        int numberOfEggs = state.getValue(EGGS);
        if (numberOfEggs <= 1) {
            level.destroyBlock(pos, false);
        } else {
            level.setBlock(pos, state.setValue(EGGS, numberOfEggs - 1), 2);
            level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(state));
            level.levelEvent(2001, pos, Block.getId(state));
        }
    }

    @Override
    protected void randomTick(final BlockState state, final @NonNull ServerLevel level, final @NonNull BlockPos pos, final @NonNull RandomSource random) {

        int hatch = state.getValue(HATCH);

        if (hatch < 2) {
            int chance = hatch == 0 ? 9 : 6;
            if (level.getRandom().nextInt(chance) == 0) {
                level.setBlock(pos, state.setValue(HATCH, hatch + 1), 2);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
            }
            return;
        }

        if (level.getRandom().nextInt(3) != 0) return;

        level.playSound(null, pos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS,
                0.7F, 0.9F + random.nextFloat() * 0.2F);

        level.removeBlock(pos, false);
        level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(state));

        for (int i = 0; i < state.getValue(EGGS); i++) {
            level.levelEvent(2001, pos, Block.getId(state));

            Cassowary cassowary = ModEntities.CASSOWARY.create(level, EntitySpawnReason.BREEDING);

            if (cassowary != null) {
                cassowary.setAge(-24000);
                cassowary.snapTo(
                        pos.getX() + 0.3 + i * 0.2,
                        pos.getY(),
                        pos.getZ() + 0.3,
                        0.0F,
                        0.0F
                );
                level.addFreshEntity(cassowary);
            }
        }
    }

    @Override
    public void playerDestroy(
            final @NonNull Level level, final @NonNull Player player, final @NonNull BlockPos pos, final @NonNull BlockState state, @Nullable final BlockEntity blockEntity, final @NonNull ItemStack destroyedWith
    ) {
        super.playerDestroy(level, player, pos, state, blockEntity, destroyedWith);
        this.decreaseEggs(level, pos, state);
    }

    @Override
    protected boolean canBeReplaced(final @NonNull BlockState state, final BlockPlaceContext context) {
        return !context.isSecondaryUseActive() && context.getItemInHand().is(this.asItem()) && state.getValue(EGGS) < 4 || super.canBeReplaced(state, context);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        return state.is(this) ? state.setValue(EGGS, Math.min(4, state.getValue(EGGS) + 1)) : super.getStateForPlacement(context);
    }

    @Override
    protected @NonNull VoxelShape getShape(final BlockState state, final @NonNull BlockGetter level, final @NonNull BlockPos pos, final @NonNull CollisionContext context) {
        return state.getValue(EGGS) == 1 ? SHAPE_SINGLE : SHAPE_MULTIPLE;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HATCH, EGGS);
    }

    private boolean canDestroyEgg(final ServerLevel level, final Entity entity) {
        if (entity instanceof Cassowary || entity instanceof Bat) {
            return false;
        } else {
            return entity instanceof LivingEntity && (entity instanceof Player || level.getGameRules().get(GameRules.MOB_GRIEFING));
        }
    }
}
