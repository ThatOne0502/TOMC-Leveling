package com.tomc.leveling;

import com.tomc.leveling.command.LevelingCommand;
import com.tomc.leveling.config.ServerConfig;
import com.tomc.leveling.item.ModItems;
import com.tomc.leveling.item.ModSounds;
import com.tomc.leveling.network.LevelingNetwork;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TOMC-Leveling 主入口。服务端与客户端通用逻辑在此注册；
 * 客户端专属逻辑见 {@link LevelingClient}。
 */
public class Leveling implements ModInitializer {
    public static final String MOD_ID = "leveling";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModSounds.register();
        ModItems.register();
        LevelingNetwork.registerCommon();
        LevelingNetwork.registerServer();
        LevelingEvents.register();
        LevelingCommand.register();
        LevelingLoot.register();
        ServerConfig.INSTANCE.load();
        LOGGER.info("TOMC-Leveling enabled");
    }
}
