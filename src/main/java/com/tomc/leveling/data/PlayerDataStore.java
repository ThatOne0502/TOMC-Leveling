package com.tomc.leveling.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tomc.leveling.Leveling;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * UUID 文件读写 + 内存缓存。文件位于 world/leveling/players/&lt;uuid32hex&gt;.json。
 */
public final class PlayerDataStore {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final Map<UUID, PlayerLevelingData> cache = new HashMap<>();
    private Path playersDir;

    public void init(MinecraftServer server) {
        this.playersDir = server.getWorldPath(LevelResource.ROOT).resolve("leveling").resolve("players");
        this.cache.clear();
    }

    private Path fileFor(UUID uuid) {
        return playersDir.resolve(uuid.toString().replace("-", "") + ".json");
    }

    public PlayerLevelingData load(UUID uuid) {
        PlayerLevelingData cached = cache.get(uuid);
        if (cached != null) {
            return cached;
        }
        PlayerLevelingData data = new PlayerLevelingData();
        Path file = fileFor(uuid);
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                PlayerLevelingData parsed = GSON.fromJson(reader, PlayerLevelingData.class);
                if (parsed != null) {
                    data = parsed;
                }
            } catch (Exception e) {
                Leveling.LOGGER.error("[leveling] Failed to load player data {}, falling back to defaults", file, e);
                try {
                    Files.move(file, file.resolveSibling(file.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception ignored) {
                }
            }
        }
        cache.put(uuid, data);
        return data;
    }

    public void save(UUID uuid, PlayerLevelingData data) {
        if (playersDir == null) {
            return;
        }
        try {
            Files.createDirectories(playersDir);
            Path file = fileFor(uuid);
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
            // 自校验：反序列化一次，失败则丢弃临时文件、保留旧数据。
            try (Reader reader = Files.newBufferedReader(tmp, StandardCharsets.UTF_8)) {
                if (GSON.fromJson(reader, PlayerLevelingData.class) == null) {
                    throw new IllegalStateException("player data validation failed");
                }
            }
            try {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception atomicUnsupported) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
            cache.put(uuid, data);
        } catch (Exception e) {
            Leveling.LOGGER.error("[leveling] Failed to save player data for {}", uuid, e);
        }
    }

    public void invalidate(UUID uuid) {
        cache.remove(uuid);
    }

    public void invalidateAll() {
        cache.clear();
    }
}
