package com.tomc.leveling.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/** 面板展示用的单项属性分配视图。 */
public record AllocationView(Identifier id, int levels, int spent) {
    public static final StreamCodec<RegistryFriendlyByteBuf, AllocationView> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, AllocationView::id,
            ByteBufCodecs.VAR_INT, AllocationView::levels,
            ByteBufCodecs.VAR_INT, AllocationView::spent,
            AllocationView::new
    );
}
