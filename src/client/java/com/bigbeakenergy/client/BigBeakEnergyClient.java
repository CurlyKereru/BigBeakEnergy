package com.bigbeakenergy.client;

import com.bigbeakenergy.client.renderer.CassowaryRenderer;
import com.bigbeakenergy.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class BigBeakEnergyClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModEntities.CASSOWARY, CassowaryRenderer::new);
		ModEntityModelLayers.initialize();
	}
}



