package com.arcanebestiary;

import com.arcanebestiary.registry.ModArmorMaterials;
import com.arcanebestiary.registry.ModBlockEntities;
import com.arcanebestiary.registry.ModBlocks;
import com.arcanebestiary.registry.ModEntities;
import com.arcanebestiary.registry.ModItemGroups;
import com.arcanebestiary.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArcaneBestiary implements ModInitializer {
	public static final String MOD_ID = "arcanebestiary";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModEntities.init();
		ModBlocks.init();
		ModBlockEntities.init();
		ModArmorMaterials.init();
		ModItems.init();
		ModItemGroups.init();

		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES,
			RegistryKey.of(RegistryKeys.PLACED_FEATURE, id("crystalite_ore")));
		LOGGER.info("Arcane Bestiary loaded: three bosses, three mobs and a lot of things to hit them with.");
	}
}
