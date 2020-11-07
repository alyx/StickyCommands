package com.dumbdogdiner.stickycommands.commands;

import java.util.TreeMap;

import com.dumbdogdiner.stickycommands.StickyCommands;
import com.dumbdogdiner.stickycommands.User;
import com.dumbdogdiner.stickyapi.bukkit.command.AsyncCommand;
import com.dumbdogdiner.stickyapi.bukkit.command.ExitCode;
import com.dumbdogdiner.stickyapi.common.translation.LocaleProvider;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class AfkCommand extends AsyncCommand {

    private static LocaleProvider locale = StickyCommands.getInstance().getLocaleProvider();
    TreeMap<String, String> variables = locale.newVariables();
    
    public AfkCommand(Plugin owner) {
        super("afk", owner);
        setPermission("stickycommands.afk");
        setDescription("Let the server know you're afk!");
        variables.put("syntax", "/afk");
    }

    @Override
    public ExitCode executeCommand(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof Player))
            return ExitCode.EXIT_MUST_BE_PLAYER.setMessage(locale.translate("must-be-player", variables));

        User user = StickyCommands.getInstance().getOnlineUser(((Player)sender).getUniqueId());
        variables.put("player", user.getName());
        variables.put("player_uuid", user.getUniqueId().toString());
        
        setAFKAndBroadcast(user, !user.isAfk());
        return ExitCode.EXIT_SUCCESS;
    }

    public static void setAFKAndBroadcast(User user, boolean afk){
        TreeMap<String, String> variables = locale.newVariables();
        variables.put("player", user.getName());
        variables.put("player_uuid", user.getUniqueId().toString());
        user.setAfk(afk);

        if(!user.isHidden()){
            if(user.isAfk()){
                // Bukkit is literally fucking retarded, and I can't use broadcastMessage because that magically doesn't work now! Who knew....
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(StickyCommands.getInstance().getLocaleProvider().translate("afk.afk", variables));
                }
            } else {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(StickyCommands.getInstance().getLocaleProvider().translate("afk.not-afk", variables));
                }
            }
        }
    }
}
