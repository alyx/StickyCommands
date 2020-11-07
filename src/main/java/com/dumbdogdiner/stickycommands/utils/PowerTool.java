package com.dumbdogdiner.stickycommands.utils;

import com.dumbdogdiner.stickycommands.StickyCommands;

import org.bukkit.entity.Player;

import lombok.Getter;
import lombok.Setter;

public class PowerTool {

    /**
     * The player this powertool is bound to
     */
    @Getter
    @Setter
    Player player;

    /**
     * The item the command is bound to
     */
    @Getter
    @Setter
    Item item;

    /**
     * The command this powertool will execute
     */
    @Getter
    @Setter
    String command;

    /**
     * If the command bound to this powertool is chatting
     */
    @Getter
    @Setter
    Boolean chat = false;

    /**
     * If this powertool is enabled or not
     */
    @Getter
    @Setter
    Boolean enabled = true;

    /**
     * Create a new powertool object
     * @param item The item to bind the command to
     * @param command The command to bind to this powertool
     * @param player The player to bind this powertool to
     */
    public PowerTool(Item item, String command, Player player) {
        this.player = player;
        this.item = item;
        this.command = command;
        if (this.command.startsWith("c:")) {
            this.command = this.command.replaceFirst("c:", "");
            this.chat = true;
        }
    }

    /**
     * Execute the powertool command
     */
    public void execute() {
        if (!enabled)
            return;

        if (chat)
            player.chat(command);
        else {
            player.performCommand(command);
            StickyCommands.getInstance().getLogger().info(String.format("%s issued command via powertool: /%s", player.getName(), command));
        }
    }
}
