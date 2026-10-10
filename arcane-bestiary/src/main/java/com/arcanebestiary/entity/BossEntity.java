package com.arcanebestiary.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Shared behaviour for all Arcane Bestiary bosses: boss bar, summoning intro, damage cap,
 * out-of-combat regeneration and an enrage phase below half health.
 */
public abstract class BossEntity extends HostileEntity {
	private static final TrackedData<Boolean> ENRAGED = DataTracker.registerData(BossEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final int INTRO_TICKS = 60;
	private static final float MAX_DAMAGE_PER_HIT = 40.0f;

	private final ServerBossBar bossBar;
	private int introTicks = INTRO_TICKS;
	private int ticksSinceTarget;

	protected BossEntity(EntityType<? extends BossEntity> type, World world, BossBar.Color color) {
		super(type, world);
		this.bossBar = new ServerBossBar(getDisplayName(), color, BossBar.Style.NOTCHED_10);
		this.experiencePoints = 500;
		this.setPersistent();
	}

	protected static net.minecraft.entity.attribute.DefaultAttributeContainer.Builder createBossAttributes(
		double health, double attack, double speed, double armor) {
		return HostileEntity.createHostileAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, health)
			.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, attack)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, speed)
			.add(EntityAttributes.GENERIC_ARMOR, armor)
			.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
			.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(ENRAGED, false);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, false));
		this.goalSelector.add(7, new WanderAroundFarGoal(this, 0.8));
		this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 24.0f));
		this.goalSelector.add(8, new LookAroundGoal(this));
		this.targetSelector.add(1, new RevengeGoal(this));
		this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}

	public boolean isEnraged() {
		return this.dataTracker.get(ENRAGED);
	}

	/** Called once when the boss drops below half health. */
	protected void onEnrage() {
		playSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL, 3.0f, 0.7f);
		if (getWorld() instanceof ServerWorld world) {
			world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getBodyY(0.5), getZ(), 1, 0, 0, 0, 0);
		}
	}

	/** Per-tick ability logic; only invoked server side while the boss has a living target. */
	protected abstract void tickAbilities(LivingEntity target);

	protected abstract void spawnIntroParticles(ServerWorld world);

	protected abstract Text getBossName();

	@Override
	public Text getDisplayName() {
		return hasCustomName() ? super.getDisplayName() : getBossName();
	}

	@Override
	protected void mobTick() {
		super.mobTick();
		if (!(getWorld() instanceof ServerWorld world)) {
			return;
		}

		if (introTicks > 0) {
			introTicks--;
			spawnIntroParticles(world);
			if (introTicks == 0) {
				playSound(SoundEvents.ENTITY_WITHER_SPAWN, 2.0f, 1.0f);
			}
		}

		if (!isEnraged() && getHealth() <= getMaxHealth() * 0.5f) {
			this.dataTracker.set(ENRAGED, true);
			onEnrage();
		}

		LivingEntity target = getTarget();
		if (target != null && target.isAlive() && introTicks <= 0) {
			ticksSinceTarget = 0;
			tickAbilities(target);
		} else if (target == null) {
			ticksSinceTarget++;
			if (ticksSinceTarget > 200 && age % 20 == 0) {
				heal(4.0f);
			}
		}

		bossBar.setPercent(getHealth() / getMaxHealth());
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			return super.damage(source, amount);
		}
		if (introTicks > 0) {
			return false;
		}
		return super.damage(source, Math.min(amount, MAX_DAMAGE_PER_HIT));
	}

	@Override
	public void setCustomName(@Nullable Text name) {
		super.setCustomName(name);
		bossBar.setName(getDisplayName());
	}

	@Override
	public void onStartedTrackingBy(ServerPlayerEntity player) {
		super.onStartedTrackingBy(player);
		bossBar.addPlayer(player);
	}

	@Override
	public void onStoppedTrackingBy(ServerPlayerEntity player) {
		super.onStoppedTrackingBy(player);
		bossBar.removePlayer(player);
	}

	@Override
	public void onRemoved() {
		super.onRemoved();
		bossBar.clearPlayers();
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		if (hasCustomName()) {
			bossBar.setName(getDisplayName());
		}
		introTicks = nbt.contains("IntroTicks") ? nbt.getInt("IntroTicks") : 0;
		this.dataTracker.set(ENRAGED, getHealth() <= getMaxHealth() * 0.5f);
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("IntroTicks", introTicks);
	}

	@Override
	public boolean canImmediatelyDespawn(double distanceSquared) {
		return false;
	}

	@Override
	public boolean canHaveStatusEffect(net.minecraft.entity.effect.StatusEffectInstance effect) {
		return effect.getEffectType() != net.minecraft.entity.effect.StatusEffects.WITHER && super.canHaveStatusEffect(effect);
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_IRON_GOLEM_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_WITHER_DEATH;
	}

	@Override
	public boolean isPushedByFluids() {
		return false;
	}

	protected int countNearby(EntityType<?> type, double radius) {
		return getWorld().getEntitiesByType(type, getBoundingBox().expand(radius), e -> true).size();
	}

	protected void summonMinions(EntityType<? extends HostileEntity> type, int count, LivingEntity target) {
		if (!(getWorld() instanceof ServerWorld world)) {
			return;
		}
		for (int i = 0; i < count; i++) {
			double angle = random.nextDouble() * Math.PI * 2;
			double dist = 3.0 + random.nextDouble() * 3.0;
			BlockPos pos = BlockPos.ofFloored(getX() + Math.cos(angle) * dist, getY(), getZ() + Math.sin(angle) * dist);
			HostileEntity minion = type.spawn(world, pos, SpawnReason.MOB_SUMMONED);
			if (minion != null) {
				minion.setTarget(target);
				world.spawnParticles(ParticleTypes.POOF, minion.getX(), minion.getY() + 0.5, minion.getZ(), 12, 0.3, 0.5, 0.3, 0.05);
			}
		}
	}

	protected Vec3d directionTo(LivingEntity target) {
		return target.getEyePos().subtract(getEyePos()).normalize();
	}

	protected void knockAway(LivingEntity entity, double strength, double lift) {
		double dx = entity.getX() - getX();
		double dz = entity.getZ() - getZ();
		double len = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
		entity.addVelocity(dx / len * strength, lift, dz / len * strength);
		entity.velocityModified = true;
	}
}
