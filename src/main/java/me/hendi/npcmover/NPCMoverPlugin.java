package me.hendi.npcmover;

import org.bukkit.plugin.java.JavaPlugin;

public class NPCMoverPlugin extends JavaPlugin {
    private NPCManager npcManager;
    private PathEditor pathEditor;

    @Override
    public void onEnable() {
        // Inicializa o gerenciador de NPCs
        this.npcManager = new NPCManager(this);
        
        // Inicializa o editor de caminhos
        this.pathEditor = new PathEditor(this);
        getServer().getPluginManager().registerEvents(pathEditor, this);
        getServer().getPluginManager().registerEvents(new NPCListener(this), this);

        // Registra os comandos
        getCommand("npcmove").setExecutor(new MoveCommand(this));
        getCommand("npcmove").setTabCompleter(new MoveCommand(this));
        getCommand("npcwand").setExecutor((sender, command, label, args) -> {
            if (sender instanceof org.bukkit.entity.Player player) {
                player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.STICK));
                player.sendMessage("§a§l[NPCMover] §fVocê recebeu o graveto de edição!");
            }
            return true;
        });

        // Inicia a tarefa de movimentação (roda a cada 1 tick para fluidez máxima)
        new NPCMoveTask(this).runTaskTimer(this, 20L, 1L);

        getLogger().info("NPCMoverPlugin ativado com sucesso!");
    }

    @Override
    public void onDisable() {
        if (npcManager != null) {
            npcManager.despawnAll();
        }
        getLogger().info("NPCMoverPlugin desativado.");
    }

    public NPCManager getNpcManager() {
        return npcManager;
    }

    public PathEditor getPathEditor() {
        return pathEditor;
    }
}
