package com.arcanebestiary.block;

import com.arcanebestiary.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SolarPanelBlockEntity extends BlockEntity {
	private static final int TICK_INTERVAL = 20;

	private ItemStack stored = ItemStack.EMPTY;

	public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.SOLAR_PANEL, pos, state);
	}

	public ItemStack getStoredStack() {
		return stored;
	}

	public void setStoredStack(ItemStack stack) {
		this.stored = stack;
		markDirty();
	}

	public ItemStack takeStoredStack() {
		ItemStack result = stored;
		this.stored = ItemStack.EMPTY;
		markDirty();
		return result;
	}

	public static void serverTick(World world, BlockPos pos, BlockState state, SolarPanelBlockEntity panel) {
		if ((world.getTime() + pos.asLong()) % TICK_INTERVAL != 0) {
			return;
		}
		int sun = SolarPanelBlock.getSunlight(world, pos);
		if (state.get(SolarPanelBlock.POWER) != sun) {
			world.setBlockState(pos, state.with(SolarPanelBlock.POWER, sun), 3);
		}
		if (sun > 0 && !panel.stored.isEmpty() && panel.stored.isDamaged()) {
			int repair = Math.max(1, sun / 5);
			panel.stored.setDamage(Math.max(0, panel.stored.getDamage() - repair));
			panel.markDirty();
			if (world instanceof ServerWorld serverWorld) {
				serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 3, 0.25, 0.05, 0.25, 0.02);
			}
		}
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.writeNbt(nbt, registries);
		if (!stored.isEmpty()) {
			nbt.put("Stored", stored.encode(registries));
		}
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.readNbt(nbt, registries);
		stored = nbt.contains("Stored") ? ItemStack.fromNbt(registries, nbt.get("Stored")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
	}
}
