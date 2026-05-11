package com.bigbeakenergy.client;

import com.bigbeakenergy.client.model.CassowaryModel;
import com.bigbeakenergy.client.model.CassowaryBabyModel;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;

public class ModEntityModelLayers {
    public static void initialize() {
        ModelLayerRegistry.registerModelLayer(
                CassowaryModel.LAYER_LOCATION,
                CassowaryModel::createBodyLayer
        );
        ModelLayerRegistry.registerModelLayer(
                CassowaryBabyModel.LAYER_LOCATION,
                CassowaryBabyModel::createBodyLayer
        );
    }
}