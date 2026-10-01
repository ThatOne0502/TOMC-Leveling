package com.tomc.leveling.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** 客户端发送的加点请求。 */
public record SpendPointC2SPayload(Identifier attributeId) implements CustomPacketPayload {
    public static final Type<SpendPointC2SPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("leveling", "spend_point"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpendPointC2SPayload> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, SpendPointC2SPayload::attributeId,
            SpendPointC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
