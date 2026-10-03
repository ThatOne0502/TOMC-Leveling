package io.github.thatone0502.leveling.mixin;

import io.github.thatone0502.leveling.core.LevelingService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 升级检测：giveExperienceLevels 正向增加经验等级时触发分发检查。
 */
@Mixin(Player.class)
public abstract class PlayerExperienceMixin {
    @Inject(method = "giveExperienceLevels", at = @At("RETURN"))
    private void leveling$onGiveExperienceLevels(int levels, CallbackInfo ci) {
        if (levels > 0) {
            Player self = (Player) (Object) this;
            if (self instanceof ServerPlayer serverPlayer) {
                LevelingService.INSTANCE.checkDistribution(serverPlayer);
            }
        }
    }
}
