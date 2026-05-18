package com.bigbeakenergy;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {

    public static final SoundEvent GULL_PANIC = registerSound("gull_panic");
    public static final SoundEvent GULL_AMBIENT_1 = registerSound("gull_ambient_1");
    public static final SoundEvent GULL_AMBIENT_2 = registerSound("gull_ambient_2");
    public static final SoundEvent GULL_STEP = registerSound("gull_step");
    public static final SoundEvent GULL_FLAP = registerSound("gull_flap");

    private static SoundEvent registerSound(String id) {
        Identifier identifier = Identifier.fromNamespaceAndPath("bigbeakenergy", id);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, identifier,
                SoundEvent.createVariableRangeEvent(identifier));
    }

    public static void initialize() {}
}