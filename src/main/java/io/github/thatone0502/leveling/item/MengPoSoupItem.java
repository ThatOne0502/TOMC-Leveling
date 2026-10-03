package io.github.thatone0502.leveling.item;

import io.github.thatone0502.leveling.core.LevelingService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * 孟婆汤：满饥饿可食，食用时长约为普通食物的 3 倍（约 96 tick），
 * 使用后返还一个碗并触发属性点重置。
 */
public class MengPoSoupItem extends Item {
    public MengPoSoupItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 96;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            LevelingService.INSTANCE.applyMengPoSoup(player);
        }
        return new ItemStack(Items.BOWL);
    }
}
