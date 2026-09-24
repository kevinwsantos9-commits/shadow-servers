package com.shadowservants.gameplay;

import com.shadowservants.data.ServantData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.UUID;

public final class ServantEvents {
    private ServantEvents() {}

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (dead.level().isClientSide()) return;

        if (dead instanceof Mob mob && mob.getPersistentData().getBoolean("ShadowServant")) {
            UUID ownerId = mob.getPersistentData().getUUID("ShadowOwner");
            if (dead.level().getServer() != null) {
                ServerPlayer owner = dead.level().getServer().getPlayerList().getPlayer(ownerId);
                if (owner != null) {
                    ServantData data = ServantData.get(owner.serverLevel());
                    String consumed = data.player(ownerId).registerDeath(dead.getUUID());
                    data.setDirty();
                    if (consumed != null) {
                        owner.sendSystemMessage(Component.literal("§8Seu servo das sombras foi destruído: §f" + consumed));
                    }
                }
            }
            return;
        }

        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(dead.getType());
        if (!ServantRules.canBecomeServant(id)) return;

        ServantData data = ServantData.get(player.serverLevel());
        int newlyUnlocked = data.player(player.getUUID()).registerDefeat(id.toString());
        data.setDirty();
        if (newlyUnlocked > 0) {
            player.sendSystemMessage(Component.literal("§5§lSHADOW SERVANTS §r§7Você desbloqueou §f1 servo§7 de §d" + id + "§7!"));
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 10 != 0) return;
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            ServantData.PlayerData data = ServantData.get(player.serverLevel()).player(player.getUUID());
            for (var entry : data.activeView().entrySet()) {
                var entity = player.serverLevel().getEntity(entry.getKey());
                if (!(entity instanceof Mob mob) || !mob.isAlive()) continue;

                mob.getPersistentData().putUUID("ShadowOwner", player.getUUID());
                mob.setCustomName(Component.literal("§5§lServo das Sombras"));
                mob.setCustomNameVisible(false);
                mob.setGlowingTag(true);

                if (mob.distanceToSqr(player) > 25.0D) {
                    mob.getNavigation().moveTo(player, 1.10D);
                }

                if (player.getLastHurtByMob() != null && player.getLastHurtByMob().isAlive()) {
                    mob.setTarget(player.getLastHurtByMob());
                } else if (player.getLastHurtMob() != null && player.getLastHurtMob().isAlive()) {
                    mob.setTarget(player.getLastHurtMob());
                }
            }
        }
    }
}
