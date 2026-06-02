package me.hendi.npcmover;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NPCInstance {
    private String id;
    private EntityType type;
    private Location spawnLocation;
    private List<Location> waypoints;
    private int currentWaypointIndex = 0;
    private UUID entityId;
    private Entity entity;
    private String skinOwner;
    private long nextMoveTime = 0;
    private double speed = 1.0;
    private String interactCommand;
    private List<WaypointData> waypointDataList = new ArrayList<>();

    public static class WaypointData {
        public Location location;
        public long pauseTime; // em milisegundos
        public float yaw;
        public float pitch;

        public WaypointData(Location location, long pauseTime, float yaw, float pitch) {
            this.location = location;
            this.pauseTime = pauseTime;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    public NPCInstance(String id, EntityType type, Location spawnLocation) {
        this.id = id;
        this.type = type;
        this.spawnLocation = spawnLocation;
        this.waypoints = new ArrayList<>();
    }

    public String getId() { return id; }
    public EntityType getType() { return type; }
    public Location getSpawnLocation() { return spawnLocation; }
    public List<Location> getWaypoints() { return waypoints; }
    public List<WaypointData> getWaypointDataList() { return waypointDataList; }
    
    public void addWaypoint(Location loc, long pauseTime, float yaw, float pitch) { 
        waypoints.add(loc); 
        waypointDataList.add(new WaypointData(loc, pauseTime, yaw, pitch));
    }
    
    public void clearWaypoints() { 
        waypoints.clear(); 
        waypointDataList.clear();
        currentWaypointIndex = 0;
    }

    public WaypointData getCurrentWaypointData() {
        if (waypointDataList.isEmpty()) return null;
        if (currentWaypointIndex >= waypointDataList.size()) currentWaypointIndex = 0;
        return waypointDataList.get(currentWaypointIndex);
    }
    
    public Entity getEntity() { return entity; }
    public void setEntity(Entity entity) { 
        this.entity = entity; 
        this.entityId = entity.getUniqueId();
    }
    
    public String getSkinOwner() { return skinOwner; }
    public void setSkinOwner(String skinOwner) { this.skinOwner = skinOwner; }

    public long getNextMoveTime() { return nextMoveTime; }
    public void setNextMoveTime(long nextMoveTime) { this.nextMoveTime = nextMoveTime; }

    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }

    public String getInteractCommand() { return interactCommand; }
    public void setInteractCommand(String interactCommand) { this.interactCommand = interactCommand; }

    public void nextWaypoint() {
        if (waypoints.isEmpty()) return;
        currentWaypointIndex = (currentWaypointIndex + 1) % waypoints.size();
    }

    public Location getCurrentWaypoint() {
        if (waypoints.isEmpty()) return null;
        if (currentWaypointIndex >= waypoints.size()) currentWaypointIndex = 0;
        return waypoints.get(currentWaypointIndex);
    }
}
