package io.github.thatone0502.leveling;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.thatone0502.leveling.client.LevelingClientState;
import io.github.thatone0502.leveling.client.gui.LevelingScreen;
import io.github.thatone0502.leveling.config.ClientConfig;
import io.github.thatone0502.leveling.item.ModSounds;
import io.github.thatone0502.leveling.network.payload.CheckPointsC2SPayload;
import io.github.thatone0502.leveling.network.payload.PanelDataS2CPayload;
import io.github.thatone0502.leveling.network.payload.PointGainedS2CPayload;
import io.github.thatone0502.leveling.network.payload.ReminderS2CPayload;
import io.github.thatone0502.leveling.network.payload.RequestPanelC2SPayload;
import io.github.thatone0502.leveling.network.payload.SpendResultS2CPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * 客户端入口：按键、网络接收、提醒显示、周期核查、加点结果声音反馈。
 */
public class LevelingClient implements ClientModInitializer {
    private static KeyMapping openPanelKey;
    private static int checkTick = 0;

    @Override
    public void onInitializeClient() {
        ClientConfig.INSTANCE.load();

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Leveling.MOD_ID, "main"));
        openPanelKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.leveling.open_panel",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_I,
                category
        ));

        ClientPlayNetworking.registerGlobalReceiver(PanelDataS2CPayload.ID, (payload, context) ->
                context.client().execute(() -> LevelingClientState.INSTANCE.onPanelData(payload, context.client())));

        ClientPlayNetworking.registerGlobalReceiver(ReminderS2CPayload.ID, (payload, context) ->
                context.client().execute(() -> onPointsStatus(context.client(), payload.unspentPoints())));

        ClientPlayNetworking.registerGlobalReceiver(PointGainedS2CPayload.ID, (payload, context) ->
                context.client().execute(() -> onPointGained(context.client(), payload.unspentPoints())));

        ClientPlayNetworking.registerGlobalReceiver(SpendResultS2CPayload.ID, (payload, context) ->
                context.client().execute(() -> onSpendResult(context.client(), payload)));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openPanelKey.consumeClick()) {
                if (client.screen instanceof LevelingScreen) {
                    client.setScreen(null);
                } else {
                    LevelingClientState.INSTANCE.requestPanel();
                    ClientPlayNetworking.send(new RequestPanelC2SPayload());
                }
            }
            tickCheck(client);
        });
    }

    /** 供 LevelingScreen.keyPressed 复用的按键判断入口。 */
    public static KeyMapping getOpenPanelKey() {
        return openPanelKey;
    }

    private static void onPointsStatus(Minecraft client, int unspentPoints) {
        LevelingClientState.INSTANCE.onReminder(unspentPoints);
        showReminder(client, unspentPoints);
    }

    /** 刚获得属性点：播放音效（受客户端开关控制）+ 立即提醒。 */
    private static void onPointGained(Minecraft client, int unspentPoints) {
        LevelingClientState.INSTANCE.onReminder(unspentPoints);
        if (ClientConfig.INSTANCE.levelingSoundEnabled && client.player != null) {
            client.player.playSound(ModSounds.LEVELING, 1.0F, 1.0F);
        }
        showReminder(client, unspentPoints);
    }

    private static void showReminder(Minecraft client, int unspentPoints) {
        if (!ClientConfig.INSTANCE.reminderEnabled || client.player == null || unspentPoints <= 0) {
            return;
        }
        Component message = Component.translatable("message.leveling.reminder", unspentPoints, openPanelKey.getTranslatedKeyMessage());
        client.player.displayClientMessage(message, true);
    }

    private static void onSpendResult(Minecraft client, SpendResultS2CPayload payload) {
        if (payload.success()) {
            playUiSound(client, SoundEvents.NOTE_BLOCK_PLING);
        } else {
            playUiSound(client, SoundEvents.NOTE_BLOCK_BASS);
            if (client.player != null && payload.errorKey() != null && !payload.errorKey().isEmpty()) {
                client.player.displayClientMessage(Component.translatable(payload.errorKey()), true);
            }
        }
    }

    /** 周期核查：按客户端配置周期向服务端申请数据，收到应答后由 onPointsStatus 决定是否提醒。 */
    private static void tickCheck(Minecraft client) {
        ClientConfig config = ClientConfig.INSTANCE;
        if (!config.reminderEnabled || client.player == null) {
            return;
        }
        int intervalSeconds = config.reminderIntervalSeconds;
        checkTick++;
        if (intervalSeconds == 0) {
            // 常态显示：用缓存值每 tick 显示；每 1 秒轮询一次刷新缓存
            showReminder(client, LevelingClientState.INSTANCE.unspentPoints());
            if (checkTick % 20 == 0) {
                ClientPlayNetworking.send(new CheckPointsC2SPayload());
            }
        } else if (checkTick % (intervalSeconds * 20) == 0) {
            ClientPlayNetworking.send(new CheckPointsC2SPayload());
        }
    }

    private static void playUiSound(Minecraft client, Holder<SoundEvent> sound) {
        client.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F));
    }
}
