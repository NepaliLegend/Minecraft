package com.arcanebestiary.entity;

import com.arcanebestiary.registry.ModBlocks;
import com.arcanebestiary.registry.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.EvokerFangsEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * A mountain-sized crystal spider. Erupts lines of crystal spikes, slams the ground with a telegraphed
 * shockwave and, when enraged, hatches Crystal Crawlers.
 */
public class CrystalBehemothEntity extends BossEntity {
	private static final double SLAM_RADIUS = 9.0;

	private int spikeCooldown = 60;
	private int slamCooldown = 120;
	private int slamWindup = 0;
	private int summonCooldown = 100;

	public CrystalBehemothEntity(EntityType<? extends CrystalBehemothEntity> type, World world) {
		super(type, world, BossBar.Color.PURPLE);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return createBossAttributes(320.0, 16.0, 0.27, 14.0);
	}

	@Override
	protected Text getBossName() {
		return Text.translatable("entity.arcanebestiary.crystal_behemoth");
	}

	@Override
	protected void tickAbilities(LivingEntity target) {
		ServerWorld world = (ServerWorld) getWorld();
		double distSq = squaredDistanceTo(target);

		if (slamWindup > 0) {
			slamWindup--;
			getNavigation().stop();
			world.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, ModBlocks.CRYSTALITE_BLOCK.getDefaultState()),
				getX(), getY() + 0.3, getZ(), 30, SLAM_RADIUS / 3, 0.1, SLAM_RADIUS / 3, 0.1);
			if (slamWindup == 0) {
				slam(world);
			}
			return;
		}

		if (--spikeCooldown <= 0 && distSq > 16.0 && distSq < 28.0 * 28.0) {
			spikeLine(world, directionTo(target), 10);
			if (isEnraged()) {
				spikeLine(world, directionTo(target).rotateY(0.35f), 8);
				spikeLine(world, directionTo(target).rotateY(-0.35f), 8);
			}
			playSound(SoundEvents.ENTITY_EVOKER_CAST_SPELL, 2.0f, 0.6f);
			spikeCooldown = isEnraged() ? 70 : 100;
		}

		if (--slamCooldown <= 0 && distSq < SLAM_RADIUS * SLAM_RADIUS) {
			slamWindup = 25;
			slamCooldown = isEnraged() ? 110 : 170;
			playSound(SoundEvents.ENTITY_RAVAGER_ROAR, 3.0f, 0.6f);
		}

		if (isEnraged() && --summonCooldown <= 0) {
			if (countNearby(ModEntities.CRYSTAL_CRAWLER, 32.0) < 6) {
				summonMinions(ModEntities.CRYSTAL_CRAWLER, 3, target);
				playSound(SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, 2.0f, 0.6f);
			}
			summonCooldown = 280;
		}
	}

	private void spikeLine(ServerWorld world, Vec3d direction, int length) {
		Vec3d flat = new Vec3d(direction.x, 0, direction.z).normalize();
		float yaw = (float) (Math.atan2(flat.z, flat.x) * 180.0 / Math.PI) - 90.0f;
		for (int i = 0; i < length; i++) {
			double dist = 2.5 + i * 1.5;
			double x = getX() + flat.x * dist;
			double z = getZ() + flat.z * dist;
			Double y = findGroundY(world, x, z);
			if (y != null) {
				world.spawnEntity(new EvokerFangsEntity(world, x, y, z, yaw, i * 2, this));
			}
		}
	}

	private Double findGroundY(ServerWorld world, double x, double z) {
		for (int dy = 3; dy >= -4; dy--) {
			BlockPos below = BlockPos.ofFloored(x, getY() + dy - 1, z);
			BlockPos at = below.up();
			if (world.getBlockState(below).isSolidBlock(world, below) && world.getBlockState(at).getCollisionShape(world, at).isEmpty()) {
				return (double) at.getY();
			}
		}
		return null;
	}

	private void slam(ServerWorld world) {
		for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, getBoundingBox().expand(SLAM_RADIUS),
			e -> e != this && e.isAlive() && !(e instanceof BossEntity) && !(e instanceof CrystalCrawlerEntity)
				&& e.squaredDistanceTo(this) <= SLAM_RADIUS * SLAM_RADIUS)) {
			entity.damage(getDamageSources().mobAttack(this), isEnraged() ? 20.0f : 14.0f);
			knockAway(entity, 1.6, 0.8);
		}
		for (int ring = 1; ring <= 5; ring++) {
			double radius = ring * SLAM_RADIUS / 5.0;
			int points = 12 + ring * 8;
			for (int i = 0; i < points; i++) {
				double angle = i * Math.PI * 2 / points;
				world.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, ModBlocks.CRYSTALITE_BLOCK.getDefaultState()),
					getX() + Math.cos(angle) * radius, getY() + 0.2, getZ() + Math.sin(angle) * radius, 2, 0.1, 0.3, 0.1, 0.1);
			}
		}
		world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 0.5, getZ(), 1, 0, 0, 0, 0);
		playSound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 3.0f, 0.5f);
		if (isEnraged()) {
			for (int i = 0; i < 8; i++) {
				spikeLine(world, new Vec3d(Math.cos(i * Math.PI / 4), 0, Math.sin(i * Math.PI / 4)), 5);
			}
		}
	}

	@Override
	protected void spawnIntroParticles(ServerWorld world) {
		world.spawnParticles(ParticleTypes.END_ROD, getX(), getY() + 1.2, getZ(), 10, 1.5, 1.0, 1.5, 0.03);
		world.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, ModBlocks.CRYSTALITE_BLOCK.getDefaultState()),
			getX(), getY() + 0.1, getZ(), 12, 1.5, 0.1, 1.5, 0.1);
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		boolean result = super.damage(source, amount);
		if (result && source.getAttacker() instanceof LivingEntity attacker
			&& source.getSource() == attacker && !source.isIn(DamageTypeTags.IS_PROJECTILE)) {
			attacker.damage(getDamageSources().thorns(this), 3.0f);
		}
		return result;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_SPIDER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK;
	}
}
