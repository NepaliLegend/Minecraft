package com.arcanebestiary.registry;

import com.arcanebestiary.ArcaneBestiary;
import com.arcanebestiary.item.BehemothMaulItem;
import com.arcanebestiary.item.EmberbrandItem;
import com.arcanebestiary.item.FrostfangItem;
import com.arcanebestiary.item.SigilItem;
import com.arcanebestiary.item.TempestStaffItem;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.Rarity;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public final class ModItems {
	public static final List<Item> ALL = new ArrayList<>();

	public static final Item CRYSTALITE_SHARD = register("crystalite_shard", new Item(new Item.Settings()));
	public static final Item FROST_ESSENCE = register("frost_essence", new Item(new Item.Settings()));
	public static final Item EMBER_ESSENCE = register("ember_essence", new Item(new Item.Settings()));
	public static final Item INFERNAL_CORE = register("infernal_core", new Item(new Item.Settings().rarity(Rarity.EPIC).fireproof()));
	public static final Item GLACIAL_CORE = register("glacial_core", new Item(new Item.Settings().rarity(Rarity.EPIC)));
	public static final Item CRYSTAL_HEART = register("crystal_heart", new Item(new Item.Settings().rarity(Rarity.EPIC)));

	public static final Item CRYSTALITE_SWORD = register("crystalite_sword", new SwordItem(ModToolMaterials.CRYSTALITE,
		new Item.Settings().attributeModifiers(SwordItem.createAttributeModifiers(ModToolMaterials.CRYSTALITE, 3, -2.4f))));
	public static final Item CRYSTALITE_PICKAXE = register("crystalite_pickaxe", new PickaxeItem(ModToolMaterials.CRYSTALITE,
		new Item.Settings().attributeModifiers(PickaxeItem.createAttributeModifiers(ModToolMaterials.CRYSTALITE, 1.0f, -2.8f))));
	public static final Item CRYSTALITE_AXE = register("crystalite_axe", new AxeItem(ModToolMaterials.CRYSTALITE,
		new Item.Settings().attributeModifiers(AxeItem.createAttributeModifiers(ModToolMaterials.CRYSTALITE, 5.0f, -3.0f))));
	public static final Item CRYSTALITE_SHOVEL = register("crystalite_shovel", new ShovelItem(ModToolMaterials.CRYSTALITE,
		new Item.Settings().attributeModifiers(ShovelItem.createAttributeModifiers(ModToolMaterials.CRYSTALITE, 1.5f, -3.0f))));
	public static final Item CRYSTALITE_HOE = register("crystalite_hoe", new HoeItem(ModToolMaterials.CRYSTALITE,
		new Item.Settings().attributeModifiers(HoeItem.createAttributeModifiers(ModToolMaterials.CRYSTALITE, -3.0f, 0.0f))));

	public static final Item CRYSTALITE_HELMET = register("crystalite_helmet", new ArmorItem(ModArmorMaterials.CRYSTALITE, ArmorItem.Type.HELMET,
		new Item.Settings().maxDamage(ArmorItem.Type.HELMET.getMaxDamage(37))));
	public static final Item CRYSTALITE_CHESTPLATE = register("crystalite_chestplate", new ArmorItem(ModArmorMaterials.CRYSTALITE, ArmorItem.Type.CHESTPLATE,
		new Item.Settings().maxDamage(ArmorItem.Type.CHESTPLATE.getMaxDamage(37))));
	public static final Item CRYSTALITE_LEGGINGS = register("crystalite_leggings", new ArmorItem(ModArmorMaterials.CRYSTALITE, ArmorItem.Type.LEGGINGS,
		new Item.Settings().maxDamage(ArmorItem.Type.LEGGINGS.getMaxDamage(37))));
	public static final Item CRYSTALITE_BOOTS = register("crystalite_boots", new ArmorItem(ModArmorMaterials.CRYSTALITE, ArmorItem.Type.BOOTS,
		new Item.Settings().maxDamage(ArmorItem.Type.BOOTS.getMaxDamage(37))));

	public static final Item EMBERBRAND = register("emberbrand", new EmberbrandItem(ModToolMaterials.EMBER,
		new Item.Settings().rarity(Rarity.EPIC).fireproof()
			.attributeModifiers(SwordItem.createAttributeModifiers(ModToolMaterials.EMBER, 3, -2.4f))));
	public static final Item FROSTFANG = register("frostfang", new FrostfangItem(ModToolMaterials.FROST,
		new Item.Settings().rarity(Rarity.EPIC)
			.attributeModifiers(SwordItem.createAttributeModifiers(ModToolMaterials.FROST, 3, -2.2f))));
	public static final Item BEHEMOTH_MAUL = register("behemoth_maul", new BehemothMaulItem(ModToolMaterials.TITAN,
		new Item.Settings().rarity(Rarity.EPIC)
			.attributeModifiers(SwordItem.createAttributeModifiers(ModToolMaterials.TITAN, 5, -3.3f))));
	public static final Item TEMPEST_STAFF = register("tempest_staff", new TempestStaffItem(
		new Item.Settings().rarity(Rarity.EPIC).maxDamage(300)));

	public static final Item INFERNAL_SIGIL = register("infernal_sigil", new SigilItem(() -> ModEntities.INFERNAL_WARDEN, Formatting.RED,
		new Item.Settings().rarity(Rarity.RARE).maxCount(16)));
	public static final Item GLACIAL_SIGIL = register("glacial_sigil", new SigilItem(() -> ModEntities.GLACIAL_SOVEREIGN, Formatting.AQUA,
		new Item.Settings().rarity(Rarity.RARE).maxCount(16)));
	public static final Item CRYSTAL_SIGIL = register("crystal_sigil", new SigilItem(() -> ModEntities.CRYSTAL_BEHEMOTH, Formatting.LIGHT_PURPLE,
		new Item.Settings().rarity(Rarity.RARE).maxCount(16)));

	public static final Item FROST_WRAITH_SPAWN_EGG = registerEgg("frost_wraith", ModEntities.FROST_WRAITH, 0xBFE8F5, 0x3A7CA5);
	public static final Item EMBER_IMP_SPAWN_EGG = registerEgg("ember_imp", ModEntities.EMBER_IMP, 0x8B1A0A, 0xFFB000);
	public static final Item CRYSTAL_CRAWLER_SPAWN_EGG = registerEgg("crystal_crawler", ModEntities.CRYSTAL_CRAWLER, 0x4B2A7A, 0xE070FF);
	public static final Item INFERNAL_WARDEN_SPAWN_EGG = registerEgg("infernal_warden", ModEntities.INFERNAL_WARDEN, 0x2B0A05, 0xFF4400);
	public static final Item GLACIAL_SOVEREIGN_SPAWN_EGG = registerEgg("glacial_sovereign", ModEntities.GLACIAL_SOVEREIGN, 0x1B3A5C, 0x9BE8FF);
	public static final Item CRYSTAL_BEHEMOTH_SPAWN_EGG = registerEgg("crystal_behemoth", ModEntities.CRYSTAL_BEHEMOTH, 0x2A1448, 0xD060FF);

	private static Item register(String name, Item item) {
		Registry.register(Registries.ITEM, ArcaneBestiary.id(name), item);
		ALL.add(item);
		return item;
	}

	private static Item registerEgg(String name, EntityType<? extends net.minecraft.entity.mob.MobEntity> type, int primary, int secondary) {
		return register(name + "_spawn_egg", new SpawnEggItem(type, primary, secondary, new Item.Settings()));
	}

	public static void init() {
	}

	private ModItems() {
	}
}
