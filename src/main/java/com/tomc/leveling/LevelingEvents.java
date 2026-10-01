package com.tomc.leveling;

import com.tomc.leveling.core.LevelingRegistry;
import com.tomc.leveling.core.LevelingService;
import com.tomc.leveling.data.DefinitionDataLoader;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;

/**
 * 服务端事件：生命周期、数据包重载、登录/退服/重生。
 */
public final class LevelingEvents {
    private LevelingEvents() {
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(server ->
                LevelingRegistry.INSTANCE.store().init(server));

        ServerLifecycleEvents.SERVER_STOPPED.register(server ->
                LevelingRegistry.INSTANCE.store().invalidateAll());

        ResourceManagerHelper.get(PackType.SERVER_DATA)
                .registerReloadListener(new DefinitionDataLoader(LevelingRegistry.INSTANCE.definitions()));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            server.execute(() -> LevelingService.INSTANCE.onPlayerJoin(player));
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();
            if (player != null) {
                LevelingRegistry.INSTANCE.store().invalidate(player.getUUID());
            }
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                LevelingService.INSTANCE.onPlayerJoin(newPlayer));
    }
}
