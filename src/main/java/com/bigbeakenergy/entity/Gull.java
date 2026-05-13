package com.bigbeakenergy.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
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
        this.setCanPickUpLoot(true);
    }

    public float flap;
    public float flapSpeed;
    public float oFlapSpeed;
    public float oFlap;
    private float flapping = 1.0F;
    private float nextFlap = 1.0F;

    @Override
    public boolean isFood(ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean canMate(final Animal partner) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
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
        this.goalSelector.addGoal(2, new Gull.PlayWithItemsGoal());
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new Gull.GullWanderGoal(this, 1.0));
    }

    // Movement?

    @Override
    protected PathNavigation createNavigation(final Level level) {
        FlyingPathNavigation flyingPathNavigation = new FlyingPathNavigation(this, level);
        flyingPathNavigation.setCanOpenDoors(false);
        flyingPathNavigation.setCanFloat(true);
        return flyingPathNavigation;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.calculateFlapping();
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
    protected void playStepSound(final BlockPos pos, final BlockState blockState) {
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

    private static class GullWanderGoal extends WaterAvoidingRandomFlyingGoal {
        public GullWanderGoal(final PathfinderMob mob, final double speedModifier) {
            super(mob, speedModifier);
        }

        @Nullable
        @Override
        protected Vec3 getPosition() {
            if (this.mob.isInWater()) {
                return LandRandomPos.getPos(this.mob, 15, 15);
            }

            if (this.mob.getRandom().nextFloat() < 0.5F) {
                Vec3 shorePos = this.getShorePos();
                if (shorePos != null) return shorePos;
            }

            return AirAndWaterRandomPos.getPos(this.mob, 8, 4, -2,
                    this.mob.getViewVector(0.0F).x,
                    this.mob.getViewVector(0.0F).z,
                    (float)(Math.PI / 2));
        }

        @Nullable
        private Vec3 getShorePos() {
            BlockPos mobPos = this.mob.blockPosition();
            BlockPos.MutableBlockPos abovePos = new BlockPos.MutableBlockPos();
            BlockPos.MutableBlockPos belowPos = new BlockPos.MutableBlockPos();

            for (BlockPos pos : BlockPos.betweenClosed(
                    Mth.floor(this.mob.getX() - 3.0),
                    Mth.floor(this.mob.getY() - 6.0),
                    Mth.floor(this.mob.getZ() - 3.0),
                    Mth.floor(this.mob.getX() + 3.0),
                    Mth.floor(this.mob.getY() + 6.0),
                    Mth.floor(this.mob.getZ() + 3.0)
            )) {
                if (!mobPos.equals(pos)) {
                    BlockState state = this.mob.level().getBlockState(belowPos.setWithOffset(pos, Direction.DOWN));
                    boolean canSitOn = state.is(BlockTags.SAND)
                            || state.is(Blocks.GRAVEL)
                            || state.is(Blocks.STONE)
                            || state.getFluidState().is(FluidTags.WATER);
                    if (canSitOn
                            && this.mob.level().isEmptyBlock(pos)
                            && this.mob.level().isEmptyBlock(abovePos.setWithOffset(pos, Direction.UP))
                            && this.mob.level().canSeeSky(pos)) {
                        return Vec3.atBottomCenterOf(pos);
                    }
                }
            }
            return null;
        }
    }
    @Override
    protected void checkFallDamage(final double ya, final boolean onGround, final BlockState onState, final BlockPos pos) {
    }

    // Play with food
    private class PlayWithItemsGoal extends Goal {
        private int cooldown;

        PlayWithItemsGoal() {
            Objects.requireNonNull(Gull.this);
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
                Gull.this.getNavigation().moveTo(items.get(0), 1.2F);
            }
            this.cooldown = 0;
        }

        @Override
        public void tick() {
            List<ItemEntity> items = Gull.this.level()
                    .getEntitiesOfClass(ItemEntity.class, Gull.this.getBoundingBox().inflate(8.0, 8.0, 8.0), Gull.ALLOWED_ITEMS);
            if (!items.isEmpty()) {
                ItemEntity target = items.get(0);
                Gull.this.getNavigation().moveTo(target, 1.2F);
                if (Gull.this.distanceToSqr(target) < 0.3) {
                    float angleOffset = (float)(Gull.this.getRandom().nextGaussian() * 0.5);
                    float yRot = Gull.this.getYRot() * (float)(Math.PI / 180.0) + angleOffset;
                    target.setDeltaMovement(
                            -Mth.sin(yRot) * 0.2F,
                            0.2F,
                            Mth.cos(yRot) * 0.2F
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
}