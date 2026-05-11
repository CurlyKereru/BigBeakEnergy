package com.bigbeakenergy;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import com.bigbeakenergy.block.CassowaryEggBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class ModBlocksRegistry {

    public static final ResourceKey<Block> CASSOWARY_EGG_KEY = ResourceKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath("bigbeakenergy", "cassowary_egg")
    );

    public static final Block CASSOWARY_EGG = Registry.register(
            BuiltInRegistries.BLOCK,
            CASSOWARY_EGG_KEY,
            new CassowaryEggBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.TURTLE_EGG)
                            .randomTicks()
                            .setId(CASSOWARY_EGG_KEY)
            )
    );

    public static final ResourceKey<Item> CASSOWARY_EGG_ITEM_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath("bigbeakenergy", "cassowary_egg")
    );

    public static final BlockItem CASSOWARY_EGG_ITEM = Registry.register(
            BuiltInRegistries.ITEM,
            CASSOWARY_EGG_ITEM_KEY,
            new BlockItem(CASSOWARY_EGG, new Item.Properties().setId(CASSOWARY_EGG_ITEM_KEY))
    );

    public static void initialize() {}
}