package primest.steelshulkerevent.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import primest.steelshulkerevent.manager.EventManager;

public class ShulkerInteractListener
implements Listener {
    private final EventManager manager;

    public ShulkerInteractListener(EventManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!this.manager.isActive()) {
            return;
        }
        if (!e.getClickedBlock().equals((Object)this.manager.getShulkerBlock())) {
            return;
        }
        e.setCancelled(true);
    }
}

