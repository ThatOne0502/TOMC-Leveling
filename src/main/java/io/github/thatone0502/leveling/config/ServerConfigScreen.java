package io.github.thatone0502.leveling.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

/**
 * 服务端配置子页：孟婆汤附加效果开关。
 * 仅在单人模式或服务器操作员（等级 ≥2）时可编辑；其它情况下按钮灰显只读。
 */
public class ServerConfigScreen extends Screen {
    private final Screen parent;
    private boolean editable;

    public ServerConfigScreen(Screen parent) {
        super(Component.translatable("gui.leveling.config.server"));
        this.parent = parent;
    }

    public static boolean canEditServerConfig(Minecraft minecraft) {
        if (minecraft.isLocalServer()) {
            return true;
        }
        return minecraft.player != null && minecraft.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    @Override
    protected void init() {
        this.editable = canEditServerConfig(this.minecraft);
        int x = this.width / 2 - 155;
        int w = 310;

        Button recipes = Button.builder(clearRecipesLabel(), btn -> {
            ServerConfig.INSTANCE.clearRecipesOnUse = !ServerConfig.INSTANCE.clearRecipesOnUse;
            btn.setMessage(clearRecipesLabel());
        }).bounds(x, 42, w, 20).build();
        recipes.active = editable;
        addRenderableWidget(recipes);

        Button advancements = Button.builder(clearAdvancementsLabel(), btn -> {
            ServerConfig.INSTANCE.clearAdvancementsOnUse = !ServerConfig.INSTANCE.clearAdvancementsOnUse;
            btn.setMessage(clearAdvancementsLabel());
        }).bounds(x, 68, w, 20).build();
        advancements.active = editable;
        addRenderableWidget(advancements);

        addRenderableWidget(Button.builder(Component.translatable("gui.leveling.config.back"), btn -> back())
                .bounds(x, 110, w, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        if (!editable) {
            graphics.drawCenteredString(this.font, Component.translatable("gui.leveling.config.server_readonly"),
                    this.width / 2, 96, 0xFFAAAAAA);
        }
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
