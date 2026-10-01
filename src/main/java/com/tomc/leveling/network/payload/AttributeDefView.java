package com.tomc.leveling.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/** 面板展示用的属性定义视图（含排序序号与显示/描述译名键）。 */
public record AttributeDefView(Identifier id, String displayKey, String descriptionKey, int cost, Integer order) {
    public static final StreamCodec<RegistryFriendlyByteBuf, AttributeDefView> STREAM_CODEC = StreamCodec.of(
            (buf, view) -> {
                Identifier.STREAM_CODEC.encode(buf, view.id());
                ByteBufCodecs.STRING_UTF8.encode(buf, view.displayKey());
                ByteBufCodecs.STRING_UTF8.encode(buf, view.descriptionKey());
                ByteBufCodecs.VAR_INT.encode(buf, view.cost());
                buf.writeBoolean(view.order() != null);
                if (view.order() != null) {
                    buf.writeVarInt(view.order());
                }
            },
            buf -> new AttributeDefView(
                    Identifier.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    buf.readBoolean() ? buf.readVarInt() : null
            )
    );
}
