package com.arcanebestiary.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/** Forged from the Glacial Sovereign's core. Freezes foes and releases a frost nova on use. */
public class FrostfangItem extends SwordItem {
	private static final int COOLDOWN_TICKS = 100;
	private static final double NOVA_RADIUS = 5.0;

	public FrostfangItem(ToolMaterial material, Settings settings) {
		super(material, settings);
	}

	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 2), attacker);
		target.setFrozenTicks(target.getMinFreezeDamageTicks() + 60);
		return super.postHit(stack, target, attacker);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (user.getItemCooldownManager().isCoolingDown(this)) {
			return TypedActionResult.pass(stack);
		}
		if (world instanceof ServerWorld serverWorld) {
			DamageSource source = user.getDamageSources().playerAttack(user);
			for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, user.getBoundingBox().expand(NOVA_RADIUS),
				e -> e != user && e.isAlive() && !user.isTeammate(e) && e.squaredDistanceTo(user) <= NOVA_RADIUS * NOVA_RADIUS)) {
				entity.damage(source, 6.0f);
				entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 140, 3), user);
				entity.setFrozenTicks(entity.getMinFreezeDamageTicks() + 100);
			}
			serverWorld.spawnParticles(ParticleTypes.SNOWFLAKE, user.getX(), user.getY() + 1.0, user.getZ(), 120, NOVA_RADIUS / 2, 0.8, NOVA_RADIUS / 2, 0.05);
			serverWorld.spawnParticles(ParticleTypes.CLOUD, user.getX(), user.getY() + 0.3, user.getZ(), 40, NOVA_RADIUS / 2, 0.2, NOVA_RADIUS / 2, 0.02);
			world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1.2f, 0.6f);
			world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_PLAYER_HURT_FREEZE, SoundCategory.PLAYERS, 1.0f, 0.8f);
			stack.damage(3, user, LivingEntity.getSlotForHand(hand));
		}
		user.getItemCooldownManager().set(this, COOLDOWN_TICKS);
		user.swingHand(hand, true);
		return TypedActionResult.success(stack, world.isClient);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.arcanebestiary.frostfang.tooltip.hit").formatted(Formatting.AQUA));
		tooltip.add(Text.translatable("item.arcanebestiary.frostfang.tooltip.use").formatted(Formatting.GRAY));
	}
}
