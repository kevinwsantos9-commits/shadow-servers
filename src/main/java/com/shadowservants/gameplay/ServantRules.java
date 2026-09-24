package com.shadowservants.gameplay;

import net.minecraft.resources.ResourceLocation;

public final class ServantRules {
    private ServantRules() {}

    public static boolean canBecomeServant(ResourceLocation entityId) {
        if (entityId == null) return false;
        String path = entityId.getPath();
        return !path.equals("player") && !path.contains("item") && !path.contains("projectile") && !path.contains("armor_stand");
    }

    public static int strengthTier(ResourceLocation entityId) {
        String path = entityId.getPath();
        if (path.contains("ender") || path.contains("warden")) return 5;
        if (path.contains("skeleton") || path.contains("blaze") || path.contains("spider")) return 4;
        if (path.contains("zombie") || path.contains("wolf")) return 3;
        if (path.contains("cow") || path.contains("pig") || path.contains("sheep")) return 2;
        return 1;
    }
}
