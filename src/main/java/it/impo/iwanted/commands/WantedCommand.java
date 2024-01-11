package it.impo.iwanted.commands;

import it.impo.iwanted.IWanted;
import it.impo.iwanted.sql.Database;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import static it.impo.iwanted.IWanted.plugin;

public class WantedCommand implements CommandExecutor {
    private IWanted instance;
    public WantedCommand(IWanted instance){ this.instance = instance; }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player player)){
            Bukkit.getConsoleSender().sendMessage("[IWANTED] Non puoi eseguire il comando dalla console");
            return true;
        }
        String prefix = instance.getConfig().getString("PREFIX");

        if(!(player.hasPermission(Objects.requireNonNull(instance.getConfig().getString("PERMISSION.DEFAULT"))))){
            player.sendMessage(prefix + instance.getConfig().getString("MESSAGE.NO-PERMISSION"));
            return true;
        }

        if(args.length < 1 || args[0].equalsIgnoreCase("help")){
            player.sendMessage("  §6§lWANTED §8- §b§lINFO");
            player.sendMessage("  §7/wanted add <player> <reason>");
            player.sendMessage("  §7/wanted remove <player>");
            player.sendMessage("  §7/wanted list <page>");
            player.sendMessage("");
            return true;
        }

        if(args[0].equalsIgnoreCase("add")){
            if(args.length < 3){
                player.sendMessage(prefix + instance.getConfig().getString("MESSAGE.ARGOUMENTS-ERROR"));
                return true;
            }

            Player wanted = Bukkit.getPlayer(args[1]);
            String reason = String.join(" ", (CharSequence[]) Arrays.copyOfRange(args,2, args.length));
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date dataAttuale = new Date();
            if(wanted != null) {
                if (Database.getWanted(wanted)) {
                    player.sendMessage(prefix + instance.getConfig().getString("MESSAGE.ALWAYS-WANTED").replace("%player%", wanted.getName()));
                    return true;
                }

                Database.insertWanted(wanted.getName(), reason, formatter.format(dataAttuale), player, wanted);
            }else{
                player.sendMessage(prefix + instance.getConfig().getString("MESSAGE.PLAYER-NOT-FOUND"));
                return true;
            }
        }else if(args[0].equalsIgnoreCase("remove")){
            if(args.length < 2){
                player.sendMessage(prefix + instance.getConfig().getString("MESSAGE.ARGOUMENTS-ERROR"));
                return true;
            }

            Player wanted = Bukkit.getPlayer(args[1]);
            if(wanted != null){
                if (Database.getWanted(wanted)) {
                    Database.deleteWanted(wanted.getName(), player, wanted);
                    player.sendMessage(prefix + instance.getConfig().getString("MESSAGE.WANTED-REMOVED").replace("%player%", wanted.getName()));
                    return true;
                }else{
                    player.sendMessage(prefix + instance.getConfig().getString("MESSAGE.PLAYER-NOT-WANTED").replace("%player%", wanted.getName()));
                }
            }else{
                player.sendMessage(prefix + instance.getConfig().getString("MESSAGE.PLAYER-NOT-FOUND"));
                return true;
            }
        }else if(args[0].equalsIgnoreCase("list")){
            if(args.length < 2){
                player.sendMessage(prefix + instance.getConfig().getString("MESSAGE.ARGOUMENTS-ERROR"));
                return true;
            }

            int page = Integer.parseInt(args[1]);
            Database.getWantedList(player, page);
        }else if(args[0].equalsIgnoreCase("reload")){
            plugin.reloadConfig();
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            plugin.getServer().getPluginManager().enablePlugin(plugin);
            player.sendMessage(prefix + instance.getConfig().getString("MESSAGE.CONFIG-RELOADED"));

        }


        return true;
    }
}

