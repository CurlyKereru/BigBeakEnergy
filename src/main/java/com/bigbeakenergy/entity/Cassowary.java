package com.bigbeakenergy.entity;

import com.bigbeakenergy.ModBlocksRegistry;
import com.bigbeakenergy.ModItemsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import com.bigbeakenergy.block.CassowaryEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Cassowary extends Animal implements NeutralMob {

    // --- Egg state ---
    private static final EntityDataAccessor<Boolean> HAS_EGG =
            SynchedEntityData.defineId(Cassowary.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LAYING_EGG =
            SynchedEntityData.defineId(Cassowary.class, EntityDataSerializers.BOOLEAN);
    private int layEggCounter;

    public Cassowary(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    // --- Synched data ---

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HAS_EGG, false);
        builder.define(LAYING_EGG, false);
    }

    public boolean hasEgg() {
        return this.entityData.get(HAS_EGG);
    }

    private void setHasEgg(boolean value) {
        this.entityData.set(HAS_EGG, value);
    }

    public boolean isLayingEgg() {
        return this.entityData.get(LAYING_EGG);
    }

    private void setLayingEgg(boolean value) {
        this.layEggCounter = value ? 1 : 0;
        this.entityData.set(LAYING_EGG, value);
    }

    // --- Save/load ---

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("has_egg", this.hasEgg());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setHasEgg(input.getBooleanOr("has_egg", false));
    }

    // --- Breeding ---

    @Override
    public boolean canFallInLove() {
        return super.canFallInLove() && !this.hasEgg();
    }

    // --- Goals ---

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.55, true));
        this.goalSelector.addGoal(2, new CassowaryBreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new CassowaryLayEggGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.2, i -> i.is(Items.MELON), false));
        this.goalSelector.addGoal(3, new FollowParentGoal(this, 1.35));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new CassowaryHurtByTargetGoal());
        this.targetSelector.addGoal(2, new CassowaryAttackPlayersGoal());
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(4, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    // --- Attributes ---

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.18)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.TEMPT_RANGE, 16.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    // --- Anger (NeutralMob) ---

    private long persistentAngerEndTime;
    private EntityReference<LivingEntity> persistentAngerTarget;
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);

    @Override public void startPersistentAngerTimer() { this.setTimeToRemainAngry(PERSISTENT_ANGER_TIME.sample(this.random)); }
    @Override public void setPersistentAngerEndTime(long endTime) { this.persistentAngerEndTime = endTime; }
    @Override public long getPersistentAngerEndTime() { return this.persistentAngerEndTime; }
    @Override public void setPersistentAngerTarget(@org.jspecify.annotations.Nullable EntityReference<LivingEntity> target) { this.persistentAngerTarget = target; }
    @Override @org.jspecify.annotations.Nullable public EntityReference<LivingEntity> getPersistentAngerTarget() { return this.persistentAngerTarget; }

    // --- Offspring ---

    @Override
    public @Nullable Cassowary getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return ModEntities.CASSOWARY.create(level, EntitySpawnReason.BREEDING);
    }
    // --- Spawn ---

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        EntitySpawnReason reason, @Nullable SpawnGroupData spawnData) {
        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData);
        int chicks = (reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION)
                && random.nextInt(3) == 0 ? random.nextInt(3) + 1 : 0;
        for (int i = 0; i < chicks; i++) {
            Cassowary chick = ModEntities.CASSOWARY.create(level.getLevel(), EntitySpawnReason.JOCKEY);
            if (chick != null) {
                chick.setBaby(true);
                chick.setPos(this.getX(), this.getY(), this.getZ());
                level.addFreshEntity(chick);
            }
        }
        return spawnData;
    }

    // --- Loot ---

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int feathers = random.nextInt(4);
        if (feathers > 0) {
            spawnAtLocation(level, new ItemStack(Items.FEATHER, feathers));
        }
    }

    public boolean isFood(ItemStack stack) {
        return stack.is(Items.MELON);
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItemsRegistry.CASSOWARY_SPAWN_EGG);
    }

    // =========================================================
    // Inner goal classes
    // =========================================================

    private class CassowaryBreedGoal extends BreedGoal {
        private final Cassowary cassowary;

        CassowaryBreedGoal(Cassowary cassowary, double speedModifier) {
            super(cassowary, speedModifier);
            this.cassowary = cassowary;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !this.cassowary.hasEgg();
        }

        @Override
        protected void breed() {
            this.cassowary.setHasEgg(true);
            this.animal.setAge(6000);
            this.partner.setAge(6000);
            this.animal.resetLove();
            this.partner.resetLove();
        }
    }

    /***
     * Seek out a grass block and lay a single egg on top of it.
     */
    private class CassowaryLayEggGoal extends MoveToBlockGoal {
        private final Cassowary cassowary;

        CassowaryLayEggGoal(Cassowary cassowary, double speedModifier) {
            super(cassowary, speedModifier, 16);
            this.cassowary = cassowary;
        }

        @Override
        public boolean canUse() {
            return this.cassowary.hasEgg() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && this.cassowary.hasEgg();
        }

        @Override
        public void tick() {
            super.tick();
            BlockPos pos = this.cassowary.blockPosition();

            if (this.isReachedTarget()) {
                if (this.cassowary.layEggCounter < 1) {
                    this.cassowary.setLayingEgg(true);
                } else if (this.cassowary.layEggCounter > this.adjustedTickDelay(200)) {
                    Level level = this.cassowary.level();

                    level.playSound(null, pos,
                            SoundEvents.TURTLE_LAY_EGG,
                            SoundSource.BLOCKS,
                            0.3F,
                            0.9F + level.getRandom().nextFloat() * 0.2F);

                    BlockPos eggPos = this.blockPos.above();

                    BlockState eggState = ModBlocksRegistry.CASSOWARY_EGG.defaultBlockState()
                            .setValue(CassowaryEggBlock.EGGS, random.nextInt(4) + 1);
                    level.setBlock(eggPos, eggState, 3);
                    level.gameEvent(GameEvent.BLOCK_PLACE, eggPos,
                            GameEvent.Context.of(this.cassowary, eggState));

                    this.cassowary.setHasEgg(false);
                    this.cassowary.setLayingEgg(false);
                    this.cassowary.setInLoveTime(600);
                }

                if (this.cassowary.isLayingEgg()) {
                    this.cassowary.layEggCounter++;
                }
            }
        }

        /**
         * Valid nest site: grass block with empty space above for the egg.
         */
        @Override
        protected boolean isValidTarget(LevelReader level, BlockPos pos) {
            return level.getBlockState(pos).is(Blocks.GRASS_BLOCK)
                    && level.isEmptyBlock(pos.above());
        }

    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isAlive() && this.isLayingEgg() && this.layEggCounter >= 1 && this.layEggCounter % 5 == 0) {
            BlockPos pos = this.blockPosition();
            if (this.level().getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)) {
                this.level().levelEvent(2001, pos, Block.getId(this.level().getBlockState(pos.below())));
                this.gameEvent(GameEvent.ENTITY_ACTION);
            }
        }
    }


    // --- Aggression goals ---

    private class CassowaryAttackPlayersGoal extends NearestAttackableTargetGoal<Player> {
        public CassowaryAttackPlayersGoal() {
            Objects.requireNonNull(Cassowary.this);
            super(Cassowary.this, Player.class, 20, true, true, null);
        }

        @Override
        public boolean canUse() {
            if (Cassowary.this.isBaby()) return false;
            if (super.canUse()) {
                for (Cassowary chick : Cassowary.this.level().getEntitiesOfClass(
                        Cassowary.class,
                        Cassowary.this.getBoundingBox().inflate(8.0, 4.0, 8.0))) {
                    if (chick.isBaby()) return true;
                }
            }
            return false;
        }
    }

    private class CassowaryHurtByTargetGoal extends HurtByTargetGoal {
        public CassowaryHurtByTargetGoal() {
            Objects.requireNonNull(Cassowary.this);
            super(Cassowary.this);
        }

        @Override
        public void start() {
            super.start();
            if (Cassowary.this.isBaby()) {
                this.alertOthers();
                this.stop();
            }
        }

        @Override
        protected void alertOther(Mob other, LivingEntity hurtByMob) {
            if (other instanceof Cassowary && !other.isBaby()) {
                super.alertOther(other, hurtByMob);
            }
        }
    }
}
