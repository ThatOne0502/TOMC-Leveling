package com.tomc.leveling.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

/** 服务端推送给客户端的完整面板快照。 */
public record PanelDataS2CPayload(
        int highestLevel,
        int unspentPoints,
        List<AttributeDefView> definitions,
        List<AllocationView> allocations
) implements CustomPacketPayload {
    public static final Type<PanelDataS2CPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("leveling", "panel_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PanelDataS2CPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PanelDataS2CPayload::highestLevel,
            ByteBufCodecs.VAR_INT, PanelDataS2CPayload::unspentPoints,
            AttributeDefView.STREAM_CODEC.apply(ByteBufCodecs.list()), PanelDataS2CPayload::definitions,
            AllocationView.STREAM_CODEC.apply(ByteBufCodecs.list()), PanelDataS2CPayload::allocations,
            PanelDataS2CPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
