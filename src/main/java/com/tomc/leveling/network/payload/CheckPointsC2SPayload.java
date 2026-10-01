package com.tomc.leveling.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** 客户端按自身周期向服务端申请核查当前未使用点数。 */
public record CheckPointsC2SPayload() implements CustomPacketPayload {
    public static final Type<CheckPointsC2SPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("leveling", "check_points"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CheckPointsC2SPayload> CODEC = StreamCodec.of(
            (buf, value) -> {
            },
            buf -> new CheckPointsC2SPayload()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
