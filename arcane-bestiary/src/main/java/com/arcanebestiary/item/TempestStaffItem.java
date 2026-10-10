package com.arcanebestiary.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;

/** Calls lightning down on whatever the wielder is looking at. */
public class TempestStaffItem extends Item {
	private static final double RANGE = 40.0;
	private static final int COOLDOWN_TICKS = 50;

	public TempestStaffItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (user.getItemCooldownManager().isCoolingDown(this)) {
			return TypedActionResult.pass(stack);
		}
		if (world instanceof ServerWorld serverWorld) {
			Vec3d start = user.getEyePos();
			Vec3d end = start.add(user.getRotationVec(1.0f).multiply(RANGE));
			Vec3d strikePos = findStrikePos(world, user, start, end);

			int strikes = user.isSneaking() ? 3 : 1;
			for (int i = 0; i < strikes; i++) {
				LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
				if (bolt == null) {
					continue;
				}
				double ox = i == 0 ? 0 : (world.random.nextDouble() - 0.5) * 6;
				double oz = i == 0 ? 0 : (world.random.nextDouble() - 0.5) * 6;
				bolt.refreshPositionAfterTeleport(strikePos.x + ox, strikePos.y, strikePos.z + oz);
				if (user instanceof ServerPlayerEntity serverPlayer) {
					bolt.setChanneler(serverPlayer);
				}
				serverWorld.spawnEntity(bolt);
			}
			stack.damage(strikes * 2, user, LivingEntity.getSlotForHand(hand));
		}
		user.getItemCooldownManager().set(this, user.isSneaking() ? COOLDOWN_TICKS * 2 : COOLDOWN_TICKS);
		user.swingHand(hand, true);
		return TypedActionResult.success(stack, world.isClient);
	}

	private static Vec3d findStrikePos(World world, PlayerEntity user, Vec3d start, Vec3d end) {
		BlockHitResult blockHit = world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));
		Vec3d limit = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getPos();
		Box box = user.getBoundingBox().stretch(limit.subtract(start)).expand(1.0);
		EntityHitResult entityHit = ProjectileUtil.raycast(user, start, limit, box, e -> !e.isSpectator() && e.canHit(), start.squaredDistanceTo(limit));
		if (entityHit != null) {
			Entity target = entityHit.getEntity();
			return target.getPos();
		}
		if (blockHit.getType() == HitResult.Type.MISS) {
			BlockHitResult ground = world.raycast(new RaycastContext(end, end.add(0, -64, 0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));
			return ground.getType() == HitResult.Type.MISS ? end : ground.getPos();
		}
		return blockHit.getPos();
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.arcanebestiary.tempest_staff.tooltip.use").formatted(Formatting.YELLOW));
		tooltip.add(Text.translatable("item.arcanebestiary.tempest_staff.tooltip.sneak").formatted(Formatting.GRAY));
	}
}
