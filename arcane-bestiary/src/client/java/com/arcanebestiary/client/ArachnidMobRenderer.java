package com.arcanebestiary.client;

import com.arcanebestiary.ArcaneBestiary;
import com.arcanebestiary.entity.BossEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SpiderEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

/** Renders eight-legged Arcane Bestiary creatures using the vanilla spider rig with custom textures. */
public class ArachnidMobRenderer<T extends MobEntity> extends MobEntityRenderer<T, SpiderEntityModel<T>> {
	private final Identifier texture;
	private final Identifier enragedTexture;
	private final float scale;

	public ArachnidMobRenderer(EntityRendererFactory.Context ctx, String name, float scale, boolean hasEnragedVariant) {
		super(ctx, new SpiderEntityModel<>(ctx.getPart(EntityModelLayers.SPIDER)), 0.8f * scale);
		this.texture = ArcaneBestiary.id("textures/entity/" + name + ".png");
		this.enragedTexture = hasEnragedVariant ? ArcaneBestiary.id("textures/entity/" + name + "_enraged.png") : null;
		this.scale = scale;
		addFeature(new GlowFeatureRenderer<>(this, ArcaneBestiary.id("textures/entity/" + name + "_glow.png"),
			hasEnragedVariant ? ArcaneBestiary.id("textures/entity/" + name + "_enraged_glow.png") : null));
	}

	@Override
	public Identifier getTexture(T entity) {
		if (enragedTexture != null && entity instanceof BossEntity boss && boss.isEnraged()) {
			return enragedTexture;
		}
		return texture;
	}

	@Override
	protected float getLyingAngle(T entity) {
		return 180.0f;
	}

	@Override
	protected void scale(T entity, MatrixStack matrices, float amount) {
		matrices.scale(scale, scale, scale);
	}
}
