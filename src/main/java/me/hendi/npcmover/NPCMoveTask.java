package me.hendi.npcmover;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class NPCMoveTask extends BukkitRunnable {
    private final NPCMoverPlugin plugin;

    public NPCMoveTask(NPCMoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (NPCInstance npc : plugin.getNpcManager().getAllNPCs()) {
            Entity entity = npc.getEntity();
            if (entity == null || entity.isDead() || !(entity instanceof org.bukkit.entity.Mob mob)) {
                plugin.getNpcManager().spawn(npc);
                continue;
            }

            // OTIMIZAÇÃO: Se não houver jogadores num raio de 48 blocos, para de processar movimento
            boolean playerNearby = false;
            for (org.bukkit.entity.Player p : mob.getWorld().getPlayers()) {
                if (p.getLocation().distanceSquared(entity.getLocation()) < 2304) { // 48^2
                    playerNearby = true;
                    break;
                }
            }
            if (!playerNearby) {
                mob.getPathfinder().stopPathfinding();
                continue;
            }

            // Bloqueia qualquer movimento se não houver waypoints
            if (npc.getWaypoints().isEmpty()) {
                mob.getPathfinder().stopPathfinding();
                if (entity.getLocation().distanceSquared(npc.getSpawnLocation()) > 0.5) {
                    entity.teleport(npc.getSpawnLocation());
                }
                continue;
            }

            // Pega o waypoint atual ANTES de qualquer alteração
            NPCInstance.WaypointData data = npc.getCurrentWaypointData();
            if (data == null) continue;

            // 1. Lógica de Pausa
            if (System.currentTimeMillis() < npc.getNextMoveTime()) {
                mob.getPathfinder().stopPathfinding();
                entity.setRotation(data.yaw, data.pitch);
                continue;
            }

            org.bukkit.Location currentTarget = data.location;
            double distanceSq = entity.getLocation().distanceSquared(currentTarget);
            
            // 2. Lógica de Chegada
            if (distanceSq < 1.0) {
                if (data.pauseTime > 0 && npc.getNextMoveTime() == 0) {
                    npc.setNextMoveTime(System.currentTimeMillis() + data.pauseTime);
                    entity.setRotation(data.yaw, data.pitch);
                    continue; 
                }
                npc.nextWaypoint();
                npc.setNextMoveTime(0);
                continue;
            }

            // 3. Lógica de Movimento: Usa a velocidade customizada do NPC
            mob.getPathfinder().moveTo(currentTarget, npc.getSpeed());
            smoothLook(entity, currentTarget); 
            
            if (distanceSq > 225) { 
                entity.teleport(npc.getSpawnLocation());
            }
        }
    }

    private void smoothLook(org.bukkit.entity.Entity entity, org.bukkit.Location target) {
        org.bukkit.Location loc = entity.getLocation();
        org.bukkit.util.Vector direction = target.toVector().subtract(loc.toVector());
        if (direction.lengthSquared() < 0.01) return;
        org.bukkit.Location lookAt = loc.setDirection(direction);
        smoothLookFixed(entity, lookAt.getYaw(), lookAt.getPitch());
    }

    private void smoothLookFixed(org.bukkit.entity.Entity entity, float targetYaw, float targetPitch) {
        org.bukkit.Location loc = entity.getLocation();
        float currentYaw = loc.getYaw();
        float currentPitch = loc.getPitch();
        
        // Interpolação simples para rotação não ser instantânea
        float newYaw = currentYaw + (targetYaw - currentYaw) * 0.15f;
        float newPitch = currentPitch + (targetPitch - currentPitch) * 0.15f;
        
        entity.setRotation(newYaw, newPitch);
    }
}
