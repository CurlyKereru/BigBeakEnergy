package com.bigbeakenergy.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.Identifier;
import com.bigbeakenergy.client.renderer.CassowaryRenderState;
import net.minecraft.util.Mth;

public class CassowaryModel extends EntityModel<CassowaryRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("bigbeakenergy", "cassowary"), "main");

    private final ModelPart Body;
    private final ModelPart Neck;
    private final ModelPart Head;
    private final ModelPart RLeg;
    private final ModelPart LLeg;

    public CassowaryModel(ModelPart root) {
        super(root);
        this.Body = root.getChild("Body");
        this.Neck = this.Body.getChild("Neck");
        this.Head = this.Neck.getChild("Head");
        this.RLeg = this.Body.getChild("RLeg");
        this.LLeg = this.Body.getChild("LLeg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(16, 8).addBox(-5.0F, -5.5F, -6.5F, 10.0F, 11.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 11.5F, 1.5F));
        PartDefinition Neck = Body.addOrReplaceChild("Neck", CubeListBuilder.create().texOffs(7, 0).addBox(-3.0F, -4.0F, -2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.5F, -8.5F));
        PartDefinition Head = Neck.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(50, 0).addBox(-2.0F, -5.0F, -3.0F, 4.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(52, 10).addBox(-2.0F, 2.0F, -3.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(39, 0).addBox(-1.0F, -4.0F, -6.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(28, 0).addBox(-1.0F, -8.0F, -4.0F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, -2.0F));
        PartDefinition RLeg = Body.addOrReplaceChild("RLeg", CubeListBuilder.create().texOffs(2, 19).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(2, 28).addBox(1.5F, 6.0F, -3.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(2, 28).addBox(0.0F, 6.0F, -3.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(2, 28).addBox(-1.5F, 6.0F, -3.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, 5.5F, -1.0F));
        PartDefinition LLeg = Body.addOrReplaceChild("LLeg", CubeListBuilder.create().texOffs(2, 19).mirror().addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(2, 28).addBox(1.5F, 6.0F, -3.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(2, 28).addBox(0.0F, 6.0F, -3.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(2, 28).addBox(-1.5F, 6.0F, -3.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, 5.5F, -1.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(CassowaryRenderState state) {
        super.setupAnim(state);

        float speed = state.walkAnimationSpeed;
        float position = state.walkAnimationPos;

        // Legs
        RLeg.xRot = Mth.cos(position * 1.0F) * 0.8F * speed;
        LLeg.xRot = Mth.cos(position * 1.0F + Mth.PI) * 0.8F * speed;
        RLeg.z = Mth.cos(position * 1.0F) * 4.1F * speed;
        LLeg.z = Mth.cos(position * 1.0F + Mth.PI) * 4.1F * speed;
        RLeg.y = 5.5F - Math.max(0, Mth.sin(position * 1.0F)) * 3.5F * speed;
        LLeg.y = 5.5F - Math.max(0, Mth.sin(position * 1.0F + Mth.PI)) * 3.5F * speed;

        // Body sway
        Body.y = 11.5F + Mth.sin(position * 1.0F) * 0.3F * speed;
        Body.zRot = Mth.sin(position * 1.0F) * 0.2F * speed;

        // Neck bob
        Neck.xRot = Mth.sin(position * 2.0F + Mth.PI) * 0.3F * speed;
        Neck.zRot = Mth.sin(position * 1.0F+ Mth.PI) * 0.2F * speed;

        // Head bob
        Head.z = -2.0F + Math.max(0, Mth.cos(position * 2.0F + Mth.PI) * 3.9F * speed);
        Head.y = -3.0F - Math.max(0, Mth.sin(position * 2.0F + Mth.PI)) * 0.75F * speed;

        // Head look — blend with bob by adding to xRot instead of overwriting
        Head.yRot = state.yRot * (Mth.PI / 180F);
        Head.xRot += state.xRot * (Mth.PI / 180F);
    }

}