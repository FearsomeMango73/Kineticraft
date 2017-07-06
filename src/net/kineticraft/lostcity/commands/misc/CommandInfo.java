package net.kineticraft.lostcity.commands.misc;

import net.kineticraft.lostcity.commands.PlayerCommand;
import net.kineticraft.lostcity.config.Configs;
import net.kineticraft.lostcity.utils.TextBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;

import java.util.stream.Collectors;

/**
 * Command that will display all lines of a config file.
 *
 * Created by Kneesnap on 6/10/2017.
 */
public class CommandInfo extends PlayerCommand {

    private Configs.ConfigType type;

    public CommandInfo(Configs.ConfigType type, String help, String... alias) {
        super("", help, alias);
        this.type = type;
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        sender.sendMessage(Configs.getTextConfig(this.type).getText().create());
    }
}
