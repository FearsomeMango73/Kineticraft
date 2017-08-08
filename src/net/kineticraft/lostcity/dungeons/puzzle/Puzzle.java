package net.kineticraft.lostcity.dungeons.puzzle;

import lombok.Getter;
import net.kineticraft.lostcity.Core;
import net.kineticraft.lostcity.dungeons.Dungeon;
import net.kineticraft.lostcity.dungeons.Dungeons;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CommandBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * A base dungeon puzzle.
 * Created by Kneesnap on 7/29/2017.
 */
@Getter
public abstract class Puzzle implements Listener {
    private Location gateLocation;
    private BlockFace gateFace;
    private boolean complete;
    private List<BukkitTask> tasks = new ArrayList<>();
    private Dungeon dungeon;

    private static final Map<Class<? extends Puzzle>, Map<String, Method>> triggers = new HashMap<>();

    public Puzzle(Location place, BlockFace gateFace) {
        this.gateLocation = place;
        this.gateFace = gateFace;
        Bukkit.getPluginManager().registerEvents(this, Core.getInstance());
    }

    /**
     * Set the dungeon this puzzle is active in.
     * @param d
     */
    public void setDungeon(Dungeon d) {
        this.dungeon = d;
        this.gateLocation = fixLocation(this.gateLocation);
    }

    /**
     * Get a location clone with the correct world.
     * @param l
     * @return loc
     */
    protected Location fixLocation(Location l) {
        return getDungeon().fixLocation(l);
    }

    /**
     * Complete this puzzle.
     */
    public void complete() {
        complete = true;

        Bukkit.getScheduler().runTaskLater(Core.getInstance(), () -> { // Cosmetic delay.
            getDungeon().playCutscene(new PuzzleDoorCutscene(getGateLocation(), getGateFace()));
            Bukkit.getScheduler().runTaskLater(Core.getInstance(), this::onComplete, 20L);
        }, 35L);
    }

    /**
     * Called when this puzzle is completed.
     */
    protected void onComplete() {
        removeTasks();
    }

    /**
     * Called when the dungeon is removed.
     */
    public void onDungeonRemove() {
        removeTasks();
        PlayerInteractEvent.getHandlerList().unregister(this);
    }

    protected void removeTasks() {
        getTasks().stream().filter(t -> Bukkit.getScheduler().isCurrentlyRunning(t.getTaskId())
                || Bukkit.getScheduler().isQueued(t.getTaskId())).forEach(BukkitTask::cancel);
        getTasks().clear();
    }

    /**
     * Called when a block is punched.
     * @param bk
     * @param isRightClick
     */
    public void onBlockClick(PlayerInteractEvent evt, Block bk, boolean isRightClick) {

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent evt) {
        if (Dungeons.getDungeon(evt.getPlayer()) != getDungeon())
            return;

        if (evt.hasBlock())
            onBlockClick(evt, evt.getClickedBlock(), evt.getAction() == Action.RIGHT_CLICK_BLOCK);
    }

    /**
     * Register a TimerTask that will be cancelled upon dungeon exit.
     * @param r
     * @param ticks
     * @return task
     */
    protected BukkitTask addTimerTask(Runnable r, long ticks) {
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(Core.getInstance(), r, 0, ticks);
        getTasks().add(task);
        return task;
    }

    /**
     * Execute a trigger for this puzzle.
     *
     * Valid Method Signatures:
     * doStuff()
     * doStuff(CommandBlock)
     * @param trigger
     */
    public void fireTrigger(String trigger, CommandBlock block) {
        if (!triggers.containsKey(getClass())) { // Get and cache the trigger map.
            Map<String, Method> map = new HashMap<>();
            Stream.of(getClass().getDeclaredMethods()).filter(m -> m.isAnnotationPresent(PuzzleTrigger.class))
                    .forEach(m -> map.put(m.getName(), m));
            triggers.put(getClass(), map);
        }

        Map<String, Method> t = triggers.get(getClass());
        if (!t.containsKey(trigger))
            return; // Is this trigger applicable to this puzzle?

        try {
            Method m = t.get(trigger); // Fire the method associated with the trigger.
            PuzzleTrigger pt = m.getAnnotation(PuzzleTrigger.class);
            if (!pt.skipCheck() && !canTrigger())
                return; // If the trigger conditions aren't met, don't execute.

            if (m.getParameterCount() > 0) {
                m.invoke(this, block);
            } else {
                m.invoke(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Core.warn("Failed to execute puzzle trigger '" + trigger + "' in " + getClass().getSimpleName() + ".");
        }
    }

    /**
     * Should puzzle triggers fire right now?
     * @return shouldFire
     */
    protected boolean canTrigger() {
        return !isComplete();
    }
}