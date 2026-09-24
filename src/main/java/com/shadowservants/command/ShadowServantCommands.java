package com.shadowservants.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.shadowservants.data.ServantData;
import com.shadowservants.gameplay.ServantRules;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.UUID;

public final class ShadowServantCommands {
    private ShadowServantCommands() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shadowservants")
            .then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
            .then(Commands.literal("summon")
                .then(Commands.argument("entity", StringArgumentType.word())
                    .executes(ctx -> summon(ctx.getSource(), StringArgumentType.getString(ctx, "entity")))))
            .then(Commands.literal("dismiss_all").executes(ctx -> dismissAll(ctx.getSource())))
            .then(Commands.literal("dismiss")
                .then(Commands.argument("entity", StringArgumentType.word())
                    .executes(ctx -> dismissType(ctx.getSource(), StringArgumentType.getString(ctx, "entity")))))
        );
    }

    private static int status(CommandSourceStack source) throws Exception {
        ServerPlayer player = source.getPlayerOrException();
        ServantData.PlayerData data = ServantData.get(player.serverLevel()).player(player.getUUID());
        player.sendSystemMessage(Component.literal("§5§lShadow Servants §8— §fprogresso"));
        if (data.defeatsView().isEmpty()) {
            player.sendSystemMessage(Component.literal("§7Nenhuma criatura derrotada ainda."));
            return 1;
        }
        data.defeatsView().forEach((id, count) -> player.sendSystemMessage(Component.literal(
                "§7" + id + " §f" + count + " derrotas §8| §5" + data.unlocked(id) + " desbloqueados §8| §d" + data.activeCount(id) + " ativos")));
        return 1;
    }

    private static int summon(CommandSourceStack source, String raw) throws Exception {
        ServerPlayer player = source.getPlayerOrException();
        ResourceLocation id = ResourceLocation.parse(raw.contains(":") ? raw : "minecraft:" + raw);
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
        if (type == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
            source.sendFailure(Component.literal("Entidade inválida: " + id));
            return 0;
        }
        if (!ServantRules.canBecomeServant(id)) {
            source.sendFailure(Component.literal("Essa entidade não pode ser um servo."));
            return 0;
        }
        if (!(type.create(player.serverLevel()) instanceof Mob mob)) {
            source.sendFailure(Component.literal("Essa entidade não pode ser um servo móvel."));
            return 0;
        }

        ServantData data = ServantData.get(player.serverLevel());
        ServantData.PlayerData pd = data.player(player.getUUID());
        if (!pd.canSummon(id.toString())) {
            source.sendFailure(Component.literal("Você não possui um servo disponível desse tipo. Derrote 10 criaturas para desbloquear 1."));
            return 0;
        }

        mob.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0);
        mob.getPersistentData().putBoolean("ShadowServant", true);
        mob.getPersistentData().putUUID("ShadowOwner", player.getUUID());
        mob.setPersistenceRequired();
        mob.setCustomName(Component.literal("§5§lServo das Sombras"));
        mob.setGlowingTag(true);
        player.serverLevel().addFreshEntity(mob);
        pd.registerSummon(mob.getUUID(), id.toString());
        data.setDirty();
        source.sendSuccess(() -> Component.literal("§5Servo invocado: §f" + id), true);
        return 1;
    }

    private static int dismissAll(CommandSourceStack source) throws Exception {
        ServerPlayer player = source.getPlayerOrException();
        ServantData data = ServantData.get(player.serverLevel());
        ServantData.PlayerData pd = data.player(player.getUUID());
        int count = 0;
        for (UUID uuid : new ArrayList<>(pd.activeView().keySet())) {
            var entity = player.serverLevel().getEntity(uuid);
            if (entity != null) entity.discard();
            if (pd.registerDismiss(uuid)) count++;
        }
        data.setDirty();
        final int result = count;
        source.sendSuccess(() -> Component.literal("§7" + result + " servo(s) guardado(s)."), true);
        return result;
    }

    private static int dismissType(CommandSourceStack source, String raw) throws Exception {
        ServerPlayer player = source.getPlayerOrException();
        String id = ResourceLocation.parse(raw.contains(":") ? raw : "minecraft:" + raw).toString();
        ServantData data = ServantData.get(player.serverLevel());
        ServantData.PlayerData pd = data.player(player.getUUID());
        int count = 0;
        for (var entry : new ArrayList<>(pd.activeView().entrySet())) {
            if (!entry.getValue().equals(id)) continue;
            var entity = player.serverLevel().getEntity(entry.getKey());
            if (entity != null) entity.discard();
            if (pd.registerDismiss(entry.getKey())) count++;
        }
        data.setDirty();
        final int result = count;
        source.sendSuccess(() -> Component.literal("§7" + result + " servo(s) de " + id + " guardado(s)."), true);
        return result;
    }
}
