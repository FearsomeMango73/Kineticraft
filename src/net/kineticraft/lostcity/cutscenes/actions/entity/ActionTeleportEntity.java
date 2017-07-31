package net.kineticraft.lostcity.cutscenes.actions.entity;

import net.kineticraft.lostcity.cutscenes.annotations.ActionData;
import net.kineticraft.lostcity.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;

/**
 * Teleport an entity.
 * Created by Kneesnap on 7/22/2017.
 */
@ActionData(Material.ENDER_PEARL)
public class ActionTeleportEntity extends ActionEntity {
    private Location location = null;

    @Override
    public void execute() {
        getEntity().teleport(fixLocation(location));
    }

    @Override
    public String toString() {
        return Utils.toCleanString(location) + super.toString();
    }
}