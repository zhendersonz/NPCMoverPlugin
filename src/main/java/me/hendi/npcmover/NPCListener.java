package me.hendi.npcmover;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class NPCListener implements Listener {
    private final NPCMoverPlugin plugin;

    public NPCListener(NPCMoverPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCombust(EntityCombustEvent event) {
        Entity entity = event.getEntity();
        if (entity.hasMetadata("NPCMover")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        Entity entity = event.getEntity();
        if (entity.hasMetadata("NPCMover")) {
            event.setCancelled(true);
            event.setTarget(null);
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (NPCInstance npc : plugin.getNpcManager().getAllNPCs()) {
            if (npc.getSpawnLocation().getChunk().equals(event.getChunk())) {
                plugin.getNpcManager().spawn(npc);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (!entity.hasMetadata("NPCMover")) return;
        
        event.setCancelled(true);
        Player player = event.getPlayer();
        
        // Se o player estiver no modo editor, não executa o comando para não atrapalhar
        if (plugin.getPathEditor().isEditing(player)) return;

        // Procura qual NPC foi clicado pelo nome (id)
        for (NPCInstance npc : plugin.getNpcManager().getAllNPCs()) {
            if (npc.getEntity() != null && npc.getEntity().getUniqueId().equals(entity.getUniqueId())) {
                String cmd = npc.getInteractCommand();
                if (cmd != null && !cmd.isEmpty()) {
                    // Executa o comando como CONSOLE ou PLAYER
                    // Substitui {player} pelo nome de quem clicou
                    String finalCmd = cmd.replace("{player}", player.getName());
                    if (finalCmd.startsWith("/")) finalCmd = finalCmd.substring(1);
                    
                    org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), finalCmd);
                }
                break;
            }
        }
    }
}
