package com.bigbeakenergy.client.renderer;

import com.bigbeakenergy.client.model.CassowaryModel;
import com.bigbeakenergy.client.model.CassowaryBabyModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import com.bigbeakenergy.entity.Cassowary;
import net.minecraft.client.model.AdultAndBabyModelPair;

public class CassowaryRenderer extends MobRenderer<Cassowary, CassowaryRenderState, EntityModel<CassowaryRenderState>> {
    private static final Identifier CASSOWARY_LOCATION = Identifier.fromNamespaceAndPath("bigbeakenergy", "textures/entity/cassowary.png");
    private static final Identifier CASSOWARY_BABY_LOCATION = Identifier.fromNamespaceAndPath("bigbeakenergy", "textures/entity/cassowary_baby.png");

    private final AdultAndBabyModelPair<EntityModel<CassowaryRenderState>> models;

    public CassowaryRenderer(EntityRendererProvider.Context context) {
        super(context, new CassowaryModel(context.bakeLayer(CassowaryModel.LAYER_LOCATION)), 0.7F);
        this.models = new AdultAndBabyModelPair<>(
                new CassowaryModel(context.bakeLayer(CassowaryModel.LAYER_LOCATION)),
                new CassowaryBabyModel(context.bakeLayer(CassowaryBabyModel.LAYER_LOCATION))
        );
    }

    @Override
    public void submit(CassowaryRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        this.model = models.getModel(state.isBaby);
        super.submit(state, poseStack, submitNodeCollector, camera);
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