package com.tomc.leveling.client.gui;

import com.tomc.leveling.network.payload.AllocationView;
import com.tomc.leveling.network.payload.AttributeDefView;
import com.tomc.leveling.network.payload.PanelDataS2CPayload;
import com.tomc.leveling.network.payload.SpendPointC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 属性升级面板：搜索框 + 可滚动列表，每行 序号 / 属性名 / 当前值 / 已投入点数 / [+] 按钮。
 */
public class LevelingScreen extends Screen {
    private static final int ROW_HEIGHT = 24;
    private static final int LIST_TOP = 58;
    private static final int LIST_BOTTOM_MARGIN = 8;

    // 文本颜色必须带 alpha（顶层字节 0xFF），否则 1.21.11 按 alpha=0 渲染为透明。
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_VALUE = 0xFFAAAAAA;
    private static final int COLOR_DISABLED = 0xFF666666;
    private static final int COLOR_BUTTON_ENABLED = 0xFF555555;
    private static final int COLOR_BUTTON_DISABLED = 0xFF2B2B2B;
    private static final int COLOR_SCROLL_TRACK = 0xFF1E1E1E;
    private static final int COLOR_SCROLL_THUMB = 0xFF888888;

    private static final int ORDER_COL_X = 8;
    private static final int NAME_COL_X = 34;

    private PanelDataS2CPayload data;
    private int scrollOffset = 0;
    private EditBox searchBox;

    private final int buttonWidth = 20;
    private int buttonX;
    private int valueColumnX;
    private int spentColumnX;

    public LevelingScreen(PanelDataS2CPayload data) {
        super(Component.translatable("gui.leveling.title"));
        this.data = data;
    }

    public void updateData(PanelDataS2CPayload data) {
        this.data = data;
    }

    @Override
    protected void init() {
        this.buttonX = this.width - 30;
        this.valueColumnX = this.width / 2;
        this.spentColumnX = this.width - 90;

        this.searchBox = new EditBox(this.font, 8, 34, this.width - 16, 18, Component.translatable("gui.leveling.search"));
        this.searchBox.setHint(Component.translatable("gui.leveling.search"));
        this.searchBox.setMaxLength(100);
        this.addRenderableWidget(this.searchBox);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        Font font = this.font;

        graphics.drawString(font, Component.translatable("gui.leveling.points", data.unspentPoints()), 8, 8, COLOR_TEXT);
        graphics.drawString(font, Component.translatable("gui.leveling.next_level", data.highestLevel() + 1), 8, 20, COLOR_TEXT);

        renderRows(graphics, mouseX, mouseY);
    }

    private List<AttributeDefView> filtered() {
        String query = searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            return data.definitions();
        }
        List<AttributeDefView> result = new ArrayList<>();
        for (AttributeDefView definition : data.definitions()) {
            if (matches(definition, query)) {
                result.add(definition);
            }
        }
        return result;
    }

    private boolean matches(AttributeDefView definition, String query) {
        if (definition.id().toString().toLowerCase(Locale.ROOT).contains(query)) {
            return true;
        }
        if (definition.order() != null && Integer.toString(definition.order()).contains(query)) {
            return true;
        }
        return displayName(definition).toLowerCase(Locale.ROOT).contains(query);
    }

    private void renderRows(GuiGraphics graphics, int mouseX, int mouseY) {
        Font font = this.font;
        List<AttributeDefView> definitions = filtered();
        int listBottom = this.height - LIST_BOTTOM_MARGIN;
        int visible = Math.max(0, (listBottom - LIST_TOP) / ROW_HEIGHT);
        int maxScroll = Math.max(0, definitions.size() - visible);
        if (scrollOffset > maxScroll) {
            scrollOffset = maxScroll;
        }

        Map<Identifier, AllocationView> allocationMap = new HashMap<>();
        for (AllocationView allocation : data.allocations()) {
            allocationMap.put(allocation.id(), allocation);
        }

        int y = LIST_TOP;
        for (int index = scrollOffset; index < definitions.size() && y + ROW_HEIGHT <= listBottom; index++) {
            AttributeDefView definition = definitions.get(index);

            String orderText = definition.order() == null ? "-" : Integer.toString(definition.order());
            graphics.drawString(font, orderText, ORDER_COL_X, y + 6, COLOR_VALUE);

            String name = displayName(definition);
            String truncated = truncate(font, name, valueColumnX - NAME_COL_X - 12);
            graphics.drawString(font, truncated, NAME_COL_X, y + 6, COLOR_TEXT);
            if (mouseX >= NAME_COL_X && mouseX <= valueColumnX && mouseY >= y && mouseY <= y + ROW_HEIGHT) {
                renderNameTooltip(graphics, definition, mouseX, mouseY);
            }

            String currentValue = String.format(Locale.ROOT, "%.2f", getCurrentValue(definition.id()));
            graphics.drawString(font, currentValue, valueColumnX, y + 6, COLOR_VALUE);

            AllocationView allocation = allocationMap.get(definition.id());
            int spent = allocation == null ? 0 : allocation.spent();
            graphics.drawString(font, Component.translatable("gui.leveling.spent", spent).getString(), spentColumnX, y + 6, COLOR_VALUE);

            boolean canAfford = data.unspentPoints() >= definition.cost();
            graphics.fill(buttonX, y + 2, buttonX + buttonWidth, y + ROW_HEIGHT - 2, canAfford ? COLOR_BUTTON_ENABLED : COLOR_BUTTON_DISABLED);
            graphics.drawCenteredString(font, "+", buttonX + buttonWidth / 2, y + 6, canAfford ? COLOR_TEXT : COLOR_DISABLED);

            if (mouseX >= buttonX && mouseX <= buttonX + buttonWidth && mouseY >= y && mouseY <= y + ROW_HEIGHT) {
                renderButtonTooltip(graphics, definition, mouseX, mouseY);
            }

            y += ROW_HEIGHT;
        }

        renderScrollbar(graphics, definitions.size(), maxScroll);
    }

    private void renderNameTooltip(GuiGraphics graphics, AttributeDefView definition, int mouseX, int mouseY) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(displayName(definition)));
        lines.add(Component.literal(definition.id().toString()));
        String description = descriptionText(definition);
        if (description != null) {
            lines.add(Component.literal(description));
        }
        graphics.setComponentTooltipForNextFrame(this.font, lines, mouseX, mouseY);
    }

    private void renderButtonTooltip(GuiGraphics graphics, AttributeDefView definition, int mouseX, int mouseY) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("gui.leveling.cost", definition.cost()));
        String description = descriptionText(definition);
        if (description != null) {
            lines.add(Component.literal(description));
        }
        graphics.setComponentTooltipForNextFrame(this.font, lines, mouseX, mouseY);
    }

    private void renderScrollbar(GuiGraphics graphics, int total, int maxScroll) {
        if (maxScroll <= 0) {
            return;
        }
        int listBottom = this.height - LIST_BOTTOM_MARGIN;
        int trackHeight = listBottom - LIST_TOP;
        int visible = Math.max(1, (listBottom - LIST_TOP) / ROW_HEIGHT);
        int thumbHeight = Math.max(16, trackHeight * visible / Math.max(1, total));
        int thumbY = LIST_TOP + (int) ((trackHeight - thumbHeight) * ((double) scrollOffset / maxScroll));
        graphics.fill(this.width - 4, LIST_TOP, this.width - 2, listBottom, COLOR_SCROLL_TRACK);
        graphics.fill(this.width - 4, thumbY, this.width - 2, thumbY + thumbHeight, COLOR_SCROLL_THUMB);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int listBottom = this.height - LIST_BOTTOM_MARGIN;
        int visible = Math.max(0, (listBottom - LIST_TOP) / ROW_HEIGHT);
        int maxScroll = Math.max(0, filtered().size() - visible);
        if (scrollY > 0) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else if (scrollY < 0) {
            scrollOffset = Math.min(maxScroll, scrollOffset + 1);
        }
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            double mouseX = event.x();
            double mouseY = event.y();
            List<AttributeDefView> definitions = filtered();
            int listBottom = this.height - LIST_BOTTOM_MARGIN;
            int visible = Math.max(0, (listBottom - LIST_TOP) / ROW_HEIGHT);
            int maxScroll = Math.max(0, definitions.size() - visible);
            int clampedScroll = Math.min(scrollOffset, maxScroll);

            int y = LIST_TOP;
            for (int index = clampedScroll; index < definitions.size() && y + ROW_HEIGHT <= listBottom; index++) {
                if (mouseX >= buttonX && mouseX <= buttonX + buttonWidth && mouseY >= y && mouseY <= y + ROW_HEIGHT) {
                    AttributeDefView definition = definitions.get(index);
                    if (data.unspentPoints() >= definition.cost()) {
                        playClickSound();
                        ClientPlayNetworking.send(new SpendPointC2SPayload(definition.id()));
                    }
                    return true;
                }
                y += ROW_HEIGHT;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    /** 显示名：用 getOrDefault 拿原始翻译（不经过 String.format，避免 % 触发 Format error）。 */
    private String displayName(AttributeDefView definition) {
        if (definition.displayKey() != null && !definition.displayKey().isEmpty() && Language.getInstance().has(definition.displayKey())) {
            return Language.getInstance().getOrDefault(definition.displayKey());
        }
        return definition.id().toString();
    }

    /** 描述：同样用 getOrDefault 拿原始翻译；键不存在返回 null。 */
    private String descriptionText(AttributeDefView definition) {
        if (definition.descriptionKey() != null && !definition.descriptionKey().isEmpty() && Language.getInstance().has(definition.descriptionKey())) {
            return Language.getInstance().getOrDefault(definition.descriptionKey());
        }
        return null;
    }

    private double getCurrentValue(Identifier attributeId) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return 0.0;
        }
        Holder<Attribute> holder = BuiltInRegistries.ATTRIBUTE.get(attributeId).orElse(null);
        if (holder == null) {
            return 0.0;
        }
        AttributeInstance instance = client.player.getAttribute(holder);
        return instance == null ? 0.0 : instance.getValue();
    }

    private String truncate(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int ellipsisWidth = font.width(ellipsis);
        int prefixWidth = Math.max(2, maxWidth - ellipsisWidth);
        return font.plainSubstrByWidth(text, prefixWidth) + ellipsis;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
