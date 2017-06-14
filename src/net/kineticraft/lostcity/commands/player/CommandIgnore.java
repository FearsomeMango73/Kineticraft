package net.kineticraft.lostcity.commands.player;

import net.kineticraft.lostcity.EnumRank;
import net.kineticraft.lostcity.commands.CommandType;
import net.kineticraft.lostcity.commands.PlayerCommand;
import net.kineticraft.lostcity.data.wrappers.KCPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Let players ignore other players.
 *
 * Created by Kneesnap on 6/10/2017.
 */
public class CommandIgnore extends PlayerCommand {

    public CommandIgnore() {
        super("<player>", "Block messages from a player", "ignore");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        KCPlayer player = KCPlayer.getWrapper((Player) sender);

        if (player.getIgnored().containsIgnoreCase(args[0])) {
            sender.sendMessage(ChatColor.GRAY + "You are already ignoring this player.");
            return;
        }

        player.getIgnored().add(args[0]);
        sender.sendMessage(ChatColor.GRAY + "You are now ignoring " + ChatColor.GREEN + args[0] + ChatColor.GRAY + ".");
    }
}
