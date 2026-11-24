package primest.steelshulkerevent;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import primest.steelshulkerevent.command.SheCommand;
import primest.steelshulkerevent.event.ShulkerBlockListener;
import primest.steelshulkerevent.event.ShulkerInteractListener;
import primest.steelshulkerevent.manager.EventManager;

public class SteelShulkerEvent
extends JavaPlugin {
    private static SteelShulkerEvent instance;
    private EventManager eventManager;

    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        this.eventManager = new EventManager(this);
        this.getCommand("she").setExecutor((CommandExecutor)new SheCommand(this));
        Bukkit.getPluginManager().registerEvents((Listener)new ShulkerBlockListener(this.eventManager), (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new ShulkerInteractListener(this.eventManager), (Plugin)this);
        if (Bukkit.getPluginManager().getPlugin("DecentHolograms") != null) {
            this.getLogger().info("DecentHolograms \u043d\u0430\u0439\u0434\u0435\u043d, \u0433\u043e\u043b\u043e\u0433\u0440\u0430\u043c\u043c\u044b \u0431\u0443\u0434\u0443\u0442 \u0440\u0430\u0431\u043e\u0442\u0430\u0442\u044c!");
        } else {
            this.getLogger().warning("DecentHolograms \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d! \u0413\u043e\u043b\u043e\u0433\u0440\u0430\u043c\u043c\u044b \u043d\u0435 \u0431\u0443\u0434\u0443\u0442 \u0440\u0430\u0431\u043e\u0442\u0430\u0442\u044c!");
        }
    }

    public void onDisable() {
        this.getLogger().info("\u041e\u0442\u043a\u043b\u044e\u0447\u0430\u0435\u043c SteelShulkerEvent...");
        if (this.eventManager != null) {
            this.eventManager.disable();
        }
        this.getLogger().info("SteelShulkerEvent \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d!");
    }

    public static SteelShulkerEvent get() {
        return instance;
    }

    public EventManager getEventManager() {
        return this.eventManager;
    }
}

