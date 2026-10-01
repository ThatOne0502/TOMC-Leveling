package com.tomc.leveling.item;

import com.tomc.leveling.Leveling;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
    public static final SoundEvent LEVELING = Registry.register(
            BuiltInRegistries.SOUND_EVENT,
            Identifier.fromNamespaceAndPath(Leveling.MOD_ID, "leveling"),
            SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(Leveling.MOD_ID, "leveling"))
    );

    private ModSounds() {
    }

    /** 在 onInitialize 中调用，触发本类静态初始化完成声音注册。 */
    public static void register() {
    }
}
