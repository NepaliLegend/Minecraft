package com.arcanebestiary.registry;

import com.arcanebestiary.ArcaneBestiary;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public final class ModBlocks {
	public static final Block CRYSTALITE_ORE = register("crystalite_ore",
		new ExperienceDroppingBlock(UniformIntProvider.create(2, 5), AbstractBlock.Settings.create()
			.mapColor(MapColor.PURPLE)
			.requiresTool()
			.strength(3.5f, 3.5f)
			.sounds(BlockSoundGroup.AMETHYST_BLOCK)
			.luminance(state -> 3)));

	public static final Block CRYSTALITE_BLOCK = register("crystalite_block",
		new Block(AbstractBlock.Settings.create()
			.mapColor(MapColor.PURPLE)
			.requiresTool()
			.strength(5.0f, 6.0f)
			.sounds(BlockSoundGroup.AMETHYST_BLOCK)
			.luminance(state -> 7)));

	private static Block register(String name, Block block) {
		Identifier id = ArcaneBestiary.id(name);
		Registry.register(Registries.BLOCK, id, block);
		Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
		return block;
	}

	public static void init() {
	}

	private ModBlocks() {
	}
}
