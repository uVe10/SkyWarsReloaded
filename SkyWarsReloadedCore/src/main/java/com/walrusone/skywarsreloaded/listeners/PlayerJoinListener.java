package com.walrusone.skywarsreloaded.listeners;

import com.walrusone.skywarsreloaded.SkyWarsReloaded;
import com.walrusone.skywarsreloaded.enums.GameType;
import com.walrusone.skywarsreloaded.game.GameMap;
import com.walrusone.skywarsreloaded.managers.MatchManager;
import com.walrusone.skywarsreloaded.managers.PlayerStat;
import com.walrusone.skywarsreloaded.utilities.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onLogin(final PlayerLoginEvent event) {
        //VICTOR
        final Player player = event.getPlayer();
        FileConfiguration config = SkyWarsReloaded.get().getConfigUtil().getYamlConfiguration();
        if(SkyWarsReloaded.get().getConfigUtil().getYamlConfiguration().get("hearts."+player.getUniqueId()) != null){
            long banTime = Long.parseLong(config.getString("hearts."+player.getUniqueId()+".banTime"));
            if(banTime <= System.currentTimeMillis()){
                player.setHealthScale(SkyWarsReloaded.get().getConfigUtil().getYamlConfiguration().getInt("hearts."+player.getUniqueId()+".health"));
            } else{
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED+"You are banned for: "+ getMSG(banTime) +" more");
            }
        } else{
            config.set("hearts."+player.getUniqueId()+".name", player.getName());
            config.set("hearts."+player.getUniqueId()+".health", 20);
            config.set("hearts."+player.getUniqueId()+".tier", SkyWarsReloaded.get().getTier(20));
            config.set("hearts."+player.getUniqueId()+".banTime", "0");
            SkyWarsReloaded.get().getConfigUtil().saveConfig();
        }
    }

    public static String getMSG(long time) {
        String message = "";

        long now = System.currentTimeMillis();
        long diff = time - now;
        long seconds = (long) (diff / 1000);

        if (seconds >= 60 * 60 * 24) {
            long days = seconds / (60 * 60 * 24);
            seconds = seconds % (60 * 60 * 24);

            message += days + " Day(s) ";
        }
        if (seconds >= 60 * 60) {
            long hours = seconds / (60 * 60);
            seconds = seconds % (60 * 60);

            message += hours + " Hour(s) ";
        }
        if (seconds >= 60) {
            long min = seconds / 60;
            seconds = seconds % 60;

            message += min + " Minute(s) ";
        }
        if (seconds >= 0) {
            message += seconds + " Second(s) ";
        }

        return message;
    }



    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (SkyWarsReloaded.getCfg().getSpawn() != null && SkyWarsReloaded.getCfg().teleportOnJoin()) {
                    player.teleport(SkyWarsReloaded.getCfg().getSpawn());
                }
            }
        }.runTaskLater(SkyWarsReloaded.get(), 1);

        if (SkyWarsReloaded.getCfg().promptForResource()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.setResourcePack(SkyWarsReloaded.getCfg().getResourceLink());
                }
            }.runTaskLater(SkyWarsReloaded.get(), 20);
        }

        if (PlayerStat.getPlayerStats(player) != null) {
            PlayerStat.removePlayer(player.getUniqueId().toString());
        }

        if (!SkyWarsReloaded.getCfg().bungeeMode()) {
            for (GameMap gMap : GameMap.getMapsCopy()) {
                if (gMap.getCurrentWorld() != null && gMap.getCurrentWorld().equals(player.getWorld())) {
                    if (SkyWarsReloaded.getCfg().getSpawn() != null) {
                        player.teleport(SkyWarsReloaded.getCfg().getSpawn());
                    }
                }
            }
        }

        PlayerStat pStats = new PlayerStat(player);
        PlayerStat.getPlayers().add(pStats);
        pStats.updatePlayerIfInLobby(player);
        // Load player data
        pStats.loadStats(() -> {
            if (!postLoadStats(player)) return;

            SkyWarsReloaded.get().getUpdater().handleJoiningPlayer(player);
        });
    }

    /**
     * Handle bungeecord join
     * @param player The joining player
     * @return Whether the player was kicked when trying to join
     */
    public boolean postLoadStats(Player player) {
        // After stats are done loading, move to a game if in bungeecord mode
        if (SkyWarsReloaded.getCfg().bungeeMode()) {
            if (player != null) {
                if (!SkyWarsReloaded.getCfg().isLobbyServer()) {
                    Bukkit.getLogger().log(Level.WARNING, "Trying to let " + player.getName() + " join a game");

                    boolean joined = MatchManager.get().joinGame(player, GameType.ALL);
                    if (!joined) {
                        Bukkit.getLogger().log(Level.WARNING, "Failed to put " + player.getName() + " in a game");
                        if (SkyWarsReloaded.getCfg().debugEnabled()) {
                            Util.get().logToFile(ChatColor.YELLOW + "Couldn't find an arena for player " + player.getName() + ". Sending the player back to the skywars lobby.");
                        }
                        if (player.hasPermission("sw.admin")) {
                            player.sendMessage(ChatColor.RED +
                                    "Skywars encountered an issue while joining this bungeecord mode server.\n" +
                                    "However, since you have the sw.admin permissions, you will not be kicked to the lobby.");
                        } else {
                            SkyWarsReloaded.get().sendBungeeMsg(player, "Connect", SkyWarsReloaded.getCfg().getBungeeLobby());
                            kickPlayerIfStillOnline(player, 20);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // UTILS

    public void kickPlayerIfStillOnline(Player player, long ticks) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) player.kickPlayer("");
            }
        }.runTaskLater(SkyWarsReloaded.get(), ticks);
    }
}