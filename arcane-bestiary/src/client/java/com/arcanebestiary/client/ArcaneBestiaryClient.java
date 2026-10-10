package com.arcanebestiary.client;

import com.arcanebestiary.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public class ArcaneBestiaryClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.FROST_WRAITH, ctx -> new HumanoidMobRenderer<>(ctx, "frost_wraith", 1.0f, false));
		EntityRendererRegistry.register(ModEntities.EMBER_IMP, ctx -> new HumanoidMobRenderer<>(ctx, "ember_imp", 0.7f, false));
		EntityRendererRegistry.register(ModEntities.CRYSTAL_CRAWLER, ctx -> new ArachnidMobRenderer<>(ctx, "crystal_crawler", 1.0f, false));

		EntityRendererRegistry.register(ModEntities.INFERNAL_WARDEN, ctx -> new HumanoidMobRenderer<>(ctx, "infernal_warden", 2.0f, true));
		EntityRendererRegistry.register(ModEntities.GLACIAL_SOVEREIGN, ctx -> new HumanoidMobRenderer<>(ctx, "glacial_sovereign", 1.7f, true));
		EntityRendererRegistry.register(ModEntities.CRYSTAL_BEHEMOTH, ctx -> new ArachnidMobRenderer<>(ctx, "crystal_behemoth", 3.0f, true));

		EntityRendererRegistry.register(ModEntities.FROST_BOLT, FlyingItemEntityRenderer::new);
	}
}
