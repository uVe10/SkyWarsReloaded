package com.walrusone.skywarsreloaded.commands.maps;

import com.walrusone.skywarsreloaded.game.GameMap;
import com.walrusone.skywarsreloaded.utilities.Messaging;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TierCmd extends com.walrusone.skywarsreloaded.commands.BaseCmd  {
    public TierCmd(String t) {
        type = t;
        forcePlayer = false;
        cmdName = "tier";
        alias = new String[]{"setTier"};
        argLength = 3;
    }
    public boolean run(CommandSender sender, Player player, String[] args) {
        String worldName = args[1];
        if (!com.walrusone.skywarsreloaded.utilities.Util.get().isInteger(args[2])) {
            sender.sendMessage(new Messaging.MessageFormatter().format("error.map-min-be-int"));
            return true;
        }

        int tier = Integer.parseInt(args[2]);
        GameMap map = GameMap.getMap(worldName);
        if (map != null) {
            map.setTier(tier);
            sender.sendMessage(ChatColor.GREEN+"You have setted correctly the tier "+ tier + " to the map "+ map.getName());
            return true;
        }
        sender.sendMessage(new Messaging.MessageFormatter().format("error.map-does-not-exist"));
        return true;
    }
}
