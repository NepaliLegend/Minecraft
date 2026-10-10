package com.arcanebestiary.registry;

import com.arcanebestiary.ArcaneBestiary;
import com.arcanebestiary.entity.CrystalBehemothEntity;
import com.arcanebestiary.entity.CrystalCrawlerEntity;
import com.arcanebestiary.entity.EmberImpEntity;
import com.arcanebestiary.entity.FrostBoltEntity;
import com.arcanebestiary.entity.FrostWraithEntity;
import com.arcanebestiary.entity.GlacialSovereignEntity;
import com.arcanebestiary.entity.InfernalWardenEntity;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.world.Heightmap;

public final class ModEntities {
	public static final EntityType<FrostWraithEntity> FROST_WRAITH = register("frost_wraith",
		EntityType.Builder.create(FrostWraithEntity::new, SpawnGroup.MONSTER).dimensions(0.6f, 1.95f).maxTrackingRange(8));

	public static final EntityType<EmberImpEntity> EMBER_IMP = register("ember_imp",
		EntityType.Builder.create(EmberImpEntity::new, SpawnGroup.MONSTER).dimensions(0.5f, 1.2f).maxTrackingRange(8).makeFireImmune());

	public static final EntityType<CrystalCrawlerEntity> CRYSTAL_CRAWLER = register("crystal_crawler",
		EntityType.Builder.create(CrystalCrawlerEntity::new, SpawnGroup.MONSTER).dimensions(1.1f, 0.8f).maxTrackingRange(8));

	public static final EntityType<InfernalWardenEntity> INFERNAL_WARDEN = register("infernal_warden",
		EntityType.Builder.create(InfernalWardenEntity::new, SpawnGroup.MONSTER).dimensions(1.2f, 3.8f).maxTrackingRange(10).makeFireImmune());

	public static final EntityType<GlacialSovereignEntity> GLACIAL_SOVEREIGN = register("glacial_sovereign",
		EntityType.Builder.create(GlacialSovereignEntity::new, SpawnGroup.MONSTER).dimensions(1.0f, 3.3f).maxTrackingRange(10));

	public static final EntityType<CrystalBehemothEntity> CRYSTAL_BEHEMOTH = register("crystal_behemoth",
		EntityType.Builder.create(CrystalBehemothEntity::new, SpawnGroup.MONSTER).dimensions(4.2f, 2.6f).maxTrackingRange(10));

	public static final EntityType<FrostBoltEntity> FROST_BOLT = register("frost_bolt",
		EntityType.Builder.<FrostBoltEntity>create(FrostBoltEntity::new, SpawnGroup.MISC).dimensions(0.3f, 0.3f).maxTrackingRange(4).trackingTickInterval(10));

	private static <T extends net.minecraft.entity.Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		return Registry.register(Registries.ENTITY_TYPE, ArcaneBestiary.id(name), builder.build(name));
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(FROST_WRAITH, FrostWraithEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(EMBER_IMP, EmberImpEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(CRYSTAL_CRAWLER, CrystalCrawlerEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(INFERNAL_WARDEN, InfernalWardenEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(GLACIAL_SOVEREIGN, GlacialSovereignEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(CRYSTAL_BEHEMOTH, CrystalBehemothEntity.createAttributes());

		SpawnRestriction.register(FROST_WRAITH, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);
		SpawnRestriction.register(EMBER_IMP, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnIgnoreLightLevel);
		SpawnRestriction.register(CRYSTAL_CRAWLER, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);

		BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.SPAWNS_SNOW_FOXES), SpawnGroup.MONSTER, FROST_WRAITH, 30, 1, 2);
		BiomeModifications.addSpawn(BiomeSelectors.foundInTheNether(), SpawnGroup.MONSTER, EMBER_IMP, 20, 1, 3);
		BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, CRYSTAL_CRAWLER, 12, 1, 2);
	}

	private ModEntities() {
	}
}
