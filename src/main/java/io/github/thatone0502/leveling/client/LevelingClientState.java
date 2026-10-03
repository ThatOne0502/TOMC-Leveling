package io.github.thatone0502.leveling.client;

import io.github.thatone0502.leveling.client.gui.LevelingScreen;
import io.github.thatone0502.leveling.network.payload.PanelDataS2CPayload;
import net.minecraft.client.Minecraft;

/**
 * 客户端侧缓存：最近一次面板快照 + 未使用点数 + 打开请求标记。
 */
public final class LevelingClientState {
    public static final LevelingClientState INSTANCE = new LevelingClientState();

    private PanelDataS2CPayload data;
    private int unspentPoints = 0;
    private boolean panelRequested;

    private LevelingClientState() {
    }

    public void onPanelData(PanelDataS2CPayload payload, Minecraft client) {
        this.data = payload;
        this.unspentPoints = payload.unspentPoints();
        if (client.screen instanceof LevelingScreen screen) {
            screen.updateData(payload);
        } else if (panelRequested) {
            panelRequested = false;
            client.setScreen(new LevelingScreen(payload));
        }
    }

    public void onReminder(int unspent) {
        this.unspentPoints = unspent;
    }

    public void requestPanel() {
        this.panelRequested = true;
    }

    public int unspentPoints() {
        return unspentPoints;
    }
}
