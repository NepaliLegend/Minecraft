package com.arcanebestiary.registry;

import com.arcanebestiary.ArcaneBestiary;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public final class ModItemGroups {
	public static final ItemGroup ARCANE_BESTIARY = Registry.register(Registries.ITEM_GROUP, ArcaneBestiary.id("main"),
		FabricItemGroup.builder()
			.icon(() -> new ItemStack(ModItems.EMBERBRAND))
			.displayName(Text.translatable("itemGroup.arcanebestiary.main"))
			.entries((context, entries) -> {
				ModItems.ALL.forEach(entries::add);
				entries.add(ModBlocks.CRYSTALITE_ORE);
				entries.add(ModBlocks.CRYSTALITE_BLOCK);
				entries.add(ModBlocks.SOLAR_PANEL);
			})
			.build());

	public static void init() {
	}

	private ModItemGroups() {
	}
}
