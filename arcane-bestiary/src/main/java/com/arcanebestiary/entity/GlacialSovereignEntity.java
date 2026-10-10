package com.arcanebestiary.entity;

import com.arcanebestiary.registry.ModEntities;
import com.arcanebestiary.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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
 * Frozen lich-king. Barrages of ice bolts, a blizzard that chills everyone nearby, short-range teleports
 * and, once enraged, a retinue of Frost Wraiths.
 */
public class GlacialSovereignEntity extends BossEntity {
	private static final double BLIZZARD_RADIUS = 14.0;

	private int barrageCooldown = 40;
	private int blizzardCooldown = 160;
	private int blizzardWindup = 0;
	private int teleportCooldown = 120;
	private int summonCooldown = 120;

	public GlacialSovereignEntity(EntityType<? extends GlacialSovereignEntity> type, World world) {
		super(type, world, BossBar.Color.BLUE);
		equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.FROSTFANG));
		setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0f);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return createBossAttributes(240.0, 6.0, 0.28, 8.0);
	}

	@Override
	protected Text getBossName() {
		return Text.translatable("entity.arcanebestiary.glacial_sovereign");
	}

	@Override
	protected void tickAbilities(LivingEntity target) {
		ServerWorld world = (ServerWorld) getWorld();
		double distSq = squaredDistanceTo(target);

		if (blizzardWindup > 0) {
			blizzardWindup--;
			getNavigation().stop();
			world.spawnParticles(ParticleTypes.SNOWFLAKE, getX(), getY() + 1.5, getZ(), 40, BLIZZARD_RADIUS / 3, 1.5, BLIZZARD_RADIUS / 3, 0.1);
			if (blizzardWindup == 0) {
				blizzard(world);
			}
			return;
		}

		if (--barrageCooldown <= 0 && distSq > 9.0 && distSq < 32.0 * 32.0 && canSee(target)) {
			int count = isEnraged() ? 5 : 3;
			Vec3d base = directionTo(target);
			for (int i = 0; i < count; i++) {
				double angle = (i - (count - 1) / 2.0) * 0.16;
				Vec3d dir = base.rotateY((float) angle);
				FrostBoltEntity bolt = new FrostBoltEntity(world, this, 7.0f);
				bolt.setPosition(getX() + dir.x, getEyeY() - 0.3, getZ() + dir.z);
				bolt.setVelocity(dir.x, dir.y, dir.z, 1.5f, 0.0f);
				world.spawnEntity(bolt);
			}
			playSound(SoundEvents.ENTITY_SNOW_GOLEM_SHOOT, 2.0f, 0.5f);
			barrageCooldown = isEnraged() ? 35 : 55;
		}

		if (--blizzardCooldown <= 0) {
			blizzardWindup = 30;
			blizzardCooldown = isEnraged() ? 160 : 240;
			playSound(SoundEvents.ENTITY_PLAYER_HURT_FREEZE, 3.0f, 0.5f);
		}

		if (--teleportCooldown <= 0) {
			teleportCooldown = 140;
			if (distSq > 100.0 || random.nextInt(3) == 0) {
				teleportNear(world, target);
			}
		}

		if (isEnraged() && --summonCooldown <= 0) {
			if (countNearby(ModEntities.FROST_WRAITH, 32.0) < 4) {
				summonMinions(ModEntities.FROST_WRAITH, 2, target);
				playSound(SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, 2.0f, 1.2f);
			}
			summonCooldown = 320;
		}
	}

	private void blizzard(ServerWorld world) {
		for (PlayerEntity player : world.getEntitiesByClass(PlayerEntity.class, getBoundingBox().expand(BLIZZARD_RADIUS),
			p -> p.isAlive() && !p.isSpectator() && p.squaredDistanceTo(this) <= BLIZZARD_RADIUS * BLIZZARD_RADIUS)) {
			player.damage(getDamageSources().freeze(), isEnraged() ? 8.0f : 5.0f);
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 120, 2), this);
			player.setFrozenTicks(player.getMinFreezeDamageTicks() + 140);
		}
		for (int ring = 1; ring <= 5; ring++) {
			double radius = ring * BLIZZARD_RADIUS / 5.0;
			int points = 14 + ring * 8;
			for (int i = 0; i < points; i++) {
				double angle = i * Math.PI * 2 / points;
				world.spawnParticles(ParticleTypes.SNOWFLAKE, getX() + Math.cos(angle) * radius, getY() + 0.5, getZ() + Math.sin(angle) * radius, 2, 0.1, 0.6, 0.1, 0.02);
			}
		}
		playSound(SoundEvents.BLOCK_GLASS_BREAK, 3.0f, 0.5f);
	}

	private void teleportNear(ServerWorld world, LivingEntity target) {
		for (int attempt = 0; attempt < 12; attempt++) {
			double angle = random.nextDouble() * Math.PI * 2;
			double dist = 5.0 + random.nextDouble() * 3.0;
			double x = target.getX() + Math.cos(angle) * dist;
			double z = target.getZ() + Math.sin(angle) * dist;
			for (int dy = 3; dy >= -4; dy--) {
				double y = target.getY() + dy;
				BlockPos below = BlockPos.ofFloored(x, y - 1, z);
				if (world.getBlockState(below).isSolidBlock(world, below)
					&& world.isSpaceEmpty(this, getBoundingBox().offset(x - getX(), y - getY(), z - getZ()))) {
					world.spawnParticles(ParticleTypes.SNOWFLAKE, getX(), getY() + 2, getZ(), 40, 0.5, 1.5, 0.5, 0.1);
					requestTeleport(x, y, z);
					world.spawnParticles(ParticleTypes.SNOWFLAKE, x, y + 2, z, 40, 0.5, 1.5, 0.5, 0.1);
					playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 2.0f, 0.6f);
					return;
				}
			}
		}
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (source.isIn(DamageTypeTags.IS_FREEZING)) {
			return false;
		}
		if (source.isIn(DamageTypeTags.IS_FIRE)) {
			amount *= 1.5f;
		}
		boolean result = super.damage(source, amount);
		if (result && source.isIn(DamageTypeTags.IS_PROJECTILE) && getTarget() != null && random.nextInt(4) == 0
			&& getWorld() instanceof ServerWorld world) {
			teleportNear(world, getTarget());
		}
		return result;
	}

	@Override
	protected void spawnIntroParticles(ServerWorld world) {
		world.spawnParticles(ParticleTypes.SNOWFLAKE, getX(), getY() + 1.5, getZ(), 20, 0.8, 1.5, 0.8, 0.05);
		world.spawnParticles(ParticleTypes.CLOUD, getX(), getY() + 0.3, getZ(), 3, 0.8, 0.1, 0.8, 0.02);
	}

	@Override
	public void tickMovement() {
		if (getWorld().isClient) {
			getWorld().addParticle(ParticleTypes.SNOWFLAKE, getParticleX(0.7), getRandomBodyY(), getParticleZ(0.7), 0, -0.03, 0);
		}
		super.tickMovement();
	}

	@Override
	public boolean canFreeze() {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_STRAY_AMBIENT;
	}
}
