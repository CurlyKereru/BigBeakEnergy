package com.bigbeakenergy.client.model;

import com.bigbeakenergy.client.renderer.CassowaryRenderState;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class CassowaryBabyModel extends CassowaryModel {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("bigbeakenergy", "cassowary_baby"), "main");

    private final ModelPart Body;
    private final ModelPart Head;
    private final ModelPart RLeg;
    private final ModelPart LLeg;

    public CassowaryBabyModel(ModelPart root) {
        super(root);
        this.Body = root.getChild("Body");
        this.Head = this.Body.getChild("Neck").getChild("Head");
        this.RLeg = this.Body.getChild("RLeg");
        this.LLeg = this.Body.getChild("LLeg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Body = partdefinition.addOrReplaceChild("Body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.5F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 19.0F, 0.5F));

        PartDefinition Neck = Body.addOrReplaceChild("Neck",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -1.0F, -2.5F));

        PartDefinition Head = Neck.addOrReplaceChild("Head",
                CubeListBuilder.create().texOffs(0, 9).addBox(-1.0F, -3.0F, -2.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(8, 9).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        Body.addOrReplaceChild("RLeg",
                CubeListBuilder.create().texOffs(8, 11).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.0F, 2.0F, 0.5F));

        Body.addOrReplaceChild("LLeg",
                CubeListBuilder.create().texOffs(8, 11).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.0F, 2.0F, 0.5F));

        return LayerDefinition.create(meshdefinition, 32, 16);
    }

    @Override
    public void setupAnim(CassowaryRenderState state) {
        float speed = state.walkAnimationSpeed;
        float position = state.walkAnimationPos;

        Body.y = 19.0F + Mth.sin(position * 1.0F) * 0.3F * speed;
        Body.zRot = Mth.sin(position * 1.0F) * 0.2F * speed;

        RLeg.xRot = Mth.cos(position * 1.3F) * 0.65F * speed;
        LLeg.xRot = Mth.cos(position * 1.3F + Mth.PI) * 0.65F * speed;
        RLeg.z = Mth.cos(position * 1.3F) * 1.5F * speed;
        LLeg.z = Mth.cos(position * 1.3F + Mth.PI) * 1.5F * speed;
        RLeg.y = 2.0F - Math.max(0, Mth.sin(position * 1.3F)) * 1.5F * speed;
        LLeg.y = 2.0F - Math.max(0, Mth.sin(position * 1.3F + Mth.PI)) * 1.5F * speed;

        Head.z = Math.max(0, Mth.cos(position * 1.5F + Mth.PI) * 1.9F * speed);
        Head.y = Math.max(0, Mth.sin(position * 1.5F + Mth.PI)) * 0.4F * speed;
        Head.yRot = state.yRot * (Mth.PI / 180F);
        Head.xRot = state.xRot * (Mth.PI / 180F);
    }
}