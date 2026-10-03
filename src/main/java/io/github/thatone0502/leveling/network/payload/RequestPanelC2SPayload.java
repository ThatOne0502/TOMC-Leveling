package io.github.thatone0502.leveling.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** 客户端请求打开面板（按 I 键触发）。 */
public record RequestPanelC2SPayload() implements CustomPacketPayload {
    public static final Type<RequestPanelC2SPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("leveling", "request_panel"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestPanelC2SPayload> CODEC = StreamCodec.of(
            (buf, value) -> {
            },
            buf -> new RequestPanelC2SPayload()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
