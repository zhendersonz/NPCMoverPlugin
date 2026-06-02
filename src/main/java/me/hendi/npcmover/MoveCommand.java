package me.hendi.npcmover;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import org.bukkit.command.TabCompleter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MoveCommand implements CommandExecutor, TabCompleter {
    private final NPCMoverPlugin plugin;

    public MoveCommand(NPCMoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "path", "show", "remove", "list", "tp", "tphere", "speed", "cmd").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            return Arrays.stream(EntityType.values())
                    .filter(EntityType::isSpawnable)
                    .map(Enum::name)
                    .filter(s -> s.startsWith(args[1].toUpperCase()))
                    .collect(Collectors.toList());
        }
        if (args.length >= 2 && (args[0].equalsIgnoreCase("path") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("show") || args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("tphere") || args[0].equalsIgnoreCase("speed") || args[0].equalsIgnoreCase("cmd"))) {
            String input = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).toLowerCase();
            return plugin.getNpcManager().getAllNPCs().stream()
                    .map(NPCInstance::getId)
                    .filter(s -> s.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando.");
            return true;
        }

        if (!player.hasPermission("npcmover.admin")) {
            player.sendMessage("§cVocê não tem permissão.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 3) {
                    player.sendMessage("§cUse: /npcmove create <tipo> <nome...>");
                    return true;
                }
                try {
                    EntityType type = EntityType.valueOf(args[1].toUpperCase());
                    // Pega todo o resto dos argumentos como o nome
                    String name = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    
                    plugin.getNpcManager().createNPC(name, type, player.getLocation());
                    player.sendMessage("§aNPC §f" + name + " §a(Tipo: " + type.name() + ") criado com sucesso!");
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cTipo de entidade inválido. Use o TAB para ver as opções.");
                }
                break;

            case "path":
                if (args.length < 2) {
                    player.sendMessage("§cUse: /npcmove path <nome...>");
                    return true;
                }
                String pathName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                if (plugin.getPathEditor().isEditing(player)) {
                    plugin.getPathEditor().stopEditing(player);
                } else {
                    if (plugin.getNpcManager().getNPC(pathName) == null) {
                        player.sendMessage("§cNPC '§f" + pathName + "§c' não encontrado.");
                        return true;
                    }
                    plugin.getPathEditor().startEditing(player, pathName);
                }
                break;

            case "show":
                if (args.length < 2) {
                    player.sendMessage("§cUse: /npcmove show <nome...>");
                    return true;
                }
                String showName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                NPCInstance showNpc = plugin.getNpcManager().getNPC(showName);
                if (showNpc == null) {
                    player.sendMessage("§cNPC '§f" + showName + "§c' não encontrado.");
                    return true;
                }
                // Mostra partículas por 10 segundos
                player.sendMessage("§aMostrando o caminho do NPC §f" + showName + " §apor 10 segundos.");
                new org.bukkit.scheduler.BukkitRunnable() {
                    int ticks = 0;
                    @Override
                    public void run() {
                        if (ticks > 20 || !player.isOnline()) {
                            this.cancel();
                            return;
                        }
                        for (org.bukkit.Location loc : showNpc.getWaypoints()) {
                            player.spawnParticle(org.bukkit.Particle.END_ROD, loc, 3, 0.1, 0.1, 0.1, 0.02);
                        }
                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 10L);
                break;

            case "remove":
                if (args.length < 2) {
                    player.sendMessage("§cUse: /npcmove remove <nome...>");
                    return true;
                }
                String removeName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                plugin.getNpcManager().removeNPC(removeName);
                player.sendMessage("§aNPC '§f" + removeName + "§a' removido.");
                break;

            case "list":
                player.sendMessage("§b§lNPCs Registrados:");
                if (plugin.getNpcManager().getAllNPCs().isEmpty()) {
                    player.sendMessage("§7Nenhum NPC criado.");
                } else {
                    for (NPCInstance npc : plugin.getNpcManager().getAllNPCs()) {
                        player.sendMessage("§7- §f" + npc.getId() + " §8(Tipo: " + npc.getType().name() + ", Pontos: " + npc.getWaypoints().size() + ")");
                    }
                }
                break;

            case "pause":
                if (args.length < 2) {
                    player.sendMessage("§cUse: /npcmove pause <segundos>");
                    return true;
                }
                try {
                    int seconds = Integer.parseInt(args[1]);
                    plugin.getPathEditor().setPauseTime(player, seconds);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cPor favor, insira um número válido de segundos.");
                }
                break;

            case "tp":
                if (args.length < 2) {
                    player.sendMessage("§cUse: /npcmove tp <nome...>");
                    return true;
                }
                String tpName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                NPCInstance tpNpc = plugin.getNpcManager().getNPC(tpName);
                if (tpNpc != null && tpNpc.getEntity() != null) {
                    player.teleport(tpNpc.getEntity().getLocation());
                    player.sendMessage("§aTeleportado para o NPC §f" + tpName);
                } else {
                    player.sendMessage("§cNPC não encontrado.");
                }
                break;

            case "tphere":
                if (args.length < 2) {
                    player.sendMessage("§cUse: /npcmove tphere <nome...>");
                    return true;
                }
                String tpHName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                NPCInstance tphNpc = plugin.getNpcManager().getNPC(tpHName);
                if (tphNpc != null && tphNpc.getEntity() != null) {
                    tphNpc.getEntity().teleport(player.getLocation());
                    player.sendMessage("§aNPC §f" + tpHName + " §ateleportado para você.");
                } else {
                    player.sendMessage("§cNPC não encontrado.");
                }
                break;

            case "speed":
                if (args.length < 3) {
                    player.sendMessage("§cUse: /npcmove speed <nome...> <valor>");
                    player.sendMessage("§7Ex: /npcmove speed Guarda 1.5");
                    return true;
                }
                try {
                    double speedVal = Double.parseDouble(args[args.length - 1]);
                    String sName = String.join(" ", Arrays.copyOfRange(args, 1, args.length - 1));
                    NPCInstance sNpc = plugin.getNpcManager().getNPC(sName);
                    if (sNpc != null) {
                        sNpc.setSpeed(speedVal);
                        plugin.getNpcManager().save();
                        player.sendMessage("§aVelocidade do NPC §f" + sName + " §adefinida para §f" + speedVal);
                    } else {
                        player.sendMessage("§cNPC não encontrado.");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("§cValor de velocidade inválido.");
                }
                break;

            case "cmd":
                if (args.length < 3) {
                    player.sendMessage("§cUse: /npcmove cmd <nome...> <comando>");
                    player.sendMessage("§7Dica: Use {player} para o nome de quem clicar.");
                    player.sendMessage("§7Para remover: /npcmove cmd <nome...> none");
                    return true;
                }
                String cName = "";
                String fullCmd = "";
                // Tenta achar o NPC na lista para saber onde termina o nome
                for (NPCInstance n : plugin.getNpcManager().getAllNPCs()) {
                    if (String.join(" ", args).contains(n.getId())) {
                        cName = n.getId();
                        fullCmd = String.join(" ", args).split(cName)[1].trim();
                        break;
                    }
                }

                if (cName.isEmpty()) {
                    player.sendMessage("§cNPC não encontrado. Certifique-se de digitar o nome exato.");
                    return true;
                }

                NPCInstance cNpc = plugin.getNpcManager().getNPC(cName);
                if (fullCmd.equalsIgnoreCase("none")) {
                    cNpc.setInteractCommand(null);
                    player.sendMessage("§aInteração removida do NPC §f" + cName);
                } else {
                    cNpc.setInteractCommand(fullCmd);
                    player.sendMessage("§aComando definido para o NPC §f" + cName + "§a: §7" + fullCmd);
                }
                plugin.getNpcManager().save();
                break;

            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§b§lNPCMover - Comandos");
        player.sendMessage("§f/npcmove create <tipo> <nome...> §7- Cria um NPC.");
        player.sendMessage("§f/npcmove path <nome...> §7- Edita o caminho com graveto.");
        player.sendMessage("§f/npcmove pause <segundos> §7- Define o tempo de pausa do editor.");
        player.sendMessage("§f/npcmove show <nome...> §7- Mostra o caminho atual.");
        player.sendMessage("§f/npcmove tp <nome...> §7- Teleporta você até o NPC.");
        player.sendMessage("§f/npcmove tphere <nome...> §7- Teleporta o NPC até você.");
        player.sendMessage("§f/npcmove remove <nome...> §7- Remove um NPC.");
        player.sendMessage("§f/npcmove list §7- Lista todos os NPCs.");
        player.sendMessage("§f/npcwand §7- Recebe o graveto de edição.");
    }
}
