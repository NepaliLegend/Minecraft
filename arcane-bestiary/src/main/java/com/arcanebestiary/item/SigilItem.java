package com.arcanebestiary.item;

import com.arcanebestiary.entity.BossEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;

import java.util.function.Supplier;

/** Right-click the ground with a sigil to summon the boss it is bound to. */
public class SigilItem extends Item {
	private final Supplier<? extends EntityType<? extends BossEntity>> bossType;
	private final Formatting tooltipColor;

	public SigilItem(Supplier<? extends EntityType<? extends BossEntity>> bossType, Formatting tooltipColor, Settings settings) {
		super(settings);
		this.bossType = bossType;
		this.tooltipColor = tooltipColor;
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		if (!(context.getWorld() instanceof ServerWorld world)) {
			return ActionResult.SUCCESS;
		}
		PlayerEntity player = context.getPlayer();
		if (world.getDifficulty() == Difficulty.PEACEFUL) {
			if (player != null) {
				player.sendMessage(Text.translatable("message.arcanebestiary.sigil.peaceful").formatted(Formatting.RED), true);
			}
			return ActionResult.FAIL;
		}
		BlockPos pos = context.getBlockPos().offset(context.getSide());
		BossEntity boss = bossType.get().spawn(world, pos, SpawnReason.MOB_SUMMONED);
		if (boss == null) {
			return ActionResult.FAIL;
		}
		boss.setYaw(player != null ? player.getYaw() + 180.0f : 0.0f);
		LightningEntity effect = net.minecraft.entity.EntityType.LIGHTNING_BOLT.create(world);
		if (effect != null) {
			effect.refreshPositionAfterTeleport(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			effect.setCosmetic(true);
			world.spawnEntity(effect);
		}
		if (player != null) {
			context.getStack().decrementUnlessCreative(1, player);
		}
		return ActionResult.CONSUME;
	}

	@Override
	public void appendTooltip(net.minecraft.item.ItemStack stack, TooltipContext context, java.util.List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
		tooltip.add(Text.translatable(getTranslationKey() + ".tooltip").formatted(tooltipColor));
	}
}
