package net.kineticraft.lostcity.utils;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kineticraft.lostcity.Core;
import net.kineticraft.lostcity.data.JsonData;
import net.kineticraft.lostcity.data.lists.JsonList;
import net.kineticraft.lostcity.data.Jsonable;
import net.kineticraft.lostcity.data.wrappers.KCPlayer;
import net.kineticraft.lostcity.mechanics.MetadataManager;
import net.kineticraft.lostcity.mechanics.MetadataManager.Metadata;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utils - Contains basic static utilties.
 * Created by Kneesnap on 5/29/2017.
 */
public class Utils {


    /**
     * Gets an enum value from the given class. Returns null if not found.
     * @param value
     * @param clazz
     */
    public static <T extends Enum<T>> T getEnum(String value, Class<T> clazz) {
        return getEnum(value, clazz, null);
    }

    /**
     * Gets an enum value, falling back on a default value.
     * @param value
     * @param defaultValue
     */
    public static <T extends Enum<T>> T getEnum(String value, T defaultValue) {
        return getEnum(value, (Class<T>) defaultValue.getClass(), defaultValue);
    }

    public static <T extends Enum<T>> T getEnum(String value, Class<T> clazz, T defaultValue) {
        try {
            return (T) clazz.getMethod("valueOf", String.class).invoke(null, value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Capitalize every letter after a space.
     * @param sentence
     * @return
     */
    public static String capitalize(String sentence) {
        String[] split = sentence.replaceAll("_", " ").split(" ");
        List<String> out = new ArrayList<>();
        for (String s : split)
            out.add(s.length() > 0 ? s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase() : "");
        return String.join(" ", out);
    }

    /**
     * Joins a string together by the given string.
     * @param join
     * @param values
     * @param display
     * @return
     */
    public static <T> String join(String join, T[] values, Displayer<T> display) {
        return join(join, Arrays.asList(values), display);
    }

    /**
     * Joins a string together by the delimeter.
     * @param join
     * @param values
     * @param displayer
     * @param <T>
     * @return
     */
    public static <T> String join(String join, Iterable<T> values, Displayer<T> displayer) {
        String res = "";
        for (T val : values)
            res += (res.length() > 0 ? join : "") + displayer.getDisplay(val);
        return res;
    }

    /**
     * Teleport the player to the specified location with cooldowns applied.
     * @param player
     * @param locationDescription
     * @param location
     */
    public static void teleport(Player player, String locationDescription, Location location) {
        if (MetadataManager.hasMetadata(player, Metadata.TELEPORTING)) {
            player.sendMessage(ChatColor.RED + "Please wait until your current teleport finishes.");
            return;
        }
        KCPlayer p = KCPlayer.getWrapper(player);

        double lastDamage = player.getLastDamage();
        final Location startLocation = player.getLocation().clone();
        final BukkitTask[] tpTask = new BukkitTask[1]; // Have to use an array here to store a value, as otherwise it can't be final.
        // Must be final to work in the bukkit scheduler.
        int[] tpTime = new int[] {p.getTemporaryRank().getTpTime()};

        MetadataManager.setMetadata(player, Metadata.TELEPORTING, true);
        player.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1F, 1F);
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * (tpTime[0] + 4), 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * (tpTime[0] + 4), 2));
        player.sendMessage(ChatColor.BOLD + "Teleport: " + ChatColor.WHITE + ChatColor.UNDERLINE + locationDescription);

        //TODO: Verify destination is safe)
        tpTask[0] = Bukkit.getScheduler().runTaskTimer(Core.getInstance(), () -> {
                boolean complete = tpTime[0] <= -1 || !player.isOnline();
                if (complete || player.getLocation().distanceSquared(startLocation) >= 3.5 || player.getLastDamage() < lastDamage) {
                    if (!complete) {
                        player.removePotionEffect(PotionEffectType.CONFUSION);
                        player.removePotionEffect(PotionEffectType.BLINDNESS);
                        player.sendMessage(ChatColor.RED + "Teleport cancelled.");
                        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1F, 2F);
                    }
                    tpTask[0].cancel();
                    MetadataManager.removeMetadata(player, Metadata.TELEPORTING);
                    return;
                }

                if (tpTime[0] > 0) {
                    player.sendMessage(ChatColor.WHITE + "Teleporting... " + ChatColor.UNDERLINE + tpTime[0] + "s");
                } else {
                    player.teleport(location.add(0, 2, 0));
                    player.playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 1F, 1.333F);
                }

                tpTime[0]--;
        }, 0L, 20L);
    }

    public interface Displayer<T> {
        public String getDisplay(T val);
    }

    /**
     * Turn milliseconds into a user friendly string.
     * @param time
     * @return
     */
    public static String formatTime(long time) {
        time /= 1000;
        String formatted = "";
        for (int i = 0; i < TimeInterval.values().length; i++) {
            TimeInterval iv = TimeInterval.values()[TimeInterval.values().length - i - 1];
            if (time >= iv.getInterval()) {
                int temp = (int) (time - (time % iv.getInterval()));
                int add = temp / iv.getInterval();
                formatted += " " + add + iv.getSuffix() + (add > 1 && iv != TimeInterval.SECOND ? "s" : "");
                time -= temp;
            }
        }
        return formatted.equals("") ? "" : formatted.substring(1);
    }

    @AllArgsConstructor @Getter
    private enum TimeInterval {
        SECOND("s", 1),
        MINUTE("min", 60 * SECOND.getInterval()),
        HOUR("hr", 60 * MINUTE.getInterval()),
        DAY("day", 24 * HOUR.getInterval()),
        MONTH("month", 30 * DAY.getInterval()),
        YEAR("yr", 365 * DAY.getInterval());

        private String suffix;
        private int interval;
    }

    /**
     * Convert a location into a friendly string.
     * @param location
     * @return
     */
    public static String toString(Location location) {
       return "[" + location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "]";
    }

    /**
     * Give an item to a player. If their inventory is full, it drops it at their feet.
     * @param player
     * @param itemStack
     */
    public static void giveItem(Player player, ItemStack itemStack) {
        if (player.getInventory().firstEmpty() > -1) {
            player.getInventory().addItem(itemStack);
        } else {
            player.getWorld().dropItem(player.getLocation(), itemStack);
            player.sendMessage(ChatColor.RED + "Your inventory was full, so you dropped the item.");
        }
    }

    /**
     * Construct an object from JSON.
     * @param type
     * @param object
     * @return Constructed object
     */
    public static <T extends Jsonable> T fromJson(Class<T> type, JsonObject object) {
        return ReflectionUtil.construct(type, new JsonData(object));
    }

    /**
     * Gets a players username by their uuid. Offline safe.
     * Returns null if this player has never joined.
     * @param uuid
     * @return name
     */
    public static String getPlayerName(UUID uuid) {
        return KCPlayer.isWrapper(uuid) ? KCPlayer.getWrapper(uuid).getUsername() : null;
    }

    /**
     * Return the display name of an ItemStack.
     * @param itemStack
     * @return Display Name
     */
    public static String getItemName(ItemStack itemStack) {
        return itemStack.getItemMeta().hasDisplayName() ? itemStack.getItemMeta().getDisplayName()
                : capitalize(itemStack.getType().name().replaceAll("_", " "));
    }

    /**
     * Get a random number between the given range.
     * @param min
     * @param max
     * @return int
     */
    public static int randInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(max - min) + min;
    }

    /**
     * Get a random element from an array.
     * @param arr
     * @param <T>
     * @return element
     */
    public static <T> T randElement(T[] arr) {
        return randElement(Arrays.asList(arr));
    }

    /**
     * Get a random element from a json list
     * @param list
     * @param <T>
     * @return rand
     */
    public static <T extends Jsonable> T randElement(JsonList<T> list) {
        return randElement(list.getValues());
    }

    /**
     * Get a random element from a list.
     * @param iterable
     * @param <T>
     * @return element
     */
    public static <T> T randElement(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list.get(nextInt(list.size()));
    }

    /**
     * Gets a random number between 0 and max - 1
     * @param max
     * @return rand
     */
    public static int nextInt(int max) {
        return randInt(0, max - 1);
    }

    /**
     * Check if a random chance succeeds.
     * @param chance
     * @return success
     */
    public static boolean randChance(int chance) {
        return nextInt(chance) == 0;
    }

    /**
     * Replaces an existing itemstack in a player's inventory with a new one.
     * @param player
     * @param original
     * @param newItem
     */
    public static void replaceItem(Player player, ItemStack original, ItemStack newItem) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            if (original.equals(player.getInventory().getItem(i))) {
                player.getInventory().setItem(i, newItem);
                player.updateInventory();
                return;
            }
        }

        Core.warn("Failed to replace " + Utils.getItemName(original) + " in " + player.getName() + "'s inventory.");
    }

    /**
     * Shave the first element of an array
     * @param array
     * @param <T>
     * @return shavedArray
     */
    public static <T> T[] shift(T[] array) {
        T[] ret = (T[]) new Object[array.length - 1];
        for (int i = 0; i < ret.length; i++)
            ret[i] = array[i + 1];
        return ret;
    }

    /**
     * Get the colored name of this CommandSender
     * @param sender
     * @return name
     */
    public static String getSenderName(CommandSender sender) {
        return sender instanceof Player ? KCPlayer.getWrapper((Player) sender).getColoredName()
                : ChatColor.YELLOW + sender.getName();
    }

    /**
     * Use an item and decrement its amount.
     * @param item
     */
    public static void useItem(ItemStack item) {
        item.setAmount(item.getAmount() - 1);
        if (item.getAmount() <= 0)
            item.setType(Material.AIR);
    }
}
