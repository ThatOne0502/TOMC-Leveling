package io.github.thatone0502.leveling.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 客户端配置子页：提醒开关、提醒周期、获得属性点音效开关。
 * 这些均为本地配置，任何情况下都可自由修改。
 */
public class ClientConfigScreen extends Screen {
    private final Screen parent;
    private EditBox intervalBox;
    private int intervalLabelX;
    private int intervalLabelY;

    public ClientConfigScreen(Screen parent) {
        super(Component.translatable("gui.leveling.config.client"));
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

        this.intervalLabelX = x;
        this.intervalLabelY = y;
        y += 12;

        this.intervalBox = new EditBox(this.font, x, y, w, 20,
                Component.translatable("gui.leveling.config.interval_hint"));
        this.intervalBox.setMaxLength(6);
        this.intervalBox.setFilter(ClientConfigScreen::digitsOnly);
        this.intervalBox.setResponder(this::onIntervalChanged);
        this.intervalBox.setValue(Integer.toString(ClientConfig.INSTANCE.reminderIntervalSeconds));
        this.addRenderableWidget(this.intervalBox);
        y += 26;

        addRenderableWidget(Button.builder(soundLabel(), btn -> {
            ClientConfig.INSTANCE.levelingSoundEnabled = !ClientConfig.INSTANCE.levelingSoundEnabled;
            btn.setMessage(soundLabel());
        }).bounds(x, y, w, 20).build());
        y += 32;

        addRenderableWidget(Button.builder(Component.translatable("gui.leveling.config.back"), btn -> back())
                .bounds(x, y, w, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        graphics.drawString(this.font, Component.translatable("gui.leveling.config.interval_hint"),
                this.intervalLabelX, this.intervalLabelY, 0xFFFFFFFF);
    }

    private void back() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public void onClose() {
        back();
    }

    private static boolean digitsOnly(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private void onIntervalChanged(String value) {
        if (value.isEmpty()) {
            return;
        }
        try {
            int parsed = Integer.parseInt(value);
            if (parsed >= 0) {
                ClientConfig.INSTANCE.reminderIntervalSeconds = parsed;
            }
        } catch (NumberFormatException ignored) {
            // filter 已限制为数字，理论不会发生；保留防御。
        }
    }

    private Component reminderLabel() {
        return Component.translatable("gui.leveling.config.reminder_enabled", onOff(ClientConfig.INSTANCE.reminderEnabled));
    }

    private Component soundLabel() {
        return Component.translatable("gui.leveling.config.sound", onOff(ClientConfig.INSTANCE.levelingSoundEnabled));
    }

    private Component onOff(boolean value) {
        return Component.translatable(value ? "gui.leveling.config.on" : "gui.leveling.config.off");
    }
}
