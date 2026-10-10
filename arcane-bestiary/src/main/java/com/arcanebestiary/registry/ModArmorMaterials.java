package com.arcanebestiary.registry;

import com.arcanebestiary.ArcaneBestiary;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.List;

public final class ModArmorMaterials {
	public static final RegistryEntry<ArmorMaterial> CRYSTALITE = register("crystalite",
		3, 8, 6, 3, 20, 2.5f, 0.05f);

	private static RegistryEntry<ArmorMaterial> register(String name, int helmet, int chest, int legs, int boots,
	                                                     int enchantability, float toughness, float knockbackResistance) {
		EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
		defense.put(ArmorItem.Type.HELMET, helmet);
		defense.put(ArmorItem.Type.CHESTPLATE, chest);
		defense.put(ArmorItem.Type.LEGGINGS, legs);
		defense.put(ArmorItem.Type.BOOTS, boots);
		defense.put(ArmorItem.Type.BODY, chest);
		Identifier id = ArcaneBestiary.id(name);
		ArmorMaterial material = new ArmorMaterial(defense, enchantability,
			SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND,
			() -> Ingredient.ofItems(ModItems.CRYSTALITE_SHARD),
			List.of(new ArmorMaterial.Layer(id)), toughness, knockbackResistance);
		return Registry.registerReference(Registries.ARMOR_MATERIAL, id, material);
	}

	public static void init() {
	}

	private ModArmorMaterials() {
	}
}
