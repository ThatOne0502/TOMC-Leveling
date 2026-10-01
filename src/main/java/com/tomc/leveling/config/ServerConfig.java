package com.tomc.leveling.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tomc.leveling.Leveling;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * 服务端配置：孟婆汤附加效果开关（默认关闭）。
 */
public final class ServerConfig {
    public static final ServerConfig INSTANCE = new ServerConfig();

    public boolean clearRecipesOnUse = false;
    public boolean clearAdvancementsOnUse = false;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    // transient：阻止 Gson 序列化该 Path 字段（否则会抛异常/产生非法 JSON）。
    private transient Path file;

    private ServerConfig() {
    }

    public void load() {
        this.file = FabricLoader.getInstance().getConfigDir().resolve("leveling-server.json");
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                ServerConfig parsed = GSON.fromJson(reader, ServerConfig.class);
                if (parsed != null) {
                    this.clearRecipesOnUse = parsed.clearRecipesOnUse;
                    this.clearAdvancementsOnUse = parsed.clearAdvancementsOnUse;
                }
            } catch (Exception e) {
                Leveling.LOGGER.warn("[leveling] Failed to read server config, using defaults", e);
            }
        } else {
            save();
        }
    }

    public void save() {
        if (file == null) {
            return;
        }
        Path tmp = null;
        try {
            Files.createDirectories(file.getParent());
            tmp = file.resolveSibling(file.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
            try (Reader reader = Files.newBufferedReader(tmp, StandardCharsets.UTF_8)) {
                if (GSON.fromJson(reader, ServerConfig.class) == null) {
                    throw new IllegalStateException("config validation failed");
                }
            }
            try {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception atomicUnsupported) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
            tmp = null;
        } catch (Exception e) {
            Leveling.LOGGER.warn("[leveling] Failed to write server config", e);
        } finally {
            if (tmp != null) {
                try {
                    Files.deleteIfExists(tmp);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
