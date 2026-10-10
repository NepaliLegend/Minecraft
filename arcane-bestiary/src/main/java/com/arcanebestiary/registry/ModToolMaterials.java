package com.arcanebestiary.registry;

import net.minecraft.block.Block;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

import java.util.function.Supplier;

public enum ModToolMaterials implements ToolMaterial {
	CRYSTALITE(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1400, 8.5f, 3.0f, 18, () -> Ingredient.ofItems(ModItems.CRYSTALITE_SHARD)),
	EMBER(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2400, 9.0f, 4.0f, 22, () -> Ingredient.ofItems(ModItems.INFERNAL_CORE)),
	FROST(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2400, 9.0f, 4.0f, 22, () -> Ingredient.ofItems(ModItems.GLACIAL_CORE)),
	TITAN(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 3000, 6.0f, 5.0f, 15, () -> Ingredient.ofItems(ModItems.CRYSTAL_HEART));

	private final TagKey<Block> inverseTag;
	private final int durability;
	private final float miningSpeed;
	private final float attackDamage;
	private final int enchantability;
	private final Supplier<Ingredient> repairIngredient;

	ModToolMaterials(TagKey<Block> inverseTag, int durability, float miningSpeed, float attackDamage, int enchantability, Supplier<Ingredient> repairIngredient) {
		this.inverseTag = inverseTag;
		this.durability = durability;
		this.miningSpeed = miningSpeed;
		this.attackDamage = attackDamage;
		this.enchantability = enchantability;
		this.repairIngredient = repairIngredient;
	}

	@Override
	public int getDurability() {
		return durability;
	}

	@Override
	public float getMiningSpeedMultiplier() {
		return miningSpeed;
	}

	@Override
	public float getAttackDamage() {
		return attackDamage;
	}

	@Override
	public TagKey<Block> getInverseTag() {
		return inverseTag;
	}

	@Override
	public int getEnchantability() {
		return enchantability;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return repairIngredient.get();
	}
}
