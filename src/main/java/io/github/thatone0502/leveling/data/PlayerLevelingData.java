package io.github.thatone0502.leveling.data;

import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 单个玩家的等级化数据（Gson 序列化到 world/leveling/players/&lt;uuid&gt;.json）。
 */
public final class PlayerLevelingData {
    public int dataVersion = 1;
    public int highestLevel = 0;
    public int unspentPoints = 0;
    public Map<String, Allocation> allocations = new LinkedHashMap<>();

    public static final class Allocation {
        public int levels = 0;
        public int spent = 0;
    }

    public Allocation getAllocation(Identifier id) {
        return allocations.computeIfAbsent(id.toString(), k -> new Allocation());
    }
}
