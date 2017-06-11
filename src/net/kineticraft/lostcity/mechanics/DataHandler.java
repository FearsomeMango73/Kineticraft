package net.kineticraft.lostcity.mechanics;

import net.kineticraft.lostcity.Core;
import net.kineticraft.lostcity.data.QueryTools;
import net.kineticraft.lostcity.utils.Utils;
import net.kineticraft.lostcity.data.wrappers.JsonLocation;
import net.kineticraft.lostcity.data.wrappers.KCPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Handler - Manages loading and unloading of player data.
 *
 * Created by Kneesnap on 5/29/2017.
 */
public class DataHandler extends Mechanic {


    @Override
    public void onEnable() {
        Core.makeFolder("players");

        // Every 5 minutes, save all playerdata
        Bukkit.getScheduler().runTaskTimerAsynchronously(Core.getInstance(), () -> saveAllPlayers(), 0, 5 * 60 * 20);
    }

    /**
     * Save all loaded player data.
     */
    public static void saveAllPlayers() {
        KCPlayer.getPlayerMap().values().forEach(KCPlayer::writeData);
    }

    @EventHandler(priority = EventPriority.LOWEST) // Run first, so other things like ban checker have data.
    public void onAttemptJoin(AsyncPlayerPreLoginEvent evt) {
        try {
            if (evt.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED)
                KCPlayer.getPlayerMap().put(evt.getUniqueId(), KCPlayer.getWrapper(evt.getUniqueId())); // Load player.
        } catch (Exception e) {
            e.printStackTrace();
            Core.warn("Failed to load " + evt.getName() + "'s player data!");
            evt.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    ChatColor.RED + "There was an error loading your playerdata. Staff have been notified.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST) // Run last.
    public void onJoinResult(AsyncPlayerPreLoginEvent evt) {
        String ip = evt.getAddress().toString().split("/")[1].split(":")[0];
        if (evt.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            KCPlayer.getPlayerMap().remove(evt.getUniqueId()); // Remove their data if they weren't let on.
            Core.alertStaff(ChatColor.RED + evt.getName() + " (" + ChatColor.YELLOW + evt.getAddress().toString()
                    + ChatColor.RED + ") attempted login.");
            return;
        }

        QueryTools.queryData(d -> {
            List<KCPlayer> maybe = d.filter(k -> k.getLastIP().equals(ip)).collect(Collectors.toList());
            boolean banned = maybe.stream().filter(kcPlayer -> false).count() > 0; //TODO Grab real
            Core.alertStaff(evt.getName() + " shares the same IP as " + maybe.stream().map(KCPlayer::getUsername)
                    .collect(Collectors.joining(", ")));
            //TODO: Punish if found.
        });
    }

    @Override
    public void onJoin(Player player) {
        KCPlayer.getWrapper(player).updatePlayer();
    }

    @Override
    public void onQuit(Player p) {
        // Don't use KCPlayer#getWrapper, since we don't want to load the data if it's not there.
        KCPlayer player = KCPlayer.getPlayerMap().get(p.getUniqueId());
        if (player != null)
            player.writeData(); // Save the player's data to disk.
        KCPlayer.getPlayerMap().remove(p.getUniqueId()); // Unload their data from memory.
    }

    @Override
    public void onDisable() {
        Core.logInfo("Saving all player data...");
        saveAllPlayers();
    }
}
