package com.arcanebestiary.client;

import com.arcanebestiary.entity.BossEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/** Renders a fullbright overlay texture (eyes, cracks, runes) on top of the base entity texture. */
public class GlowFeatureRenderer<T extends Entity, M extends EntityModel<T>> extends EyesFeatureRenderer<T, M> {
	private final RenderLayer layer;
	private final RenderLayer enragedLayer;

	public GlowFeatureRenderer(FeatureRendererContext<T, M> context, Identifier glowTexture, @Nullable Identifier enragedGlowTexture) {
		super(context);
		this.layer = RenderLayer.getEyes(glowTexture);
		this.enragedLayer = enragedGlowTexture == null ? layer : RenderLayer.getEyes(enragedGlowTexture);
	}

	@Override
	public RenderLayer getEyesTexture() {
		return layer;
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity,
	                   float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
		boolean enraged = entity instanceof BossEntity boss && boss.isEnraged();
		VertexConsumer consumer = vertexConsumers.getBuffer(enraged ? enragedLayer : layer);
		this.getContextModel().render(matrices, consumer, 0xF00000, OverlayTexture.DEFAULT_UV);
	}
}
