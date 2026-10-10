package com.arcanebestiary.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

/** A shard of magical ice that chills whatever it strikes. */
public class FrostBoltEntity extends ThrownItemEntity {
	private float damage = 5.0f;

	public FrostBoltEntity(EntityType<? extends FrostBoltEntity> type, World world) {
		super(type, world);
	}

	public FrostBoltEntity(World world, LivingEntity owner, float damage) {
		super(com.arcanebestiary.registry.ModEntities.FROST_BOLT, owner, world);
		this.damage = damage;
	}

	@Override
	protected Item getDefaultItem() {
		return Items.BLUE_ICE;
	}

	@Override
	protected double getGravity() {
		return 0.02;
	}

	@Override
	public void tick() {
		super.tick();
		if (getWorld() instanceof ServerWorld world) {
			world.spawnParticles(ParticleTypes.SNOWFLAKE, getX(), getY(), getZ(), 2, 0.05, 0.05, 0.05, 0.0);
			if (age > 100) {
				discard();
			}
		}
	}

	@Override
	protected void onEntityHit(EntityHitResult hit) {
		super.onEntityHit(hit);
		if (hit.getEntity() instanceof LivingEntity target && !getWorld().isClient) {
			DamageSource source = getDamageSources().thrown(this, getOwner());
			if (target.damage(source, damage)) {
				target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 1), getOwner());
				target.setFrozenTicks(target.getMinFreezeDamageTicks() + 60);
			}
		}
	}

	@Override
	protected void onCollision(HitResult hit) {
		super.onCollision(hit);
		if (getWorld() instanceof ServerWorld world) {
			world.spawnParticles(ParticleTypes.SNOWFLAKE, getX(), getY(), getZ(), 12, 0.2, 0.2, 0.2, 0.08);
			world.playSound(null, getX(), getY(), getZ(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.HOSTILE, 0.6f, 1.6f);
			discard();
		}
	}
}
