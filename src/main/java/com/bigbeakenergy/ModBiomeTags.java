package com.bigbeakenergy;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class ModBiomeTags {
    public static class Biomes {
        public static final TagKey<Biome> SPAWNS_CASSOWARY = TagKey.create(
                Registries.BIOME,
                Identifier.fromNamespaceAndPath("bigbeakenergy", "spawns_cassowary")
        );
    }
}