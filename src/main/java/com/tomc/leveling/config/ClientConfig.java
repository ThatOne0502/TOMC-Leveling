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
 * 客户端配置：actionbar 提醒开关 + 提醒周期（秒，非负，0 表示常态显示）。
 * 周期由客户端驱动：客户端按此周期向服务端申请数据核查，再自行决定是否提醒。
 */
public final class ClientConfig {
    public static final ClientConfig INSTANCE = new ClientConfig();

    public boolean reminderEnabled = true;
    public int reminderIntervalSeconds = 10;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    // transient：阻止 Gson 序列化该 Path 字段。
    private transient Path file;

    private ClientConfig() {
    }

    public void load() {
        this.file = FabricLoader.getInstance().getConfigDir().resolve("leveling-client.json");
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                ClientConfig parsed = GSON.fromJson(reader, ClientConfig.class);
                if (parsed != null) {
                    this.reminderEnabled = parsed.reminderEnabled;
                    this.reminderIntervalSeconds = Math.max(0, parsed.reminderIntervalSeconds);
                }
            } catch (Exception e) {
                Leveling.LOGGER.warn("[leveling] Failed to read client config, using defaults", e);
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
                if (GSON.fromJson(reader, ClientConfig.class) == null) {
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
            Leveling.LOGGER.warn("[leveling] Failed to write client config", e);
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
