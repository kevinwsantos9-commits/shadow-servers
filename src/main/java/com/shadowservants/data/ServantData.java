package com.shadowservants.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.server.level.ServerLevel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Persistent per-world state. Progress is stored per player UUID. */
public final class ServantData extends SavedData {
    private static final String DATA_NAME = "shadowservants";
    private final Map<UUID, PlayerData> players = new HashMap<>();

    public static ServantData create() {
        return new ServantData();
    }

    public static ServantData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        ServantData data = create();
        ListTag list = tag.getList("Players", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag playerTag = list.getCompound(i);
            UUID uuid = playerTag.getUUID("UUID");
            data.players.put(uuid, PlayerData.load(playerTag));
        }
        return data;
    }

    public static ServantData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(
                new Factory<>(ServantData::create, ServantData::load), DATA_NAME);
    }

    public PlayerData player(UUID uuid) {
        return players.computeIfAbsent(uuid, ignored -> new PlayerData());
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, PlayerData> entry : players.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("UUID", entry.getKey());
            entry.getValue().save(playerTag);
            list.add(playerTag);
        }
        tag.put("Players", list);
        return tag;
    }

    public static final class PlayerData {
        private final Map<String, Integer> defeats = new HashMap<>();
        private final Map<String, Integer> unlocked = new HashMap<>();
        private final Map<UUID, String> active = new HashMap<>();

        public int defeats(String id) { return defeats.getOrDefault(id, 0); }
        public int unlocked(String id) { return unlocked.getOrDefault(id, 0); }
        public int activeCount(String id) {
            int count = 0;
            for (String value : active.values()) {
                if (value.equals(id)) count++;
            }
            return count;
        }
        public Map<String, Integer> defeatsView() { return Collections.unmodifiableMap(defeats); }
        public Map<String, Integer> activeView() { return Collections.unmodifiableMap(active); }

        public int registerDefeat(String id) {
            int count = defeats.merge(id, 1, Integer::sum);
            int unlockedNow = count / 10;
            int unlockedBefore = unlocked(id);
            if (unlockedNow > unlockedBefore) unlocked.put(id, unlockedNow);
            return unlockedNow - unlockedBefore;
        }

        public boolean canSummon(String id) {
            return unlocked(id) > activeCount(id);
        }

        public void registerSummon(UUID entityUuid, String id) {
            active.put(entityUuid, id);
        }

        public boolean registerDismiss(UUID entityUuid) {
            return active.remove(entityUuid) != null;
        }

        /** A real death permanently consumes one unlocked servant. */
        public String registerDeath(UUID entityUuid) {
            String id = active.remove(entityUuid);
            if (id == null) return null;
            int count = unlocked(id);
            if (count > 0) unlocked.put(id, count - 1);
            return id;
        }

        private void save(CompoundTag tag) {
            ListTag defeatsList = new ListTag();
            for (Map.Entry<String, Integer> e : defeats.entrySet()) {
                CompoundTag value = new CompoundTag();
                value.putString("Id", e.getKey());
                value.putInt("Count", e.getValue());
                defeatsList.add(value);
            }
            tag.put("Defeats", defeatsList);

            ListTag unlockedList = new ListTag();
            for (Map.Entry<String, Integer> e : unlocked.entrySet()) {
                CompoundTag value = new CompoundTag();
                value.putString("Id", e.getKey());
                value.putInt("Count", e.getValue());
                unlockedList.add(value);
            }
            tag.put("Unlocked", unlockedList);

            ListTag activeList = new ListTag();
            for (Map.Entry<UUID, String> e : active.entrySet()) {
                CompoundTag value = new CompoundTag();
                value.putUUID("UUID", e.getKey());
                value.putString("Id", e.getValue());
                activeList.add(value);
            }
            tag.put("Active", activeList);
        }

        private static PlayerData load(CompoundTag tag) {
            PlayerData data = new PlayerData();
            ListTag defeatsList = tag.getList("Defeats", Tag.TAG_COMPOUND);
            for (int i = 0; i < defeatsList.size(); i++) {
                CompoundTag value = defeatsList.getCompound(i);
                data.defeats.put(value.getString("Id"), value.getInt("Count"));
            }
            ListTag unlockedList = tag.getList("Unlocked", Tag.TAG_COMPOUND);
            for (int i = 0; i < unlockedList.size(); i++) {
                CompoundTag value = unlockedList.getCompound(i);
                data.unlocked.put(value.getString("Id"), value.getInt("Count"));
            }
            ListTag activeList = tag.getList("Active", Tag.TAG_COMPOUND);
            for (int i = 0; i < activeList.size(); i++) {
                CompoundTag value = activeList.getCompound(i);
                data.active.put(value.getUUID("UUID"), value.getString("Id"));
            }
            return data;
        }
    }
}
