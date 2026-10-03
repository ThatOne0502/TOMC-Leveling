package io.github.thatone0502.leveling.core;

import io.github.thatone0502.leveling.data.PlayerDataStore;

/**
 * 服务端全局状态：属性定义注册表 + 玩家数据存储。
 */
public final class LevelingRegistry {
    public static final LevelingRegistry INSTANCE = new LevelingRegistry();

    private final AttributeDefinitionRegistry definitions = new AttributeDefinitionRegistry();
    private final PlayerDataStore store = new PlayerDataStore();

    private LevelingRegistry() {
    }

    public AttributeDefinitionRegistry definitions() {
        return definitions;
    }

    public PlayerDataStore store() {
        return store;
    }
}
