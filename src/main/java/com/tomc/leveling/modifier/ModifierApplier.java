package com.tomc.leveling.modifier;

import com.tomc.leveling.Leveling;
import com.tomc.leveling.core.AttributeDefinition;
import com.tomc.leveling.core.LevelingRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * 每项属性只维护一个本模组修饰符，更新时先移除再加。
 */
public final class ModifierApplier {
    private ModifierApplier() {
    }

    public static Identifier modifierId(Identifier attributeId) {
        return Identifier.fromNamespaceAndPath(Leveling.MOD_ID, "upgrade/" + attributeId.getPath());
    }

    public static void apply(ServerPlayer player, Identifier attributeId, AttributeDefinition definition, int levels) {
        AttributeInstance instance = instance(player, attributeId);
        if (instance == null) {
            return;
        }
        Identifier id = modifierId(attributeId);
        instance.removeModifier(id);
        if (levels > 0) {
            instance.addTransientModifier(new AttributeModifier(id, definition.totalValue(levels), definition.operation()));
        }
    }

    public static void remove(ServerPlayer player, Identifier attributeId) {
        AttributeInstance instance = instance(player, attributeId);
        if (instance != null) {
            instance.removeModifier(modifierId(attributeId));
        }
    }

    public static void removeAll(ServerPlayer player) {
        for (AttributeDefinition definition : LevelingRegistry.INSTANCE.definitions().all()) {
            remove(player, definition.attribute());
        }
    }

    private static AttributeInstance instance(ServerPlayer player, Identifier attributeId) {
        Holder<Attribute> holder = BuiltInRegistries.ATTRIBUTE.get(attributeId).orElse(null);
        if (holder == null) {
            Leveling.LOGGER.warn("[leveling] Unknown attribute {}", attributeId);
            return null;
        }
        AttributeInstance instance = player.getAttribute(holder);
        if (instance == null) {
            Leveling.LOGGER.warn("[leveling] Player has no attribute instance for {}", attributeId);
        }
        return instance;
    }
}
