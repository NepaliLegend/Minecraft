package com.arcanebestiary.item;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/** Forged from the Infernal Warden's heart. Ignites what it hits and hurls fireballs on use. */
public class EmberbrandItem extends SwordItem {
	private static final int COOLDOWN_TICKS = 30;

	public EmberbrandItem(ToolMaterial material, Settings settings) {
		super(material, settings);
	}

	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		target.setOnFireFor(6);
		return super.postHit(stack, target, attacker);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (user.getItemCooldownManager().isCoolingDown(this)) {
			return TypedActionResult.pass(stack);
		}
		if (!world.isClient) {
			Vec3d look = user.getRotationVec(1.0f);
			SmallFireballEntity fireball = new SmallFireballEntity(world, user, look.multiply(1.4));
			fireball.setPosition(user.getX() + look.x, user.getEyeY() - 0.1 + look.y, user.getZ() + look.z);
			world.spawnEntity(fireball);
			world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);
			stack.damage(2, user, LivingEntity.getSlotForHand(hand));
		}
		user.getItemCooldownManager().set(this, COOLDOWN_TICKS);
		user.swingHand(hand, true);
		return TypedActionResult.success(stack, world.isClient);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.arcanebestiary.emberbrand.tooltip.hit").formatted(Formatting.GOLD));
		tooltip.add(Text.translatable("item.arcanebestiary.emberbrand.tooltip.use").formatted(Formatting.GRAY));
	}
}
