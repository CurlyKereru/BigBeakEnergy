package com.bigbeakenergy;

import com.bigbeakenergy.entity.ModEntities;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.entity.animal.Animal;

public class ModEntitySpawn {
    public static void initialize() {
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ModBiomeTags.Biomes.SPAWNS_CASSOWARY),
                MobCategory.CREATURE,
                ModEntities.CASSOWARY,
                8,  // weight
                1,  // min group size
                1   // max group size
        );

        SpawnPlacements.register(
                ModEntities.CASSOWARY,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules
        );
    }
}