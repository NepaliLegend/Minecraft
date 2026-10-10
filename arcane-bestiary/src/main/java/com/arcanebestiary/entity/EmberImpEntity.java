package com.arcanebestiary.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
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
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** A small, spiteful fire spirit. Spits fireballs at range and bites when cornered. */
public class EmberImpEntity extends HostileEntity implements RangedAttackMob {
	public EmberImpEntity(EntityType<? extends EmberImpEntity> type, World world) {
		super(type, world);
		this.experiencePoints = 6;
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return HostileEntity.createHostileAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 16.0)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.32)
			.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 26.0)
			.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(1, new SwimGoal(this));
		this.goalSelector.add(2, new ProjectileAttackGoal(this, 1.1, 50, 14.0f));
		this.goalSelector.add(3, new MeleeAttackGoal(this, 1.2, true));
		this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.9));
		this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
		this.goalSelector.add(6, new LookAroundGoal(this));
		this.targetSelector.add(1, new RevengeGoal(this));
		this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}

	@Override
	public void shootAt(LivingEntity target, float pullProgress) {
		Vec3d dir = target.getEyePos().subtract(getEyePos()).normalize();
		SmallFireballEntity fireball = new SmallFireballEntity(getWorld(), this, dir.multiply(1.2));
		fireball.setPosition(getX() + dir.x, getEyeY() - 0.1, getZ() + dir.z);
		playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 0.8f, 1.5f);
		getWorld().spawnEntity(fireball);
	}

	@Override
	public void tickMovement() {
		if (getWorld().isClient) {
			if (random.nextInt(2) == 0) {
				getWorld().addParticle(ParticleTypes.FLAME, getParticleX(0.4), getRandomBodyY(), getParticleZ(0.4), 0, 0.02, 0);
			}
		} else if (isTouchingWater()) {
			damage(getDamageSources().drown(), 1.0f);
		}
		super.tickMovement();
	}

	@Override
	public boolean isFireImmune() {
		return true;
	}

	@Override
	public boolean tryAttack(net.minecraft.entity.Entity target) {
		boolean hit = super.tryAttack(target);
		if (hit) {
			target.setOnFireFor(4);
		}
		return hit;
	}

	@Override
	public float getPathfindingFavor(net.minecraft.util.math.BlockPos pos, net.minecraft.world.WorldView world) {
		return 0.0f;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_BLAZE_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_BLAZE_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_BLAZE_DEATH;
	}
}
