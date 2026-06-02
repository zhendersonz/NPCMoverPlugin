package me.hendi.npcmover;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PathEditor implements Listener {
    private final NPCMoverPlugin plugin;
    private final Map<UUID, String> editingPlayers = new HashMap<>();
    private final Map<UUID, Integer> pauseTimes = new HashMap<>();

    public PathEditor(NPCMoverPlugin plugin) {
        this.plugin = plugin;
        startVisualizer();
    }

    public void setPauseTime(Player player, int seconds) {
        pauseTimes.put(player.getUniqueId(), seconds);
        player.sendMessage("§eTempo de pausa definido para: §f" + seconds + " segundos.");
    }

    private void startVisualizer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID, String> entry : editingPlayers.entrySet()) {
                    Player player = org.bukkit.Bukkit.getPlayer(entry.getKey());
                    if (player == null || !player.isOnline()) continue;
                    
                    // Só mostra se estiver com graveto na mão
                    if (player.getInventory().getItemInMainHand().getType() != Material.STICK &&
                        player.getInventory().getItemInOffHand().getType() != Material.STICK) {
                        continue;
                    }
                    
                    NPCInstance npc = plugin.getNpcManager().getNPC(entry.getValue());
                    if (npc == null) continue;
                    
                    for (NPCInstance.WaypointData data : npc.getWaypointDataList()) {
                        if (data.pauseTime > 0) {
                            player.spawnParticle(Particle.ANGRY_VILLAGER, data.location, 3, 0.2, 0.2, 0.2, 0);
                        } else {
                            player.spawnParticle(Particle.HAPPY_VILLAGER, data.location, 5, 0.2, 0.2, 0.2, 0.05);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    public void startEditing(Player player, String npcId) {
        editingPlayers.put(player.getUniqueId(), npcId);
        pauseTimes.putIfAbsent(player.getUniqueId(), 3); // 3s por padrão
        player.sendMessage("§a§lModo de Edição Ativado: §f" + npcId);
        player.sendMessage("§7- §fClique Esquerdo: §7Add ponto de §fMOVIMENTO.");
        player.sendMessage("§7- §fShift + Clique Direito: §7Add ponto de §fPAUSA.");
        player.sendMessage("§7- §fClique Direito: §7Limpar todos os pontos.");
        player.sendMessage("§7- §fComando: §e/npcmove pause <segundos> §7para ajustar a espera.");
    }

    public void stopEditing(Player player) {
        editingPlayers.remove(player.getUniqueId());
        player.sendMessage("§cModo de edição desativado.");
    }

    public boolean isEditing(Player player) {
        return editingPlayers.containsKey(player.getUniqueId());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isEditing(player)) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.STICK) return;

        event.setCancelled(true);
        String npcId = editingPlayers.get(player.getUniqueId());
        NPCInstance npc = plugin.getNpcManager().getNPC(npcId);

        if (npc == null) {
            stopEditing(player);
            return;
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Location loc = event.getClickedBlock().getLocation().add(0.5, 1, 0.5);
            
            // Verificação de acessibilidade (Função 7)
            if (npc.getEntity() instanceof org.bukkit.entity.Mob mob) {
                if (mob.getPathfinder().findPath(loc) == null) {
                    player.sendMessage("§c§lAviso: §7O NPC pode ter dificuldade para chegar neste local (caminho não encontrado).");
                }
            }

            npc.addWaypoint(loc, 0, 0, 0); // Sem pausa
            player.sendMessage("§aPonto de §fMOVIMENTO §aadicionado.");
            plugin.getNpcManager().save();
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            if (player.isSneaking() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                // MODO PAUSA: Shift + Direito
                Location loc = event.getClickedBlock().getLocation().add(0.5, 1, 0.5);
                
                // Verificação de acessibilidade (Função 7)
                if (npc.getEntity() instanceof org.bukkit.entity.Mob mob) {
                    if (mob.getPathfinder().findPath(loc) == null) {
                        player.sendMessage("§c§lAviso: §7O NPC pode ter dificuldade para chegar neste local (caminho não encontrado).");
                    }
                }

                int seconds = pauseTimes.getOrDefault(player.getUniqueId(), 3);
                // Usamos o Yaw do player, mas Pitch 0 para ele olhar para frente (não pro chão)
                npc.addWaypoint(loc, seconds * 1000L, player.getLocation().getYaw(), 0.0f);
                player.sendMessage("§ePonto de §fPAUSA §aadicionado (§f" + seconds + "s§e).");
            } else {
                // LIMPAR: Apenas Direito
                npc.clearWaypoints();
                player.sendMessage("§cTodos os pontos foram removidos.");
            }
            plugin.getNpcManager().save();
        }
    }
}
