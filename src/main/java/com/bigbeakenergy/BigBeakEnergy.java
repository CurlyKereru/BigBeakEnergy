package com.bigbeakenergy;

import com.bigbeakenergy.entity.ModEntities;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BigBeakEnergy implements ModInitializer {
	public static final String MOD_ID = "bigbeakenergy";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Big Beak Energy initializing!");
		ModEntities.initialize();
		ModEntities.registerAttributes();
		ModEntitySpawn.initialize();
		ModItemsRegistry.initialize();
		ModBlocksRegistry.initialize();
		BigBeakEnergyCreativeModeTab.initialize();
	}
}