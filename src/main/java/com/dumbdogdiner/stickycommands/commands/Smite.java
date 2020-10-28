package com.dumbdogdiner.stickycommands.commands;

import com.dumbdogdiner.stickyapi.bukkit.command.AsyncCommand;
import com.dumbdogdiner.stickyapi.bukkit.command.ExitCode;
import com.dumbdogdiner.stickyapi.common.arguments.Arguments;
import com.dumbdogdiner.stickyapi.common.translation.LocaleProvider;
import com.dumbdogdiner.stickycommands.Main;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;


public class Smite extends AsyncCommand {


    private final LocaleProvider locale = Main.getPlugin(Main.class).getLocaleProvider();
    TreeMap<String, String> variables = locale.newVariables();
    private static final String PERMISSION_USE = "stickycommands.smite";

    private static final float EXPLOSION_STRENGTH = 1.5F;
    private static final int TARGET_RANGE = 100;



    public Smite(Plugin owner) {
        super("smite", owner);
        setPermission(PERMISSION_USE);
        setDescription("Smite a player, block, or yourself...");
        setAliases(Arrays.asList("strike", "lightningify"));
        variables.put("syntax", "/smite [player | me | everyone | " + ChatColor.BOLD + "target" + ChatColor.RESET + "]");

    }

    //TODO Move to StickyAPI, where its better suited?
    public List<String> getGroupsList() {
        LuckPerms perms = Main.getInstance().getPerms();
        List<String> returnList = new ArrayList<>();
        if (perms != null) {
             for(Group group : perms.getGroupManager().getLoadedGroups()){
                 returnList.add(group.getName().toLowerCase());
             }
        } else {
            Main.getInstance().getLogger().severe("Could not find luckperms!!!!!");
        }
        return returnList;
    }


    @Override
    public ExitCode executeCommand(CommandSender sender, String commandLabel, String[] args) {
        // Should really be in some sort of util function
        if (!sender.hasPermission(PERMISSION_USE))
            return ExitCode.EXIT_PERMISSION_DENIED.setMessage(locale.translate("no-permission", variables));
        Arguments a = new Arguments(args);
        a.optionalFlag("group", "group");
        a.optionalString("smitetarget", "target");



        String smiteTarget = a.get("smitetarget").toLowerCase();
        System.out.println(smiteTarget);
        boolean isConsole = !(sender instanceof Player);


        if (a.getFlag("group")) {
            variables.put("player", "the group " + a.get("smitetarget"));
            List<String> groupList = getGroupsList();
            if (groupList.contains(smiteTarget)) {

                //noinspection unchecked
                TreeMap<String, String> tempvars = (TreeMap<String, String>)variables.clone();
                tempvars.remove("player");
                tempvars.put("player", "%s");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("group." + smiteTarget)) {
                        if (smitePlayer(player)) {

                            sender.sendMessage(String.format(locale.translate("smite.smite-other-player-success", tempvars), player.getDisplayName()));
                        } else {
                                sender.sendMessage(String.format(locale.translate("not-online-player", tempvars), player.getDisplayName()));
                        }
                    }
                }
                return ExitCode.EXIT_SUCCESS.setMessage(locale.translate("smite.smite-other-player-success", variables));
            } else {
                variables.put("group", variables.get("player"));
                return ExitCode.EXIT_INVALID_SYNTAX.setMessage(locale.translate("invalid-group", variables));
            }
        } else {
            variables.put("player", a.get("smitetarget"));
            if (smiteTarget.equals("me") || smiteTarget.equals("target")) {
                if (isConsole) {
                    return ExitCode.EXIT_MUST_BE_PLAYER.setMessage(locale.translate("must-be-player", variables));

                } else {
                    if (smiteTarget.equals("me")) {
                        smiteMe((Player) sender);
                        return ExitCode.EXIT_SUCCESS.setMessage(locale.translate("smite.yourself", variables));
                    } else { // MUST be target
                        // TODO: Allow targetting an entity rather than a block
                        Location toStrike = ((Player) sender).getTargetBlock(null, TARGET_RANGE).getLocation();
                        World strikeWorld = ((Player) sender).getWorld();
                        variables.put("X", Integer.toString(toStrike.getBlockX()));
                        variables.put("Y", Integer.toString(toStrike.getBlockY()));
                        variables.put("Z", Integer.toString(toStrike.getBlockZ()));
                        variables.put("WORLD", strikeWorld.toString());
                        lightningOnCoord(toStrike, strikeWorld);
                        return ExitCode.EXIT_SUCCESS.setMessage(locale.translate("smite.smite-block", variables));
                    }
                }
            } else {

                if (smitePlayer(smiteTarget)) {

                    return ExitCode.EXIT_SUCCESS.setMessage(locale.translate("smite.smite-other-player-success", variables));
                } else {

                        return ExitCode.EXIT_INVALID_SYNTAX.setMessage(locale.translate("not-online-player", variables));

                }
            }
        }
    }

    /**
     * Smite a player
     *
     * @param playerName player to smite
     * @return if player exists (and was smitten) or not
     */
    private boolean smitePlayer(String playerName) {
        return smitePlayer(Bukkit.getPlayer(playerName));
    }

    /**
     * Smite a player
     *
     * @param player player to smite
     * @return if player exists (and was smitten) or not
     */
    private boolean smitePlayer(Player player) {
        if (player == null)
            return false;
        lightningOnCoord(player.getLocation(), player.getWorld());
        player.sendMessage(locale.translate("smite.smite-message", variables));
        System.out.println(locale.translate("smite.smite-message", variables));
        return true;
    }

    /**
     * Smite the sender
     *
     * @param sender command sender
     */
    private void smiteMe(Player sender) {
        lightningOnCoord(sender.getLocation(), sender.getWorld());
    }

    /**
     * Strike lightning on a given block
     *
     * @param location Location to strike
     * @param world    world of the location
     */

    private void lightningOnCoord(Location location, World world) {

        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
                LightningStrike strike = world.strikeLightning(location);

                world.createExplosion(location, EXPLOSION_STRENGTH, false, false, strike);
            }
        }, 1L);

    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        ArrayList<String> tabComplete = new ArrayList<>();

        if (args.length < 2) {
            tabComplete.add("me");
            tabComplete.add("target");
            tabComplete.add("group");
            for (var player : Bukkit.getOnlinePlayers())
                tabComplete.add(player.getName());
        } else if (args.length < 3 && args[0].equalsIgnoreCase("group")) {
            tabComplete.addAll(getGroupsList());
        }
        return tabComplete;
    }
}
