package com.bigbeakenergy.client.renderer;

import com.bigbeakenergy.client.model.GullModel;
import com.bigbeakenergy.entity.Gull;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class GullRenderer extends MobRenderer<Gull, GullRenderState, EntityModel<GullRenderState>> {
    private static final Identifier GULL_LOCATION = Identifier.fromNamespaceAndPath("bigbeakenergy", "textures/entity/gull.png");

    public GullRenderer(EntityRendererProvider.Context context) {
        super(context, new GullModel(context.bakeLayer(GullModel.LAYER_LOCATION)), 0.3F);
    }

    @Override
    public GullRenderState createRenderState() {
        return new GullRenderState();
    }

    @Override
    public Identifier getTextureLocation(GullRenderState state) {
        return GULL_LOCATION;
    }

    @Override
    public void extractRenderState(Gull gull, GullRenderState state, float partialTicks) {
        super.extractRenderState(gull, state, partialTicks);
        state.flap = gull.flap;
        state.flapSpeed = gull.flapSpeed;
        state.oFlap = gull.oFlap;
        state.oFlapSpeed = gull.oFlapSpeed;
        float flap = Mth.lerp(partialTicks, gull.oFlap, gull.flap);
        float flapSpeed = Mth.lerp(partialTicks, gull.oFlapSpeed, gull.flapSpeed);
        state.flapAngle = (Mth.sin(flap) + 1.0F) * flapSpeed;
        state.isFlying = gull.isFlying();
        state.isSwimming = gull.isInWater();
    }
}