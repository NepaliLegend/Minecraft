package com.arcanebestiary.registry;

import com.arcanebestiary.ArcaneBestiary;
import com.arcanebestiary.block.SolarPanelBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModBlockEntities {
	public static final BlockEntityType<SolarPanelBlockEntity> SOLAR_PANEL = Registry.register(Registries.BLOCK_ENTITY_TYPE,
		ArcaneBestiary.id("solar_panel"),
		FabricBlockEntityTypeBuilder.create(SolarPanelBlockEntity::new, ModBlocks.SOLAR_PANEL).build());

	public static void init() {
	}

	private ModBlockEntities() {
	}
}
