package com.bigbeakenergy;

import com.bigbeakenergy.entity.ModEntities;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.levelgen.Heightmap;
import com.bigbeakenergy.entity.Gull;

public class ModEntitySpawn {
    public static void initialize() {
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ModBiomeTags.Biomes.SPAWNS_CASSOWARY),
                MobCategory.CREATURE,
                ModEntities.CASSOWARY,
                6,  // weight
                1,  // min group size
                1   // max group size
        );

        SpawnPlacements.register(
                ModEntities.CASSOWARY,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules
        );

        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ModBiomeTags.Biomes.SPAWNS_GULL),
                MobCategory.CREATURE,
                ModEntities.GULL,
                12,  // weight
                3,  // min group size
                6   // max group size
        );

        SpawnPlacements.register(
                ModEntities.GULL,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Gull::checkGullSpawnRules
        );
    }
}