package com.bigbeakenergy.entity;

import com.bigbeakenergy.ModItemsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class Gull extends Animal implements FlyingAnimal {

    public static final Predicate<ItemEntity> ALLOWED_ITEMS = e ->
            !e.hasPickUpDelay() &&
                    e.isAlive() &&
                    e.getItem().get(DataComponents.FOOD) != null;
    public Gull(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 10, false);
        this.setPathfindingMalus(PathType.FIRE_IN_NEIGHBOR, -1.0F);
        this.setPathfindingMalus(PathType.FIRE, -1.0F);
    }

    public float flap;
    public float flapSpeed;
    public float oFlapSpeed;
    public float oFlap;
    private float flapping = 1.0F;
    private float nextFlap = 1.0F;

    private BlockPos homePos = BlockPos.ZERO;
    private boolean goingHome;
    public void setHomePos(final BlockPos pos) {
        this.homePos = pos;
    }
    private int ticksInWater = 0;

    @Override
    public boolean isFood(@NonNull ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean canMate(final @NonNull Animal partner) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(@NonNull ServerLevel level, @NonNull AgeableMob partner) {
        return null;
    }

    // Attributes
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 5.0)
                .add(Attributes.FLYING_SPEED, 1.0F)
                .add(Attributes.MOVEMENT_SPEED, 0.2F)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.TEMPT_RANGE, 16.0)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    // Goals

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new GullPanicGoal(this, 2.0));
        this.goalSelector.addGoal(2, new GullPlayWithItemsGoal());
        this.goalSelector.addGoal(3, new GullChaseDolphinGoal());
        this.goalSelector.addGoal(4, new GullGoToWaterGoal(this, 1.0));
        this.goalSelector.addGoal(5, new GullGoHomeGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new GullWanderGoal(this, 1.0));
    }

    // Movement?

    @Override
    protected @NonNull PathNavigation createNavigation(final @NonNull Level level) {
        FlyingPathNavigation flyingPathNavigation = new FlyingPathNavigation(this, level);
        flyingPathNavigation.setCanOpenDoors(false);
        flyingPathNavigation.setCanFloat(true);
        return flyingPathNavigation;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.calculateFlapping();
        if (this.isInWater()) {
            this.ticksInWater++;
        } else {
            this.ticksInWater = 0;
        }
    }

    private void calculateFlapping() {
        this.oFlap = this.flap;
        this.oFlapSpeed = this.flapSpeed;
        this.flapSpeed = this.flapSpeed + (!this.onGround() ? 4 : -1) * 0.3F;
        this.flapSpeed = Mth.clamp(this.flapSpeed, 0.0F, 1.0F);
        if (!this.onGround() && this.flapping < 1.0F) {
            this.flapping = 1.0F;
        }

        this.flapping *= 0.9F;
        Vec3 movement = this.getDeltaMovement();
        if (!this.onGround() && movement.y < 0.0) {
            this.setDeltaMovement(movement.multiply(1.0, 0.6, 1.0));
        }

        this.flap = this.flap + this.flapping * 0.75F;
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    @Override
    protected void playStepSound(final @NonNull BlockPos pos, final @NonNull BlockState blockState) {
        this.playSound(SoundEvents.PARROT_STEP, 0.15F, 1.0F);
    }

    @Override
    protected boolean isFlapping() {
        return this.flyDist > this.nextFlap;
    }

    @Override
    protected void onFlap() {
        this.playSound(SoundEvents.PARROT_FLY, 0.15F, 1.0F);
        this.nextFlap = this.flyDist + this.flapSpeed / 2.0F;
    }

    private class GullWanderGoal extends WaterAvoidingRandomFlyingGoal {
        public GullWanderGoal(final PathfinderMob mob, final double speedModifier) {
            super(mob, speedModifier);
        }

        @Nullable
        @Override
        protected Vec3 getPosition() {
            if (Gull.this.goingHome) {
                return null;
            }
            if (this.mob.isInWater()) {
                return LandRandomPos.getPos(this.mob, 15, 15);
            }
            return AirAndWaterRandomPos.getPos(this.mob, 8, 4, -2,
                    this.mob.getViewVector(0.0F).x,
                    this.mob.getViewVector(0.0F).z,
                    (float)(Math.PI / 2));
        }
    }
    @Override
    protected void checkFallDamage(final double ya, final boolean onGround, final @NonNull BlockState onState, final @NonNull BlockPos pos) {
    }

    // Play with food
    private class GullPlayWithItemsGoal extends Goal {
        private int cooldown;

        GullPlayWithItemsGoal() {
            super();
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.cooldown > Gull.this.tickCount) {
                return false;
            }
            List<ItemEntity> items = Gull.this.level()
                    .getEntitiesOfClass(ItemEntity.class, Gull.this.getBoundingBox().inflate(8.0, 8.0, 8.0), Gull.ALLOWED_ITEMS);
            return !items.isEmpty();
        }

        @Override
        public void start() {
            List<ItemEntity> items = Gull.this.level()
                    .getEntitiesOfClass(ItemEntity.class, Gull.this.getBoundingBox().inflate(8.0, 8.0, 8.0), Gull.ALLOWED_ITEMS);
            if (!items.isEmpty()) {
                Gull.this.getNavigation().moveTo(items.getFirst(), 1.2F);
            }
            this.cooldown = 0;
        }

        @Override
        public void tick() {
            List<ItemEntity> items = Gull.this.level()
                    .getEntitiesOfClass(ItemEntity.class, Gull.this.getBoundingBox().inflate(8.0, 8.0, 8.0), Gull.ALLOWED_ITEMS);
            if (!items.isEmpty()) {
                ItemEntity target = items.getFirst();
                Gull.this.getNavigation().moveTo(target, 1.2F);
                if (Gull.this.distanceToSqr(target) < 0.6) {
                    float angleOffset = (float)(Gull.this.getRandom().nextGaussian() * 0.5);
                    float yRot = Gull.this.getYRot() * (float)(Math.PI / 180.0) + angleOffset;
                    target.setDeltaMovement(
                            -Mth.sin(yRot) * 0.12F,
                            0.34F,
                            Mth.cos(yRot) * 0.12F
                    );
                    target.hurtMarked = true;
                    target.setPickUpDelay(10);
                }
            }
        }

        @Override
        public void stop() {
            this.cooldown = Gull.this.tickCount + Gull.this.random.nextInt(100);
        }
    }

    // Custom panic goal
    private class GullPanicGoal extends Goal {
        private final double speed;
        private boolean isRunning;

        GullPanicGoal(Gull gull, double speed) {
            this.speed = speed;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return this.shouldPanic();
        }

        @Override
        public boolean canContinueToUse() {
            return this.shouldPanic() || !Gull.this.getNavigation().isDone();
        }

        private boolean shouldPanic() {
            if (Gull.this.getLastHurtByMob() != null || Gull.this.isOnFire() || Gull.this.isFreezing()) {
                return true;
            }
            Player player = Gull.this.level().getNearestPlayer(Gull.this, 8.0);
            return player != null && player.isSprinting();
        }

        @Override
        public void start() {
            this.findNewPosition();
            this.isRunning = true;
        }

        @Override
        public void stop() {
            this.isRunning = false;
        }

        @Override
        public void tick() {
            if (Gull.this.getNavigation().isDone()) {
                this.findNewPosition();
            }
        }

        private void findNewPosition() {
            Vec3 fleePos = AirAndWaterRandomPos.getPos(Gull.this, 10, 7, -2, 0, 0, Math.PI);
            if (fleePos != null) {
                Gull.this.getNavigation().moveTo(fleePos.x, fleePos.y, fleePos.z, speed);
            }
        }
    }

    //Custom spawn rule

    public static boolean checkGullSpawnRules(EntityType<Gull> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        boolean brightEnoughToSpawn = EntitySpawnReason.ignoresLightRequirements(reason) || Animal.isBrightEnoughToSpawn(level, pos);
        BlockState below = level.getBlockState(pos.below());
        return (below.is(Blocks.SAND) || below.is(Blocks.GRAVEL) || below.is(Blocks.STONE)
                || below.is(Blocks.MUD) || below.is(Blocks.DIORITE) || below.is(Blocks.ANDESITE)
                || below.is(Blocks.GRANITE) || below.is(BlockTags.ANIMALS_SPAWNABLE_ON))
                && brightEnoughToSpawn;
    }

    //Spawn Egg Pick Block and rule override, also potato
    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItemsRegistry.GULL_SPAWN_EGG);
    }

    @Override
    public @NonNull InteractionResult mobInteract(Player player, @NonNull InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        if (item.getItem() == Items.BAKED_POTATO) {
            Gull.this.setHomePos(Gull.this.blockPosition());
            if (!player.getAbilities().instabuild) {
                item.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        if (item.getItem() == ModItemsRegistry.GULL_SPAWN_EGG && this.level() instanceof ServerLevel serverLevel) {
            Gull gull = ModEntities.GULL.create(serverLevel, EntitySpawnReason.SPAWN_ITEM_USE);
            if (gull != null) {
                gull.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                serverLevel.addFreshEntity(gull);
                if (!player.getAbilities().instabuild) {
                    item.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    // Home AI
    @Override
    protected void addAdditionalSaveData(final @NonNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("home_pos", BlockPos.CODEC, this.homePos);
    }

    @Override
    protected void readAdditionalSaveData(final ValueInput input) {
        this.setHomePos((BlockPos)input.read("home_pos", BlockPos.CODEC).orElse(this.blockPosition()));
        super.readAdditionalSaveData(input);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
            final @NonNull ServerLevelAccessor level, final @NonNull DifficultyInstance difficulty, final @NonNull EntitySpawnReason spawnReason, @Nullable final SpawnGroupData groupData
    ) {
        this.setHomePos(this.blockPosition());
        return super.finalizeSpawn(level, difficulty, spawnReason, groupData);
    }

    @Override
    public float getWalkTargetValue(final BlockPos pos, final LevelReader level) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(Blocks.SAND) || below.is(Blocks.GRAVEL) || below.is(Blocks.STONE)
                || below.is(Blocks.MUD) || below.is(Blocks.DIORITE) || below.is(Blocks.ANDESITE)
                || below.is(Blocks.GRANITE)) {
            return 10.0F;
        }
        return level.getPathfindingCostFromLightLevels(pos);
    }

    private class GullGoHomeGoal extends Goal {
        private final Gull gull;
        private final double speedModifier;
        private boolean stuck;
        private int closeToHomeTryTicks;
        private static final int GIVE_UP_TICKS = 600;

        GullGoHomeGoal(final Gull gull, final double speedModifier) {
            this.gull = gull;
            this.speedModifier = speedModifier;
        }

        @Override
        public boolean canUse() {
            boolean inWaterTooLong = Gull.this.ticksInWater >= 6000
                    && Gull.this.getRandom().nextInt(reducedTickDelay(200)) == 0;
            boolean tooFarFromHome = !Gull.this.homePos.closerToCenterThan(Gull.this.position(), 64.0)
                    && Gull.this.getRandom().nextInt(reducedTickDelay(200)) == 0;
            return inWaterTooLong || tooFarFromHome;
        }
        @Override
        public void start() {
            this.gull.goingHome = true;
            this.stuck = false;
            this.closeToHomeTryTicks = 0;
        }

        @Override
        public void stop() {
            this.gull.goingHome = false;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.gull.homePos.closerToCenterThan(this.gull.position(), 7.0) && !this.stuck && this.closeToHomeTryTicks <= this.adjustedTickDelay(600);
        }

        @Override
        public void tick() {
            BlockPos homePos = Gull.this.homePos;
            boolean closeToHome = homePos.closerToCenterThan(Gull.this.position(), 16.0);
            if (closeToHome) {
                this.closeToHomeTryTicks++;
            }

            if (Gull.this.getNavigation().isDone()) {
                Vec3 homePosVec = Vec3.atBottomCenterOf(homePos);
                Vec3 homeDir = homePosVec.subtract(Gull.this.position()).normalize();
                Vec3 nextPos = AirAndWaterRandomPos.getPos(Gull.this, 16, 4, -2, homeDir.x, homeDir.z, (float)(Math.PI / 4));

                if (nextPos == null) {
                    this.stuck = true;
                    return;
                }
                Gull.this.getNavigation().moveTo(nextPos.x, nextPos.y, nextPos.z, this.speedModifier);
            }
        }
    }
    private class GullGoToWaterGoal extends MoveToBlockGoal {
        private static final int GIVE_UP_TICKS = 1200;

        GullGoToWaterGoal(Gull gull, double speedModifier) {
            super(Gull.this, speedModifier, 32, 6);
            this.verticalSearchStart = -1;
        }

        @Override
        public boolean canUse() {
            return !Gull.this.goingHome
                    && !Gull.this.isInWater()
                    && Gull.this.getRandom().nextInt(reducedTickDelay(300)) == 0
                    && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return this.tryTicks <= 1200 && this.isValidTarget(Gull.this.level(), this.blockPos);
        }

        @Override
        public boolean shouldRecalculatePath() {
            return this.tryTicks % 160 == 0;
        }

        @Override
        protected boolean isValidTarget(final LevelReader level, final @NonNull BlockPos pos) {
            return level.getBlockState(pos).is(Blocks.WATER);
        }
    }
    // gulls chase dolphins
    private class GullChaseDolphinGoal extends Goal {
        private net.minecraft.world.entity.animal.dolphin.Dolphin target;

        GullChaseDolphinGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            List<Dolphin> dolphins = Gull.this.level().getEntitiesOfClass(
                    Dolphin.class,
                    Gull.this.getBoundingBox().inflate(15.0, 4.0, 15.0)
            );
            this.target = dolphins.isEmpty() ? null : dolphins.getFirst();
            return this.target != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.target == null || !this.target.isAlive()) return false;
            if (!this.target.closerThan(Gull.this, 15.0)) return false;
            // give up if dolphin is more than 3 blocks below water surface
            return !this.target.isUnderWater() ||
                    !(Gull.this.getY() - this.target.getY() > 4.0);
        }

        @Override
        public void tick() {
            Gull.this.getNavigation().moveTo(this.target, 1.4);
        }

        @Override
        public void stop() {
            this.target = null;
            Gull.this.getNavigation().stop();
        }
    }
}

