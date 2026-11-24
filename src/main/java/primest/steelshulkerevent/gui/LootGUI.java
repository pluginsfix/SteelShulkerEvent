package primest.steelshulkerevent.gui;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LootGUI {
    private static final Inventory inventory = Bukkit.createInventory(null, (int)54, (String)"\u041b\u0443\u0442 \u0428\u0430\u043b\u043a\u0435\u0440\u0430");

    public static void open(Player player) {
        player.openInventory(inventory);
    }

    public static List<ItemStack> getItems() {
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        for (ItemStack item : inventory.getContents()) {
            if (item == null) continue;
            items.add(item);
        }
        return items;
    }
}

