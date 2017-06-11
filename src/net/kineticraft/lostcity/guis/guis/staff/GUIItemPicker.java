package net.kineticraft.lostcity.guis.guis.staff;

import net.kineticraft.lostcity.guis.PagedGUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;

/**
 * A GUI that allows selecting of materials.
 *
 * Created by Kneesnap on 6/9/2017.
 */
public class GUIItemPicker extends PagedGUI {

    private Consumer<ItemStack> onPick;
    private List<ItemStack> items;

    public GUIItemPicker(Player player, List<ItemStack> items, Consumer<ItemStack> onPick) {
        super(player, "Item Selector", fitSize(items));
        this.onPick = onPick;
        this.items = items;
        markSub();
    }

    @Override
    public void addItems() {
        for (ItemStack i : items)
            addItem(i).anyClick(e -> {
                if (onPick != null)
                    onPick.accept(i);
                openPrevious();
            }).addLore("Click here to choose this item.");

        super.addItems();
    }
}
