package com.tomc.leveling.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.tomc.leveling.core.AttributeDefinition;
import com.tomc.leveling.core.LevelingRegistry;
import com.tomc.leveling.core.LevelingService;
import com.tomc.leveling.data.PlayerLevelingData;
import com.tomc.leveling.network.LevelingNetwork;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Locale;
import java.util.Map;

/**
 * /leveling point|cap 命令树，整体要求 OP（gamemaster，等级 2）权限。
 */
public final class LevelingCommand {
    private LevelingCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("leveling")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(point())
                        .then(cap())));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> point() {
        return Commands.literal("point")
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.literal("set")
                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                        .executes(LevelingCommand::pointSet)))
                        .then(Commands.literal("give")
                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                        .executes(LevelingCommand::pointGive)))
                        .then(Commands.literal("get")
                                .executes(LevelingCommand::pointGet))
                        .then(Commands.literal("clear")
                                .executes(ctx -> pointClear(ctx, -1))
                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                        .executes(ctx -> pointClear(ctx, IntegerArgumentType.getInteger(ctx, "value")))))
                        .then(Commands.literal("reset")
                                .executes(LevelingCommand::pointReset))
                        .then(Commands.literal("getspend")
                                .executes(LevelingCommand::getSpendAll)
                                .then(Commands.argument("attribute", StringArgumentType.string())
                                        .executes(LevelingCommand::getSpendOne))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> cap() {
        return Commands.literal("cap")
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.literal("set")
                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                        .executes(LevelingCommand::capSet)))
                        .then(Commands.literal("give")
                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                        .executes(LevelingCommand::capGive)))
                        .then(Commands.literal("get")
                                .executes(LevelingCommand::capGet))
                        .then(Commands.literal("clear")
                                .executes(ctx -> capClear(ctx, -1))
                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                        .executes(ctx -> capClear(ctx, IntegerArgumentType.getInteger(ctx, "value")))))
                        .then(Commands.literal("reset")
                                .executes(LevelingCommand::capReset)));
    }

    private static int pointSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        int value = IntegerArgumentType.getInteger(ctx, "value");
        PlayerLevelingData data = LevelingRegistry.INSTANCE.store().load(target.getUUID());
        data.unspentPoints = Math.max(0, value);
        saveAndSync(target, data);
        feedback(ctx, "command.leveling.point.set", target.getDisplayName(), data.unspentPoints);
        return 1;
    }

    private static int pointGive(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        int value = IntegerArgumentType.getInteger(ctx, "value");
        PlayerLevelingData data = LevelingRegistry.INSTANCE.store().load(target.getUUID());
        data.unspentPoints = Math.max(0, data.unspentPoints + value);
        saveAndSync(target, data);
        feedback(ctx, "command.leveling.point.set", target.getDisplayName(), data.unspentPoints);
        return 1;
    }

    private static int pointGet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        PlayerLevelingData data = LevelingRegistry.INSTANCE.store().load(target.getUUID());
        feedback(ctx, "command.leveling.point.get", target.getDisplayName(), data.unspentPoints);
        return 1;
    }

    private static int pointClear(CommandContext<CommandSourceStack> ctx, int amount) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        PlayerLevelingData data = LevelingRegistry.INSTANCE.store().load(target.getUUID());
        if (amount < 0) {
            data.unspentPoints = 0;
        } else {
            data.unspentPoints = Math.max(0, data.unspentPoints - amount);
        }
        saveAndSync(target, data);
        feedback(ctx, "command.leveling.point.set", target.getDisplayName(), data.unspentPoints);
        return 1;
    }

    private static int pointReset(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        LevelingService.INSTANCE.resetAllocations(target, false, false);
        feedback(ctx, "command.leveling.point.reset", target.getDisplayName());
        return 1;
    }

    private static int getSpendAll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        PlayerLevelingData data = LevelingRegistry.INSTANCE.store().load(target.getUUID());
        if (data.allocations.isEmpty()) {
            feedback(ctx, "command.leveling.getspend.empty", target.getDisplayName());
        } else {
            for (Map.Entry<String, PlayerLevelingData.Allocation> entry : data.allocations.entrySet()) {
                sendSpendLine(ctx, entry.getKey(), entry.getValue());
            }
        }
        return 1;
    }

    private static int getSpendOne(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        String attribute = StringArgumentType.getString(ctx, "attribute");
        PlayerLevelingData data = LevelingRegistry.INSTANCE.store().load(target.getUUID());
        PlayerLevelingData.Allocation allocation = data.allocations.get(attribute);
        if (allocation == null) {
            feedback(ctx, "command.leveling.getspend.none", target.getDisplayName(), attribute);
        } else {
            sendSpendLine(ctx, attribute, allocation);
        }
        return 1;
    }

    private static void sendSpendLine(CommandContext<CommandSourceStack> ctx, String attribute, PlayerLevelingData.Allocation allocation) {
        Identifier id = Identifier.tryParse(attribute);
        AttributeDefinition definition = id == null ? null : LevelingRegistry.INSTANCE.definitions().get(id);
        StringBuilder builder = new StringBuilder();
        builder.append(attribute).append(": levels=").append(allocation.levels).append(", spent=").append(allocation.spent);
        if (definition != null) {
            double bonus = definition.totalValue(allocation.levels);
            if (definition.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                builder.append(", bonus=+").append(String.format(Locale.ROOT, "%.1f%%", bonus * 100.0));
            } else {
                builder.append(", bonus=+").append(String.format(Locale.ROOT, "%.1f", bonus));
            }
        } else {
            builder.append(", ").append(Component.translatable("command.leveling.getspend.invalid").getString());
        }
        String line = builder.toString();
        ctx.getSource().sendSuccess(() -> Component.literal(line), false);
    }

    private static int capSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        int value = IntegerArgumentType.getInteger(ctx, "value");
        PlayerLevelingData data = LevelingRegistry.INSTANCE.store().load(target.getUUID());
        data.highestLevel = Math.max(0, value);
        saveAndSync(target, data);
        feedback(ctx, "command.leveling.cap.set", target.getDisplayName(), data.highestLevel);
        return 1;
    }

    private static int capGive(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        int value = IntegerArgumentType.getInteger(ctx, "value");
        PlayerLevelingData data = LevelingRegistry.INSTANCE.store().load(target.getUUID());
        data.highestLevel = Math.max(0, data.highestLevel + value);
        saveAndSync(target, data);
        feedback(ctx, "command.leveling.cap.set", target.getDisplayName(), data.highestLevel);
        return 1;
    }

    private static int capGet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        PlayerLevelingData data = LevelingRegistry.INSTANCE.store().load(target.getUUID());
        feedback(ctx, "command.leveling.cap.get", target.getDisplayName(), data.highestLevel);
        return 1;
    }

    private static int capClear(CommandContext<CommandSourceStack> ctx, int amount) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        PlayerLevelingData data = LevelingRegistry.INSTANCE.store().load(target.getUUID());
        if (amount < 0) {
            data.highestLevel = 0;
        } else {
            data.highestLevel = Math.max(0, data.highestLevel - amount);
        }
        saveAndSync(target, data);
        feedback(ctx, "command.leveling.cap.set", target.getDisplayName(), data.highestLevel);
        return 1;
    }

    private static int capReset(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        PlayerLevelingData data = LevelingRegistry.INSTANCE.store().load(target.getUUID());
        data.highestLevel = 0;
        saveAndSync(target, data);
        feedback(ctx, "command.leveling.cap.set", target.getDisplayName(), data.highestLevel);
        return 1;
    }

    private static void saveAndSync(ServerPlayer target, PlayerLevelingData data) {
        LevelingRegistry.INSTANCE.store().save(target.getUUID(), data);
        LevelingNetwork.sendPanel(target);
    }

    private static void feedback(CommandContext<CommandSourceStack> ctx, String key, Object... args) {
        ctx.getSource().sendSuccess(() -> Component.translatable(key, args), false);
    }
}
