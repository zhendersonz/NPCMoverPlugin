package me.hendi.npcmover;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class NPCManager {
    private final NPCMoverPlugin plugin;
    private final Map<String, NPCInstance> npcs = new HashMap<>();
    private final File configFile;
    private FileConfiguration config;

    public NPCManager(NPCMoverPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "npcs.yml");
        load();
    }

    public void createNPC(String id, EntityType type, Location loc) {
        NPCInstance npc = new NPCInstance(id, type, loc);
        npcs.put(id, npc);
        spawn(npc);
        save();
    }

    public void removeNPC(String id) {
        NPCInstance npc = npcs.remove(id);
        if (npc != null && npc.getEntity() != null) {
            npc.getEntity().remove();
        }
        save();
    }

    public NPCInstance getNPC(String id) {
        return npcs.get(id);
    }

    public Collection<NPCInstance> getAllNPCs() {
        return npcs.values();
    }

    public void spawn(NPCInstance npc) {
        if (npc.getEntity() != null && !npc.getEntity().isDead()) {
            npc.getEntity().remove();
        }
        Entity entity = npc.getSpawnLocation().getWorld().spawnEntity(npc.getSpawnLocation(), npc.getType());
        entity.setCustomNameVisible(true);
        entity.setCustomName(org.bukkit.ChatColor.translateAlternateColorCodes('&', npc.getId()));
        
        entity.setInvulnerable(true); // Não sofre dano
        entity.setMetadata("NPCMover", new org.bukkit.metadata.FixedMetadataValue(plugin, true)); // Proteção básica
        entity.setVisualFire(false); // Não mostra fogo visualmente
        
        if (entity instanceof LivingEntity living) {
            living.setAI(true);
            living.setRemoveWhenFarAway(false);
            living.setCanPickupItems(false);
            
            if (entity instanceof org.bukkit.entity.Mob mob) {
                // Limpa metas de IA que fazem o NPC andar sozinho
                mob.getPathfinder().stopPathfinding();
                
                // Impede que o NPC procure abrigo ou fuja (POI/Pânico)
                if (mob instanceof org.bukkit.entity.Creature creature) {
                    creature.setAware(true);
                }
            }
        }
        
        npc.setEntity(entity);
    }

    public void save() {
        config = new YamlConfiguration();
        for (NPCInstance npc : npcs.values()) {
            String path = "npcs." + npc.getId();
            config.set(path + ".type", npc.getType().name());
            config.set(path + ".location", npc.getSpawnLocation());
            config.set(path + ".skin", npc.getSkinOwner());
            config.set(path + ".speed", npc.getSpeed());
            config.set(path + ".interact_command", npc.getInteractCommand());
            
            List<Map<String, Object>> waypointList = new ArrayList<>();
            for (NPCInstance.WaypointData data : npc.getWaypointDataList()) {
                Map<String, Object> map = new HashMap<>();
                map.put("loc", data.location);
                map.put("pause", data.pauseTime);
                map.put("yaw", (double)data.yaw);
                map.put("pitch", (double)data.pitch);
                waypointList.add(map);
            }
            config.set(path + ".waypoint_data", waypointList);
        }
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        if (!configFile.exists()) return;
        config = YamlConfiguration.loadConfiguration(configFile);
        if (config.getConfigurationSection("npcs") == null) return;

        for (String id : config.getConfigurationSection("npcs").getKeys(false)) {
            String path = "npcs." + id;
            EntityType type = EntityType.valueOf(config.getString(path + ".type"));
            Location loc = config.getLocation(path + ".location");
            NPCInstance npc = new NPCInstance(id, type, loc);
            npc.setSkinOwner(config.getString(path + ".skin"));
            npc.setSpeed(config.getDouble(path + ".speed", 1.0));
            npc.setInteractCommand(config.getString(path + ".interact_command"));
            
            List<?> dataList = config.getList(path + ".waypoint_data");
            if (dataList != null) {
                for (Object o : dataList) {
                    if (o instanceof Map<?, ?> map) {
                        Location wLoc = (Location) map.get("loc");
                        long pause = ((Number) map.get("pause")).longValue();
                        float yaw = ((Number) map.get("yaw")).floatValue();
                        float pitch = ((Number) map.get("pitch")).floatValue();
                        npc.addWaypoint(wLoc, pause, yaw, pitch);
                    }
                }
            }
            npcs.put(id, npc);
            spawn(npc);
        }
    }

    public void despawnAll() {
        for (NPCInstance npc : npcs.values()) {
            if (npc.getEntity() != null) npc.getEntity().remove();
        }
    }
}
