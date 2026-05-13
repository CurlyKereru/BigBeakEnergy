package com.bigbeakenergy.client.model;

import com.bigbeakenergy.client.renderer.GullRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class GullModel extends EntityModel<GullRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("bigbeakenergy", "gull"), "main");

    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart head;
    private final ModelPart lWing;
    private final ModelPart rWing;
    private final ModelPart rLeg;
    private final ModelPart lLeg;

    public GullModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.tail = this.body.getChild("tail");
        this.head = this.body.getChild("head");
        this.lWing = this.body.getChild("lWing");
        this.rWing = this.body.getChild("rWing");
        this.rLeg = this.body.getChild("rLeg");
        this.lLeg = this.body.getChild("lLeg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 19.5F, 0.0F));

        body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(1, 11).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 3.0F));

        body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(14, 0).addBox(-1.0F, -3.0F, -2.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(26, 0).addBox(-0.5F, -1.0F, -4.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, -2.0F));

        body.addOrReplaceChild("lWing", CubeListBuilder.create().texOffs(18, 4).addBox(0.0F, 0.0F, 0.0F, 1.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -2.0F, -2.0F));

        body.addOrReplaceChild("rWing", CubeListBuilder.create().texOffs(18, 4).mirror().addBox(-1.0F, 0.0F, 0.0F, 1.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-2.0F, -2.0F, -2.0F));

        body.addOrReplaceChild("rLeg", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -0.5F, -2.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.4F, 2.0F, 0.0F));

        body.addOrReplaceChild("lLeg", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -0.5F, -2.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.4F, 2.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 16);
    }

    @Override
    public void setupAnim(GullRenderState state) {
        super.setupAnim(state);

        float speed = state.walkAnimationSpeed;
        float position = state.walkAnimationPos;

        float flapAngle = state.flapAngle;
        float flapOffset = (flapAngle - 1.0F) * 0.55F;
        boolean activeWings = state.isFlying && !state.isSwimming;

        lWing.xRot = activeWings ? Mth.HALF_PI : 0.0F;
        rWing.xRot = activeWings ? Mth.HALF_PI : 0.0F;
        lWing.zRot = activeWings ? (Mth.HALF_PI - 0.2F) + flapOffset : 0.0F;
        rWing.zRot = activeWings ? -(Mth.HALF_PI - 0.2F) - flapOffset : 0.0F;

        if (state.isSwimming) {
            body.y -= 0.75F;
        }

        if (activeWings) {
            lWing.y += (flapAngle - 1.0F) * 0.5F;
            rWing.y += (flapAngle - 1.0F) * 0.5F;
            body.y += (flapAngle - 1.0F) * -0.3F;
        }

        head.yRot = state.yRot * (Mth.PI / 180F);
        head.xRot = state.xRot * (Mth.PI / 180F);
        if (!activeWings) {
            head.z += Mth.cos(position * 1.5F + Mth.PI) * 0.5F * speed;
            head.y -= Math.max(0, Mth.sin(position * 1.5F + Mth.PI)) * 0.4F * speed;
        } else {
            head.z += Mth.lerp(state.flapSpeed, 0.0F, -1.0F);
            head.y += Mth.lerp(state.flapSpeed, 0.0F, 1.0F);
            head.xRot += Mth.lerp(state.flapSpeed, 0.0F, 15.0F * (Mth.PI / 180F));
        }
        tail.xRot = Mth.lerp(state.flapSpeed, 0.0F, 0.2F);

        if (!activeWings) {
            rLeg.z = Mth.cos(position * 1.3F) * 1.5F * speed;
            lLeg.z = Mth.cos(position * 1.3F + Mth.PI) * 1.5F * speed;
        }
        float walkXRot = Mth.cos(position * 1.3F) * 0.65F * speed;
        float walkXRotL = Mth.cos(position * 1.3F + Mth.PI) * 0.65F * speed;
        float flyXRot = 75.0F * (Mth.PI / 180F);
        rLeg.xRot = Mth.lerp(state.flapSpeed, walkXRot, flyXRot);
        lLeg.xRot = Mth.lerp(state.flapSpeed, walkXRotL, flyXRot);
    }
}
