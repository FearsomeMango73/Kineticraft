package net.kineticraft.lostcity.discord;

import com.google.common.base.Predicates;
import lombok.Getter;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.GuildController;
import net.kineticraft.lostcity.config.Configs;
import net.kineticraft.lostcity.mechanics.Mechanic;
import net.kineticraft.lostcity.utils.ServerUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

/**
 * Control the discord bot.
 *
 * Created by Kneesnap on 6/28/2017.
 */
public class DiscordAPI extends Mechanic {

    @Getter private static DiscordBot bot;

    @Override
    public void onEnable() {
        bot = new DiscordBot();
    }

    @Override
    public void onDisable() {
        sendGame("Server shutting down...");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) // Announce chat to discord.
    public void onChat(AsyncPlayerChatEvent evt) {
        sendGame(String.format(evt.getFormat(), "**" + evt.getPlayer().getName() + "**", evt.getMessage()));
    }

    /**
     * Send a message to in-game discord.
     * @param message
     */
    public static void sendGame(String message) {
        sendMessage(DiscordChannel.INGAME, message);
    }

    /**
     * Broadcast a message into a discord channel.
     * @param channel
     * @param message
     */
    public static void sendMessage(DiscordChannel channel, String message) {
        if (isAlive())
            getBot().sendMessage(channel, message);
    }

    /**
     * Get the server this applies to.
     * @return server
     */
    public static Guild getServer() {
        return getBot().getBot().getGuildById(Configs.getMainConfig().getServerId());
    }

    /**
     * Get the server manager API.
     * @return manager
     */
    public static GuildController getManager() {
        return getServer().getController();
    }

    /**
     * Return whether or not the bot exists and is connected, and enabled.
     *
     * @return alive
     */
    public static boolean isAlive() {
        return !ServerUtils.isDevServer() && getBot() != null;
    }

    /**
     * Get a role by the given name.
     * @param roleName
     * @return role
     */
    public static Role getRole(String roleName) {
        List<Role> roles = DiscordAPI.getServer().getRolesByName(roleName, true);
        return roles.isEmpty() ? null : roles.get(0);
    }

    /**
     * Return the bot's user.
     * @return selfUser
     */
    public static SelfUser getUser() {
        return getBot().getBot().getSelfUser();
    }

    /**
     * Get the bot's member.
     * @return member.
     */
    public static Member getMember() {
        return getServer().getMember(getUser());
    }

    /**
     * Does this user have the given role on discord?
     * @param user
     * @param role
     * @return hasRole
     */
    public static boolean hasRole(User user, String role) {
        return isAlive() && getServer().getMember(user).getRoles().contains(getRole(role));
    }

    /**
     * Remove a role from a user on discord.
     * @param user
     * @param role
     */
    public static void removeRole(User user, String role) {
        if (canEdit(user) && hasRole(user, role))
            getManager().removeRolesFromMember(getServer().getMember(user), getRole(role)).queue();
    }

    /**
     * Give a user a role on discord.
     * @param user
     * @param role
     */
    public static void giveRole(User user, String role) {
        if (role == null || !canEdit(user))
            return;
        removeRole(user, role);
        getManager().addRolesToMember(getServer().getMember(user), getRole(role)).queue();
    }

    /**
     * Set if a user has a role on discord.
     * @param user
     * @param role
     * @param has
     */
    public static void setRole(User user, String role, boolean has) {
        if (has) {
            giveRole(user, role);
        } else {
            removeRole(user, role);
        }
    }

    /**
     * Is this user verified on discord?
     * @param user
     * @return verified
     */
    public static boolean isVerified(User user) {
        return hasRole(user, "Verified");
    }

    /**
     * Does our bot have permissions to edit this user?
     * @param user
     * @return perms
     */
    public static boolean canEdit(User user) {
        return isAlive() && getMember().canInteract(getServer().getMember(user));
    }

    /**
     * Set the nickname of a discord user.
     * @param user
     * @param nick
     */
    public static void setNick(User user, String nick) {
        if (canEdit(user))
            getManager().setNickname(getServer().getMember(user), nick).queue();
    }

    /**
     * Remove all roles from this user.
     * @param user
     */
    @SuppressWarnings("Guava")
    public static void clearRoles(User user) {
        if (canEdit(user))
            getServer().getMember(user).getRoles().stream()
                    .filter(Predicates.not(Role::isPublicRole)) // Not @everyone
                    .forEach(r -> removeRole(user, r.getName()));
    }
}
