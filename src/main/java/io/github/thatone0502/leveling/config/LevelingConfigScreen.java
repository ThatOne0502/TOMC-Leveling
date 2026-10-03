package io.github.thatone0502.leveling.config;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 游戏内配置主界面（经 Mod Menu 打开）。分「客户端配置」「服务端配置」两个子页。
 * 客户端配置自由修改；服务端配置仅在单人模式或服务器操作员时可写，否则只读。
 */
public class LevelingConfigScreen extends Screen {
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

        addRenderableWidget(Button.builder(Component.translatable("gui.leveling.config.client"), btn -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new ClientConfigScreen(this));
            }
        }).bounds(x, y, w, 20).build());
        y += 26;

        addRenderableWidget(Button.builder(Component.translatable("gui.leveling.config.server"), btn -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new ServerConfigScreen(this));
            }
        }).bounds(x, y, w, 20).build());
        y += 32;

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), btn -> done())
                .bounds(x, y, w, 20).build());
    }

    private void done() {
        ClientConfig.INSTANCE.save();
        if (ServerConfigScreen.canEditServerConfig(this.minecraft)) {
            ServerConfig.INSTANCE.save();
        }
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
