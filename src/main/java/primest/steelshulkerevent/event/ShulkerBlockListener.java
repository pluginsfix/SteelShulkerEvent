package primest.steelshulkerevent.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import primest.steelshulkerevent.manager.EventManager;

public class ShulkerBlockListener
implements Listener {
    private final EventManager manager;

    public ShulkerBlockListener(EventManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!this.manager.isActive()) {
            return;
        }
        Block brokenBlock = e.getBlock();
        Block shulker = this.manager.getShulkerBlock();
        Player player = e.getPlayer();
        if (shulker == null) {
            return;
        }
        if (brokenBlock.equals((Object)shulker)) {
            e.setCancelled(true);
            this.manager.handleBlockBreak(player);
        }
    }
}

