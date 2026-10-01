package com.tomc.leveling.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Mod Menu 集成：在游戏内模组列表为本模组提供配置入口。
 * 仅在 Mod Menu 存在时通过 modmenu 入口点被加载，Mod Menu 不是必需前置。
 */
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new LevelingConfigScreen(parent);
    }
}
