package com.bigbeakenergy;

import com.bigbeakenergy.entity.ModEntities;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;

public class ModItemsRegistry {

    public static final ResourceKey<Item> CASSOWARY_SPAWN_EGG_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath("bigbeakenergy", "cassowary_spawn_egg")
    );

    public static final SpawnEggItem CASSOWARY_SPAWN_EGG = Registry.register(
            BuiltInRegistries.ITEM,
            CASSOWARY_SPAWN_EGG_KEY,
            new SpawnEggItem(new Item.Properties()
                    .setId(CASSOWARY_SPAWN_EGG_KEY)
                    .component(DataComponents.ENTITY_DATA,
                            TypedEntityData.of(ModEntities.CASSOWARY, new CompoundTag())))
    );

    public static void initialize() {}
}