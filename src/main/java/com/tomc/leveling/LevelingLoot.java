package com.tomc.leveling;

import com.tomc.leveling.item.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.Set;

/**
 * 孟婆汤战利品注入：废弃传送门、远古城市、下界要塞、末地城宝箱 + 试炼密室陶罐。
 * 每个目标约 30% 概率（约 3~4 个箱子找到 1 个）。
 */
public final class LevelingLoot {
    private static final Set<ResourceKey<LootTable>> TARGETS = Set.of(
            BuiltInLootTables.RUINED_PORTAL,
            BuiltInLootTables.ANCIENT_CITY,
            BuiltInLootTables.NETHER_BRIDGE,
            BuiltInLootTables.END_CITY_TREASURE,
            BuiltInLootTables.TRIAL_CHAMBERS_CORRIDOR_POT
    );

    private LevelingLoot() {
    }

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (source.isBuiltin() && TARGETS.contains(key)) {
                tableBuilder.withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ModItems.MENGPO_SOUP)
                                .when(LootItemRandomChanceCondition.randomChance(0.3F))));
            }
        });
    }
}
