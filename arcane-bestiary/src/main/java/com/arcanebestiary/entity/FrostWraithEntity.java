package com.arcanebestiary.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** A restless spirit of the tundra that hurls shards of ice from a distance. */
public class FrostWraithEntity extends HostileEntity implements RangedAttackMob {
	public FrostWraithEntity(EntityType<? extends FrostWraithEntity> type, World world) {
		super(type, world);
		this.experiencePoints = 8;
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return HostileEntity.createHostileAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 26.0)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.26)
			.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 28.0)
			.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(1, new SwimGoal(this));
		this.goalSelector.add(2, new ProjectileAttackGoal(this, 1.0, 40, 18.0f));
		this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
		this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
		this.goalSelector.add(6, new LookAroundGoal(this));
		this.targetSelector.add(1, new RevengeGoal(this));
		this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}

	@Override
	public void shootAt(LivingEntity target, float pullProgress) {
		FrostBoltEntity bolt = new FrostBoltEntity(getWorld(), this, 4.0f);
		Vec3d dir = target.getEyePos().subtract(getEyePos());
		bolt.setPosition(getX(), getEyeY() - 0.2, getZ());
		bolt.setVelocity(dir.x, dir.y + Math.sqrt(dir.x * dir.x + dir.z * dir.z) * 0.1, dir.z, 1.3f, 4.0f);
		playSound(SoundEvents.ENTITY_SNOW_GOLEM_SHOOT, 1.0f, 0.6f);
		getWorld().spawnEntity(bolt);
	}

	@Override
	public void tickMovement() {
		if (getWorld().isClient && random.nextInt(3) == 0) {
			getWorld().addParticle(ParticleTypes.SNOWFLAKE, getParticleX(0.5), getRandomBodyY(), getParticleZ(0.5), 0, -0.02, 0);
		}
		super.tickMovement();
	}

	@Override
	public boolean canFreeze() {
		return false;
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (source.isIn(DamageTypeTags.IS_FIRE)) {
			amount *= 1.5f;
		}
		return super.damage(source, amount);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_STRAY_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_STRAY_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_STRAY_DEATH;
	}
}
