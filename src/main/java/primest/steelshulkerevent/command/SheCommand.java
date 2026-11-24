package primest.steelshulkerevent.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import primest.steelshulkerevent.SteelShulkerEvent;
import primest.steelshulkerevent.gui.LootGUI;

public class SheCommand
implements CommandExecutor {
    private final SteelShulkerEvent plugin;

    public SheCommand(SteelShulkerEvent plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        if (args.length == 0) {
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "start": {
                this.plugin.getEventManager().startEvent();
                break;
            }
            case "stop": {
                this.plugin.getEventManager().stopEvent();
                break;
            }
            case "lootgui": {
                LootGUI.open((Player)sender);
            }
        }
        return true;
    }
}

