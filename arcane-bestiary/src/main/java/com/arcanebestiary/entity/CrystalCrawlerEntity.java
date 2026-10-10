package com.arcanebestiary.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.world.World;

/** A cave spider-like creature grown over with crystal. Attackers are cut by its spikes. */
public class CrystalCrawlerEntity extends SpiderEntity {
	public CrystalCrawlerEntity(EntityType<? extends CrystalCrawlerEntity> type, World world) {
		super(type, world);
		this.experiencePoints = 10;
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return SpiderEntity.createSpiderAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 24.0)
			.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
			.add(EntityAttributes.GENERIC_ARMOR, 4.0);
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		boolean result = super.damage(source, amount);
		if (result && source.getAttacker() instanceof LivingEntity attacker
			&& source.getSource() == attacker && !source.isIn(DamageTypeTags.IS_PROJECTILE)) {
			attacker.damage(getDamageSources().thorns(this), 2.0f);
		}
		return result;
	}
}
