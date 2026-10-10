package com.arcanebestiary.entity;

import com.arcanebestiary.registry.ModEntities;
import com.arcanebestiary.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Boss of the Nether-forged army. Volleys of fireballs at range, a telegraphed inferno nova up close,
 * and below half health it calls Ember Imps to its side.
 */
public class InfernalWardenEntity extends BossEntity {
	private static final double NOVA_RADIUS = 7.0;

	private int fireballCooldown = 40;
	private int novaCooldown = 140;
	private int novaWindup = 0;
	private int summonCooldown = 200;

	public InfernalWardenEntity(EntityType<? extends InfernalWardenEntity> type, World world) {
		super(type, world, BossBar.Color.RED);
		equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.EMBERBRAND));
		setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0f);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return createBossAttributes(280.0, 8.0, 0.3, 10.0);
	}

	@Override
	protected Text getBossName() {
		return Text.translatable("entity.arcanebestiary.infernal_warden");
	}

	@Override
	protected void tickAbilities(LivingEntity target) {
		ServerWorld world = (ServerWorld) getWorld();
		double distSq = squaredDistanceTo(target);

		if (novaWindup > 0) {
			novaWindup--;
			getNavigation().stop();
			world.spawnParticles(ParticleTypes.FLAME, getX(), getY() + 0.2, getZ(), 25, NOVA_RADIUS / 3, 0.1, NOVA_RADIUS / 3, 0.04);
			if (novaWindup == 0) {
				infernoNova(world);
			}
			return;
		}

		if (--fireballCooldown <= 0 && distSq > 16.0 && distSq < 40.0 * 40.0 && canSee(target)) {
			int count = isEnraged() ? 5 : 3;
			Vec3d base = directionTo(target);
			for (int i = 0; i < count; i++) {
				double angle = (i - (count - 1) / 2.0) * 0.14;
				Vec3d dir = base.rotateY((float) angle);
				SmallFireballEntity fireball = new SmallFireballEntity(world, this, dir.multiply(1.3));
				fireball.setPosition(getX() + dir.x * 1.8, getBodyY(0.75), getZ() + dir.z * 1.8);
				world.spawnEntity(fireball);
			}
			playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 2.0f, 0.6f);
			fireballCooldown = isEnraged() ? 45 : 70;
		}

		if (--novaCooldown <= 0 && distSq < NOVA_RADIUS * NOVA_RADIUS) {
			novaWindup = 30;
			novaCooldown = isEnraged() ? 120 : 200;
			playSound(SoundEvents.ENTITY_BLAZE_AMBIENT, 3.0f, 0.5f);
		}

		if (isEnraged() && --summonCooldown <= 0) {
			if (countNearby(ModEntities.EMBER_IMP, 32.0) < 6) {
				summonMinions(ModEntities.EMBER_IMP, 2, target);
				playSound(SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, 2.0f, 0.8f);
			}
			summonCooldown = 300;
		}
	}

	private void infernoNova(ServerWorld world) {
		for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, getBoundingBox().expand(NOVA_RADIUS),
			e -> e != this && e.isAlive() && !(e instanceof BossEntity) && !(e instanceof EmberImpEntity)
				&& e.squaredDistanceTo(this) <= NOVA_RADIUS * NOVA_RADIUS)) {
			entity.damage(getDamageSources().mobAttack(this), isEnraged() ? 16.0f : 12.0f);
			entity.setOnFireFor(6);
			knockAway(entity, 1.4, 0.5);
		}
		for (int ring = 1; ring <= 4; ring++) {
			double radius = ring * NOVA_RADIUS / 4.0;
			int points = 12 + ring * 8;
			for (int i = 0; i < points; i++) {
				double angle = i * Math.PI * 2 / points;
				world.spawnParticles(ParticleTypes.FLAME, getX() + Math.cos(angle) * radius, getY() + 0.3, getZ() + Math.sin(angle) * radius, 1, 0, 0.1, 0, 0.02);
			}
		}
		world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 0.5, getZ(), 1, 0, 0, 0, 0);
		playSound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 3.0f, 0.7f);
	}

	@Override
	protected void spawnIntroParticles(ServerWorld world) {
		world.spawnParticles(ParticleTypes.FLAME, getX(), getY() + 1.8, getZ(), 12, 0.7, 1.6, 0.7, 0.03);
		world.spawnParticles(ParticleTypes.LAVA, getX(), getY() + 1.8, getZ(), 2, 0.7, 1.6, 0.7, 0.0);
	}

	@Override
	public void tickMovement() {
		if (getWorld().isClient) {
			getWorld().addParticle(isEnraged() ? ParticleTypes.LARGE_SMOKE : ParticleTypes.FLAME,
				getParticleX(0.6), getRandomBodyY(), getParticleZ(0.6), 0, 0.02, 0);
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
			target.setOnFireFor(5);
		}
		return hit;
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (source.getSource() instanceof SmallFireballEntity) {
			return false;
		}
		return super.damage(source, amount);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_BLAZE_AMBIENT;
	}
}
