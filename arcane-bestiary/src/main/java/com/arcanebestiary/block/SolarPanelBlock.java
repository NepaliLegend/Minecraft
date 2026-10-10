package com.arcanebestiary.block;

import com.arcanebestiary.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Turns sunlight into two things: a redstone signal (0-15) and slow repairs for a damaged item stored inside.
 * Right-click with a damaged tool, weapon or armor piece to insert it, right-click with an empty hand to take it back.
 */
public class SolarPanelBlock extends BlockWithEntity {
	public static final MapCodec<SolarPanelBlock> CODEC = createCodec(SolarPanelBlock::new);
	public static final IntProperty POWER = IntProperty.of("power", 0, 15);
	private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 4, 16);

	public SolarPanelBlock(Settings settings) {
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(POWER, 0));
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(POWER);
	}

	/** Sunlight reaching the panel, 0 (night, rain, covered or no sky) to 15 (clear noon). */
	public static int getSunlight(World world, BlockPos pos) {
		if (!world.getDimension().hasSkyLight()) {
			return 0;
		}
		int sun = world.getLightLevel(LightType.SKY, pos.up()) - world.getAmbientDarkness();
		return Math.max(0, Math.min(15, sun));
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	protected boolean emitsRedstonePower(BlockState state) {
		return true;
	}

	@Override
	protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
		return state.get(POWER);
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new SolarPanelBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : validateTicker(type, ModBlockEntities.SOLAR_PANEL, SolarPanelBlockEntity::serverTick);
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!stack.isDamageable() || !(world.getBlockEntity(pos) instanceof SolarPanelBlockEntity panel) || !panel.getStoredStack().isEmpty()) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (!world.isClient) {
			panel.setStoredStack(stack.split(1));
		}
		return ItemActionResult.success(world.isClient);
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof SolarPanelBlockEntity panel) || panel.getStoredStack().isEmpty()) {
			return ActionResult.PASS;
		}
		if (!world.isClient) {
			ItemStack stored = panel.takeStoredStack();
			if (!player.getInventory().insertStack(stored)) {
				player.dropItem(stored, false);
			}
		}
		return ActionResult.success(world.isClient);
	}

	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof SolarPanelBlockEntity panel) {
			ItemScatterer.spawn(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, panel.takeStoredStack());
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}

	@Override
	public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType options) {
		tooltip.add(Text.translatable("block.arcanebestiary.solar_panel.tooltip.repair").formatted(Formatting.YELLOW));
		tooltip.add(Text.translatable("block.arcanebestiary.solar_panel.tooltip.redstone").formatted(Formatting.GRAY));
	}
}
