package com.bigbeakenergy;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class BigBeakEnergyCreativeModeTab {

    @SuppressWarnings("unused")
    public static final CreativeModeTab BIG_BEAK_ENERGY = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath("bigbeakenergy", "big_beak_energy"),
            FabricCreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocksRegistry.CASSOWARY_EGG_ITEM))
                    .title(Component.translatable("itemGroup.bigbeakenergy.big_beak_energy"))
                    .displayItems((_, output) -> {
                        output.accept(ModBlocksRegistry.CASSOWARY_EGG_ITEM);
                        output.accept(ModItemsRegistry.CASSOWARY_SPAWN_EGG);
                        output.accept(ModItemsRegistry.GULL_SPAWN_EGG);
                    })
                    .build()
    );

    public static void initialize() {}
}