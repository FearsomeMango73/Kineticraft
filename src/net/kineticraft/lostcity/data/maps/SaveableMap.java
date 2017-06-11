package net.kineticraft.lostcity.data.maps;

import lombok.Getter;
import net.kineticraft.lostcity.data.JsonData;
import net.kineticraft.lostcity.data.Jsonable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Base of a json saveable map.
 *
 * Created by Kneesnap on 6/10/2017.
 */
@Getter
public abstract class SaveableMap<K, V> implements Jsonable {

    private Map<K, V> map = new HashMap<>();

    public SaveableMap() {

    }

    public SaveableMap(JsonData data) {
        load(data);
    }

    /**
     * Get the element with the specified key.
     * @param key
     * @return
     */
    public V get(K key) {
        return getMap().get(key);
    }

    /**
     * Does this map contain the listed key?
     * @param key
     * @return contains
     */
    public boolean containsKey(K key) {
        return getMap().containsKey(key);
    }

    /**
     * Get a list of keys in this set.
     * @return keys
     */
    public Set<K> keySet() {
        return getMap().keySet();
    }

    /**
     * Get a set of values from this map.
     * @return values
     */
    public Collection<V> valueSet() {
        return getMap().values();
    }

    /**
     * Get an entry set of each pair we have.
     * @return entries
     */
    public Set<Map.Entry<K, V>> entrySet() {
        return getMap().entrySet();
    }

    /**
     * Remove an element from this object.
     * @param key
     * @return valueRemoved
     */
    public V remove(K key) {
        return getMap().remove(key);
    }

    /**
     * Set a value in the json map.
     * @param key
     * @param value
     */
    public void put(K key, V value) {
        getMap().put(key, value);
    }

    /**
     * Return the number of elements in this map.
     * @return size
     */
    public int size() {
        return keySet().size();
    }

    @Override
    public void load(JsonData data) {
        data.keySet().forEach(k -> load(data, k));
    }

    @Override
    public JsonData save() {
        JsonData data = new JsonData();
        getMap().entrySet().stream().forEach(e -> save(data, e.getKey(), e.getValue()));
        return data;
    }


    /**
     * Save an element
     * @param data
     * @param key
     * @param value
     */
    protected abstract void save(JsonData data, K key, V value);

    /**
     * Load an element.
     * @param data
     * @param key
     */
    protected abstract void load(JsonData data,  String key);
}
