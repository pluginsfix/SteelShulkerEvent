package primest.steelshulkerevent.hologram;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import primest.steelshulkerevent.SteelShulkerEvent;
import primest.steelshulkerevent.util.ColorUtil;

public class ShulkerHologramManager {
    private static Object hologram;
    private static boolean opening;
    private static boolean opened;

    private static boolean isEnabled() {
        return Bukkit.getPluginManager().getPlugin("DecentHolograms") != null;
    }

    public static void spawn(Location loc) {
        if (!ShulkerHologramManager.isEnabled()) {
            return;
        }
        ShulkerHologramManager.remove();
        opening = false;
        opened = false;
        try {
            Class<?> dhApi = Class.forName("eu.decentsoftware.holograms.api.DHAPI");
            String id = "steelshulker_" + UUID.randomUUID();
            Method create = dhApi.getMethod("createHologram", String.class, Location.class);
            hologram = create.invoke(null, id, loc);
            ShulkerHologramManager.update(0, 0);
        }
        catch (Throwable t) {
            Bukkit.getLogger().severe("[SteelShulkerEvent] Failed to spawn hologram");
            t.printStackTrace();
        }
    }

    public static void update(int hp, int maxHp) {
        if (!ShulkerHologramManager.isEnabled() || hologram == null) {
            return;
        }
        ShulkerHologramManager.setLinesFromConfig("hologram.lines", hp, maxHp);
    }

    private static void setLinesFromConfig(String path, int hp, int maxHp) {
        try {
            Class<?> dhApi = Class.forName("eu.decentsoftware.holograms.api.DHAPI");
            Method setLines = dhApi.getMethod("setHologramLines", hologram.getClass(), List.class);
            ArrayList<String> result = new ArrayList<String>();
            for (String s : SteelShulkerEvent.get().getConfig().getStringList(path)) {
                String line = s.replace("%hp%", String.valueOf(hp)).replace("%max%", String.valueOf(maxHp));
                result.add(ColorUtil.color(line));
            }
            if (result.isEmpty()) {
                return;
            }
            setLines.invoke(null, hologram, result);
        }
        catch (Throwable t) {
            Bukkit.getLogger().severe("[SteelShulkerEvent] Failed to update hologram lines: " + path);
            t.printStackTrace();
        }
    }

    public static void remove() {
        if (!ShulkerHologramManager.isEnabled() || hologram == null) {
            return;
        }
        try {
            Method delete = hologram.getClass().getMethod("delete", new Class[0]);
            delete.invoke(hologram, new Object[0]);
        }
        catch (Throwable t) {
            Bukkit.getLogger().severe("[SteelShulkerEvent] Failed to delete hologram");
            t.printStackTrace();
        }
        finally {
            hologram = null;
            opening = false;
            opened = false;
        }
    }

    static {
        opening = false;
        opened = false;
    }
}

