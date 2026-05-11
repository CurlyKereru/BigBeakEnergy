package com.bigbeakenergy.client.renderer;

import com.bigbeakenergy.client.model.CassowaryModel;
import com.bigbeakenergy.client.model.CassowaryBabyModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import com.bigbeakenergy.entity.Cassowary;

public class CassowaryRenderer extends AgeableMobRenderer<Cassowary, CassowaryRenderState, EntityModel<CassowaryRenderState>> {
    private static final Identifier CASSOWARY_LOCATION = Identifier.fromNamespaceAndPath("bigbeakenergy", "textures/entity/cassowary.png");
    private static final Identifier CASSOWARY_BABY_LOCATION = Identifier.fromNamespaceAndPath("bigbeakenergy", "textures/entity/cassowary_baby.png");

    public CassowaryRenderer(EntityRendererProvider.Context context) {
        super(context, new CassowaryModel(context.bakeLayer(CassowaryModel.LAYER_LOCATION)), new CassowaryBabyModel(context.bakeLayer(CassowaryBabyModel.LAYER_LOCATION)), 0.7F);
    }

    @Override
    public Identifier getTextureLocation(CassowaryRenderState state) {
        return state.isBaby ? CASSOWARY_BABY_LOCATION : CASSOWARY_LOCATION;
    }

    @Override
    public CassowaryRenderState createRenderState() {
        return new CassowaryRenderState();
    }

}