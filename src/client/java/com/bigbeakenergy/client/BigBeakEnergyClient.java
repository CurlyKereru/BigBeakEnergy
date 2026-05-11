package com.bigbeakenergy.client;

import com.bigbeakenergy.entity.ModEntities;
import com.bigbeakenergy.client.renderer.CassowaryRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class BigBeakEnergyClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.CASSOWARY, CassowaryRenderer::new);
		ModEntityModelLayers.initialize();
	}
}