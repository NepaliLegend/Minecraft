package com.arcanebestiary.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/** A slow, brutal hammer. Every hit sends a shockwave through nearby enemies. */
public class BehemothMaulItem extends SwordItem {
	private static final double SLAM_RADIUS = 3.5;

	public BehemothMaulItem(ToolMaterial material, Settings settings) {
		super(material, settings);
	}

	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (attacker.getWorld() instanceof ServerWorld world) {
			DamageSource source = attacker.getDamageSources().mobAttack(attacker);
			if (attacker instanceof net.minecraft.entity.player.PlayerEntity player) {
				source = attacker.getDamageSources().playerAttack(player);
			}
			for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, target.getBoundingBox().expand(SLAM_RADIUS),
				e -> e != attacker && e != target && e.isAlive() && !attacker.isTeammate(e) && e.squaredDistanceTo(target) <= SLAM_RADIUS * SLAM_RADIUS)) {
				entity.damage(source, 7.0f);
				double dx = entity.getX() - target.getX();
				double dz = entity.getZ() - target.getZ();
				double len = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
				entity.addVelocity(dx / len * 0.8, 0.45, dz / len * 0.8);
				entity.velocityModified = true;
			}
			world.spawnParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 0.2, target.getZ(), 3, 0.8, 0.1, 0.8, 0.0);
			world.spawnParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 0.5, target.getZ(), 30, SLAM_RADIUS / 2, 0.3, SLAM_RADIUS / 2, 0.3);
			world.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ITEM_MACE_SMASH_GROUND, SoundCategory.PLAYERS, 1.0f, 0.8f);
		}
		return super.postHit(stack, target, attacker);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.arcanebestiary.behemoth_maul.tooltip.hit").formatted(Formatting.LIGHT_PURPLE));
	}
}
