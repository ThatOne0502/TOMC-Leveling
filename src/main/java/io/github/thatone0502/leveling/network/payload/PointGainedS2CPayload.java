package io.github.thatone0502.leveling.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** 事件驱动：玩家刚获得属性点（区别于周期状态核查），客户端据此播放音效 + 立即提醒。 */
public record PointGainedS2CPayload(int unspentPoints) implements CustomPacketPayload {
    public static final Type<PointGainedS2CPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("leveling", "point_gained"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PointGainedS2CPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PointGainedS2CPayload::unspentPoints,
            PointGainedS2CPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
