package io.github.thatone0502.leveling.network;

import io.github.thatone0502.leveling.core.AttributeDefinition;
import io.github.thatone0502.leveling.core.LevelingRegistry;
import io.github.thatone0502.leveling.core.LevelingService;
import io.github.thatone0502.leveling.data.PlayerLevelingData;
import io.github.thatone0502.leveling.network.payload.AllocationView;
import io.github.thatone0502.leveling.network.payload.AttributeDefView;
import io.github.thatone0502.leveling.network.payload.CheckPointsC2SPayload;
import io.github.thatone0502.leveling.network.payload.PanelDataS2CPayload;
import io.github.thatone0502.leveling.network.payload.PointGainedS2CPayload;
import io.github.thatone0502.leveling.network.payload.ReminderS2CPayload;
import io.github.thatone0502.leveling.network.payload.RequestPanelC2SPayload;
import io.github.thatone0502.leveling.network.payload.SpendPointC2SPayload;
import io.github.thatone0502.leveling.network.payload.SpendResultS2CPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * 网络通道。客户端接收端在 {@link io.github.thatone0502.leveling.LevelingClient}（客户端专属类）。
 */
public final class LevelingNetwork {
    private LevelingNetwork() {
    }

    /** 双端都需要：注册 payload 类型。 */
    public static void registerCommon() {
        PayloadTypeRegistry.playC2S().register(RequestPanelC2SPayload.ID, RequestPanelC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SpendPointC2SPayload.ID, SpendPointC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CheckPointsC2SPayload.ID, CheckPointsC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PanelDataS2CPayload.ID, PanelDataS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SpendResultS2CPayload.ID, SpendResultS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ReminderS2CPayload.ID, ReminderS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PointGainedS2CPayload.ID, PointGainedS2CPayload.CODEC);
    }

    /** 服务端接收端。 */
    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(RequestPanelC2SPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    ServerPlayer player = context.player();
                    if (player != null) {
                        sendPanel(player);
                    }
                }));

        ServerPlayNetworking.registerGlobalReceiver(SpendPointC2SPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    ServerPlayer player = context.player();
                    if (player != null) {
                        LevelingService.INSTANCE.spendPoint(player, payload.attributeId());
                    }
                }));

        ServerPlayNetworking.registerGlobalReceiver(CheckPointsC2SPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    ServerPlayer player = context.player();
                    if (player != null) {
                        sendPointsStatus(player);
                    }
                }));
    }

    public static void sendPanel(ServerPlayer player) {
        ServerPlayNetworking.send(player, buildPanel(player));
    }

    public static void sendSpendResult(ServerPlayer player, boolean success, String errorKey) {
        ServerPlayNetworking.send(player, new SpendResultS2CPayload(success, errorKey));
    }

    public static void sendReminder(ServerPlayer player, int unspentPoints) {
        ServerPlayNetworking.send(player, new ReminderS2CPayload(unspentPoints));
    }

    /** 事件驱动：玩家刚获得属性点（区别于周期状态核查），客户端据此播放音效 + 立即提醒。 */
    public static void sendPointGained(ServerPlayer player, int unspentPoints) {
        ServerPlayNetworking.send(player, new PointGainedS2CPayload(unspentPoints));
    }

    /** 客户端周期核查的应答：回传当前未使用点数。 */
    public static void sendPointsStatus(ServerPlayer player) {
        PlayerLevelingData data = LevelingRegistry.INSTANCE.store().load(player.getUUID());
        ServerPlayNetworking.send(player, new ReminderS2CPayload(data.unspentPoints));
    }

    private static PanelDataS2CPayload buildPanel(ServerPlayer player) {
        PlayerLevelingData data = LevelingRegistry.INSTANCE.store().load(player.getUUID());
        List<AttributeDefView> definitions = new ArrayList<>();
        List<AllocationView> allocations = new ArrayList<>();
        for (AttributeDefinition definition : LevelingRegistry.INSTANCE.definitions().sorted()) {
            definitions.add(new AttributeDefView(
                    definition.attribute(),
                    definition.effectiveDisplayKey(),
                    definition.effectiveDescriptionKey(),
                    definition.cost(),
                    definition.order()
            ));
            PlayerLevelingData.Allocation allocation = data.allocations.get(definition.attribute().toString());
            if (allocation != null) {
                allocations.add(new AllocationView(definition.attribute(), allocation.levels, allocation.spent));
            }
        }
        return new PanelDataS2CPayload(data.highestLevel, data.unspentPoints, definitions, allocations);
    }
}
