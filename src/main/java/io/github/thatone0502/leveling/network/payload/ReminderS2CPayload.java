package io.github.thatone0502.leveling.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** 服务端触发的提醒（事件驱动 + 周期兜底），携带权威的未使用点数。 */
public record ReminderS2CPayload(int unspentPoints) implements CustomPacketPayload {
    public static final Type<ReminderS2CPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("leveling", "reminder"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReminderS2CPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ReminderS2CPayload::unspentPoints,
            ReminderS2CPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
