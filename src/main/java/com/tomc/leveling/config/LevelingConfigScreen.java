package com.tomc.leveling.config;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 游戏内配置界面（经 Mod Menu 打开）。可直接修改客户端与服务端配置并保存。
 * 注意：服务端配置在多人联机时仅修改本机文件，不影响远端服务器。
 */
public class LevelingConfigScreen extends Screen {
    private static final int[] INTERVALS = {0, 5, 10, 15, 30, 60};
    private final Screen parent;

    public LevelingConfigScreen(Screen parent) {
        super(Component.translatable("gui.leveling.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 155;
        int w = 310;
        int y = 42;

        addRenderableWidget(Button.builder(reminderLabel(), btn -> {
            ClientConfig.INSTANCE.reminderEnabled = !ClientConfig.INSTANCE.reminderEnabled;
            btn.setMessage(reminderLabel());
        }).bounds(x, y, w, 20).build());
        y += 26;

        addRenderableWidget(Button.builder(intervalLabel(), btn -> {
            cycleInterval();
            btn.setMessage(intervalLabel());
        }).bounds(x, y, w, 20).build());
        y += 26;

        addRenderableWidget(Button.builder(clearRecipesLabel(), btn -> {
            ServerConfig.INSTANCE.clearRecipesOnUse = !ServerConfig.INSTANCE.clearRecipesOnUse;
            btn.setMessage(clearRecipesLabel());
        }).bounds(x, y, w, 20).build());
        y += 26;

        addRenderableWidget(Button.builder(clearAdvancementsLabel(), btn -> {
            ServerConfig.INSTANCE.clearAdvancementsOnUse = !ServerConfig.INSTANCE.clearAdvancementsOnUse;
            btn.setMessage(clearAdvancementsLabel());
        }).bounds(x, y, w, 20).build());
        y += 32;

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), btn -> {
            ClientConfig.INSTANCE.save();
            ServerConfig.INSTANCE.save();
            if (this.minecraft != null) {
                this.minecraft.setScreen(this.parent);
            }
        }).bounds(x, y, w, 20).build());
    }

    private void cycleInterval() {
        int current = ClientConfig.INSTANCE.reminderIntervalSeconds;
        int nextIndex = 0;
        for (int i = 0; i < INTERVALS.length; i++) {
            if (INTERVALS[i] == current) {
                nextIndex = (i + 1) % INTERVALS.length;
                break;
            }
        }
        ClientConfig.INSTANCE.reminderIntervalSeconds = INTERVALS[nextIndex];
    }

    private Component reminderLabel() {
        return Component.translatable("gui.leveling.config.reminder_enabled", onOff(ClientConfig.INSTANCE.reminderEnabled));
    }

    private Component intervalLabel() {
        return Component.translatable("gui.leveling.config.interval", ClientConfig.INSTANCE.reminderIntervalSeconds);
    }

    private Component clearRecipesLabel() {
        return Component.translatable("gui.leveling.config.clear_recipes", onOff(ServerConfig.INSTANCE.clearRecipesOnUse));
    }

    private Component clearAdvancementsLabel() {
        return Component.translatable("gui.leveling.config.clear_advancements", onOff(ServerConfig.INSTANCE.clearAdvancementsOnUse));
    }

    private Component onOff(boolean value) {
        return Component.translatable(value ? "gui.leveling.config.on" : "gui.leveling.config.off");
    }
}
