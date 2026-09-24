package com.shadowservants;

import com.shadowservants.command.ShadowServantCommands;
import com.shadowservants.gameplay.ServantEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(ShadowServants.MOD_ID)
public final class ShadowServants {
    public static final String MOD_ID = "shadowservants";

    public ShadowServants(IEventBus modBus) {
        NeoForge.EVENT_BUS.register(ServantEvents.class);
        NeoForge.EVENT_BUS.addListener(ShadowServantCommands::register);
    }
}
