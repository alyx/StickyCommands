package com.dumbdogdiner.stickycommands.runnables;

import com.dumbdogdiner.stickyapi.common.util.NumberUtil;
import com.dumbdogdiner.stickycommands.StickyCommands;
import com.dumbdogdiner.stickycommands.User;
import com.dumbdogdiner.stickycommands.commands.AfkCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.TimerTask;
import java.util.TreeMap;

public class AfkTimeRunnable extends TimerTask {
    protected Integer AFK_TIMEOUT = StickyCommands.getInstance().getConfig().getInt("afk-timeout", 300);

    @Override
    public void run() {
        for (User user : StickyCommands.getInstance().getOnlineUserCache().values()) {
            user.incAfkTime();
            if (user.getAfkTime() >= AFK_TIMEOUT) {
                var variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
                if (!user.isAfk()) {
                    AfkCommand.setAFKAndBroadcast(user, true);
                } else if (exceedsPermittedTime(user, user.getAfkTime() - AFK_TIMEOUT)) {
                    variables.put("time", String.valueOf(user.getAfkTime() - AFK_TIMEOUT));
                    System.out.println(variables.get("time"));
                    Bukkit.getScheduler().scheduleSyncDelayedTask(StickyCommands.getInstance(), () -> Bukkit.getPlayer(user.getUniqueId()).kickPlayer(StickyCommands.getInstance().getLocaleProvider().translate("afk.afk-kick", variables)), 1L);
                }
            }
        }
    }

    private Boolean exceedsPermittedTime(User user, Integer time) {
        Player player = Bukkit.getPlayer(user.getUniqueId());
        if (player == null) {
            System.err.println("Error in exceedsPermissionTime: Player was null. Defaulting to false.");
            return false;
        }
        if(user.isHidden() || user.isVanished())
            return false;
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            if (!permission.getPermission().contains("stickycommands.afk.autokick"))
                continue; // We don't care about other permissions
            var afkArr = permission.getPermission().split("\\.");
            var afkPerm = afkArr.length == 4 ? afkArr[3] : "unlimited"; // We only care about the time!
            if (!NumberUtil.isNumeric(afkPerm) || afkPerm.equals("unlimited"))
                return false;

            var permTime = Integer.parseInt(afkPerm);
            return permTime <= user.getAfkTime() - AFK_TIMEOUT;
        }
        return false;
    }
}
