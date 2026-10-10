package com.arcanebestiary.client;

import com.arcanebestiary.ArcaneBestiary;
import com.arcanebestiary.entity.BossEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.Identifier;

/** Renders Arcane Bestiary humanoids using the vanilla zombie rig with custom textures. */
public class HumanoidMobRenderer<T extends HostileEntity> extends BipedEntityRenderer<T, AggressiveBipedModel<T>> {
	private final Identifier texture;
	private final Identifier enragedTexture;
	private final float scale;

	public HumanoidMobRenderer(EntityRendererFactory.Context ctx, String name, float scale, boolean hasEnragedVariant) {
		super(ctx, new AggressiveBipedModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)), 0.5f * scale);
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
	protected void scale(T entity, MatrixStack matrices, float amount) {
		matrices.scale(scale, scale, scale);
	}
}
