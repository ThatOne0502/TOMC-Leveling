package com.tomc.leveling.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** 服务端对加点请求的应答：成功与否 + 失败译名键。 */
public record SpendResultS2CPayload(boolean success, String errorKey) implements CustomPacketPayload {
    public static final Type<SpendResultS2CPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("leveling", "spend_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpendResultS2CPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SpendResultS2CPayload::success,
            ByteBufCodecs.STRING_UTF8, SpendResultS2CPayload::errorKey,
            SpendResultS2CPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
