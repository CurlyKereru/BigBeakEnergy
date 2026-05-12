package com.bigbeakenergy.entity;

import com.bigbeakenergy.BigBeakEnergy;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

    public static final EntityType<Cassowary> CASSOWARY = register(
            "cassowary",
            EntityType.Builder.<Cassowary>of(Cassowary::new, MobCategory.CREATURE)
                    .sized(0.8f, 1.3f)
    );

    public static final EntityType<Gull> GULL = register(
            "gull",
            EntityType.Builder.<Gull>of(Gull::new, MobCategory.CREATURE)
                    .sized(0.4f, 0.5f)
    );

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(
            String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(
                Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(BigBeakEnergy.MOD_ID, name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void initialize() {}

    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(CASSOWARY, Cassowary.createAttributes());
        FabricDefaultAttributeRegistry.register(GULL, Gull.createAttributes());
    }
}