package net.kineticraft.lostcity.data.reflect.behavior;

import net.kineticraft.lostcity.data.JsonData;
import net.kineticraft.lostcity.data.Jsonable;
import net.kineticraft.lostcity.data.maps.SaveableMap;
import net.kineticraft.lostcity.item.display.GUIItem;
import org.bukkit.Material;

import java.lang.reflect.Field;

/**
 * Handles dictionary saving / loading
 * Created by Kneesnap on 7/4/2017.
 */
public class MapStore extends DataStore<SaveableMap> {

    public MapStore() {
        super(SaveableMap.class, "setElement");
    }

    @Override
    protected Class<Jsonable> getSaveArgument() {
        return Jsonable.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SaveableMap getField(JsonData data, String key, Field field) {
        return data.getMap(key, (Class<? extends SaveableMap>) field.getType(), getArgs(field));
    }

    @Override
    public void editItem(GUIItem item, Field f, Jsonable data) {
        item.setIcon(Material.CHEST).addLoreAction("Left", "Edit Values");
        Jsonable j = (Jsonable) get(f, data);
    }
}
