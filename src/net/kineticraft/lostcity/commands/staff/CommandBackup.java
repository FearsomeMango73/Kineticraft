package net.kineticraft.lostcity.commands.staff;

import net.kineticraft.lostcity.Core;
import net.kineticraft.lostcity.EnumRank;
import net.kineticraft.lostcity.commands.StaffCommand;
import org.bukkit.command.CommandSender;

/**
 * Backup the server.
 *
 * Created by Kneesnap on 6/14/2017.
 */
public class CommandBackup extends StaffCommand {
    public CommandBackup() {
        super(EnumRank.ADMIN, false, "", "Take a backup of the server.", "backup");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        Core.takeBackup();
    }
}
