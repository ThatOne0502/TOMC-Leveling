package com.tomc.leveling.core;

import com.tomc.leveling.config.ServerConfig;
import com.tomc.leveling.data.PlayerLevelingData;
import com.tomc.leveling.item.ModSounds;
import com.tomc.leveling.modifier.ModifierApplier;
import com.tomc.leveling.network.LevelingNetwork;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 服务端权威核心逻辑：属性点分发、加点、重置、修饰符应用与同步。
 */
public final class LevelingService {
    public static final LevelingService INSTANCE = new LevelingService();

    private LevelingService() {
    }

    /** 登录 / 重生统一入口。 */
    public void onPlayerJoin(ServerPlayer player) {
        PlayerLevelingData data = LevelingRegistry.INSTANCE.store().load(player.getUUID());
        boolean changed = refundOrphans(data);
        if (changed) {
            LevelingRegistry.INSTANCE.store().save(player.getUUID(), data);
        }
        reapplyAll(player);
        checkDistribution(player);
        sendLoadErrors(player);
        LevelingNetwork.sendPanel(player);
    }

    /** 属性点分发检查：登录、重生、升级共用。 */
    public void checkDistribution(ServerPlayer player) {
        PlayerLevelingData data = LevelingRegistry.INSTANCE.store().load(player.getUUID());
        int level = player.experienceLevel;
        if (level > data.highestLevel) {
            int delta = level - data.highestLevel;
            data.unspentPoints += delta;
            data.highestLevel = level;
            LevelingRegistry.INSTANCE.store().save(player.getUUID(), data);
            player.playSound(ModSounds.LEVELING, 1.0F, 1.0F);
            LevelingNetwork.sendReminder(player, data.unspentPoints); // 事件驱动：立即提醒
            LevelingNetwork.sendPanel(player);
        }
    }

    /** 加点请求。成功返回 null，失败返回错误译名键；同时向客户端发送应答。 */
    public String spendPoint(ServerPlayer player, Identifier attributeId) {
        AttributeDefinition definition = LevelingRegistry.INSTANCE.definitions().get(attributeId);
        if (definition == null) {
            LevelingNetwork.sendSpendResult(player, false, "command.leveling.invalid_attribute");
            return "command.leveling.invalid_attribute";
        }
        PlayerLevelingData data = LevelingRegistry.INSTANCE.store().load(player.getUUID());
        if (data.unspentPoints < definition.cost()) {
            LevelingNetwork.sendSpendResult(player, false, "command.leveling.not_enough_points");
            return "command.leveling.not_enough_points";
        }
        data.unspentPoints -= definition.cost();
        PlayerLevelingData.Allocation allocation = data.getAllocation(attributeId);
        allocation.levels += 1;
        allocation.spent += definition.cost();
        LevelingRegistry.INSTANCE.store().save(player.getUUID(), data);
        ModifierApplier.apply(player, attributeId, definition, allocation.levels);
        LevelingNetwork.sendPanel(player);
        LevelingNetwork.sendSpendResult(player, true, "");
        return null;
    }

    /** 孟婆汤效果。 */
    public void applyMengPoSoup(ServerPlayer player) {
        ServerConfig config = ServerConfig.INSTANCE;
        resetAllocations(player, config.clearRecipesOnUse, config.clearAdvancementsOnUse);
    }

    /** 重置：返还 spent、清空 allocations、移除加成；不影响 highestLevel。 */
    public void resetAllocations(ServerPlayer player, boolean clearRecipes, boolean clearAdvancements) {
        PlayerLevelingData data = LevelingRegistry.INSTANCE.store().load(player.getUUID());
        for (PlayerLevelingData.Allocation allocation : data.allocations.values()) {
            data.unspentPoints += allocation.spent;
        }
        data.allocations.clear();
        LevelingRegistry.INSTANCE.store().save(player.getUUID(), data);
        ModifierApplier.removeAll(player);
        if (clearRecipes) {
            clearUnlockedRecipes(player);
        }
        if (clearAdvancements) {
            clearAdvancements(player);
        }
        LevelingNetwork.sendPanel(player);
    }

    /** 重新应用所有仍存在的加成（登录、重生、/reload）。 */
    public void reapplyAll(ServerPlayer player) {
        PlayerLevelingData data = LevelingRegistry.INSTANCE.store().load(player.getUUID());
        for (AttributeDefinition definition : LevelingRegistry.INSTANCE.definitions().all()) {
            ModifierApplier.remove(player, definition.attribute());
        }
        for (Map.Entry<String, PlayerLevelingData.Allocation> entry : data.allocations.entrySet()) {
            Identifier id = Identifier.tryParse(entry.getKey());
            AttributeDefinition definition = id == null ? null : LevelingRegistry.INSTANCE.definitions().get(id);
            if (definition != null && entry.getValue().levels > 0) {
                ModifierApplier.apply(player, id, definition, entry.getValue().levels);
            }
        }
    }

    /** 当前数据包中不存在的可提升属性：返还 spent、清空该 allocation。 */
    private boolean refundOrphans(PlayerLevelingData data) {
        boolean changed = false;
        Iterator<Map.Entry<String, PlayerLevelingData.Allocation>> iterator = data.allocations.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, PlayerLevelingData.Allocation> entry = iterator.next();
            Identifier id = Identifier.tryParse(entry.getKey());
            if (id == null || LevelingRegistry.INSTANCE.definitions().get(id) == null) {
                data.unspentPoints += entry.getValue().spent;
                iterator.remove();
                changed = true;
            }
        }
        return changed;
    }

    private void sendLoadErrors(ServerPlayer player) {
        List<String> errors = LevelingRegistry.INSTANCE.definitions().getLoadErrors();
        if (errors.isEmpty()) {
            return;
        }
        player.sendSystemMessage(Component.translatable("message.leveling.load_error_header"));
        for (String error : errors) {
            player.sendSystemMessage(Component.literal(error));
        }
    }

    private void clearUnlockedRecipes(ServerPlayer player) {
        ServerRecipeBook book = player.getRecipeBook();
        book.removeRecipes(player.level().getServer().getRecipeManager().getRecipes(), player);
    }

    private void clearAdvancements(ServerPlayer player) {
        PlayerAdvancements advancements = player.getAdvancements();
        for (AdvancementHolder holder : player.level().getServer().getAdvancements().getAllAdvancements()) {
            AdvancementProgress progress = advancements.getOrStartProgress(holder);
            for (String criterion : progress.getCompletedCriteria()) {
                advancements.revoke(holder, criterion);
            }
        }
    }
}
