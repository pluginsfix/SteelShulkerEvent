package primest.steelshulkerevent.manager;

import java.util.ArrayList;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import primest.steelshulkerevent.SteelShulkerEvent;
import primest.steelshulkerevent.gui.LootGUI;
import primest.steelshulkerevent.hologram.ShulkerHologramManager;
import primest.steelshulkerevent.util.ColorUtil;
import primest.steelshulkerevent.util.ItemGlow;

public class EventManager {
    private final SteelShulkerEvent plugin;
    private Block shulkerBlock;
    private BossBar bossBar;
    private int hp;
    private final int maxHp;
    private int idleTask = -1;
    private int lootTask = -1;
    private int autoStartTask = -1;
    private boolean finishing = false;
    private long lastMine;

    public EventManager(SteelShulkerEvent plugin) {
        this.plugin = plugin;
        this.maxHp = plugin.getConfig().getInt("settings.max-health");
        this.startAutoEventTask();
    }

    public boolean isActive() {
        return this.shulkerBlock != null;
    }

    public Block getShulkerBlock() {
        return this.shulkerBlock;
    }

    public int getHp() {
        return this.hp;
    }

    public int getMaxHp() {
        return this.maxHp;
    }

    private void startAutoEventTask() {
        long interval = 360000L;
        this.autoStartTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            if (!this.isActive() && Bukkit.getOnlinePlayers().size() > 0) {
                this.startEvent();
            }
        }, 12000L, interval).getTaskId();
    }

    public void startEvent() {
        if (this.isActive()) {
            return;
        }
        Player base = Bukkit.getOnlinePlayers().stream().findAny().orElse(null);
        if (base == null) {
            return;
        }
        World world = base.getWorld();
        Random r = new Random();
        Block spawn = null;
        int minX = this.plugin.getConfig().getInt("settings.min-x", -2000);
        int maxX = this.plugin.getConfig().getInt("settings.max-x", 2000);
        int minZ = this.plugin.getConfig().getInt("settings.min-z", -2000);
        int maxZ = this.plugin.getConfig().getInt("settings.max-z", 2000);
        String worldName = this.plugin.getConfig().getString("settings.world", "world");
        World targetWorld = Bukkit.getWorld((String)worldName);
        if (targetWorld != null) {
            world = targetWorld;
        }
        for (int i = 0; i < 200; ++i) {
            Block place;
            int z;
            int y;
            int x = minX + r.nextInt(maxX - minX + 1);
            Block ground = world.getBlockAt(x, y = world.getHighestBlockYAt(x, z = minZ + r.nextInt(maxZ - minZ + 1)), z);
            if (!ground.getType().isSolid() || ground.isLiquid() || (place = ground.getRelative(0, 1, 0)).getType() != Material.AIR) continue;
            spawn = place;
            break;
        }
        if (spawn == null) {
            Bukkit.broadcastMessage((String)ColorUtil.color("&c\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043d\u0430\u0439\u0442\u0438 \u043c\u0435\u0441\u0442\u043e \u0434\u043b\u044f \u0438\u0432\u0435\u043d\u0442\u0430"));
            return;
        }
        spawn.getChunk().load();
        spawn.setType(Material.PURPLE_SHULKER_BOX, false);
        this.shulkerBlock = spawn;
        this.hp = this.maxHp;
        this.finishing = false;
        this.lastMine = System.currentTimeMillis();
        this.bossBar = Bukkit.createBossBar((String)ColorUtil.color(this.plugin.getConfig().getString("bossbar.title").replace("%hp%", String.valueOf(this.hp)).replace("%max%", String.valueOf(this.maxHp))), (BarColor)BarColor.PURPLE, (BarStyle)BarStyle.SOLID, (BarFlag[])new BarFlag[0]);
        Bukkit.getOnlinePlayers().forEach(arg_0 -> ((BossBar)this.bossBar).addPlayer(arg_0));
        this.updateBossBar();
        ShulkerHologramManager.spawn(this.shulkerBlock.getLocation().add(0.5, 1.8, 0.5));
        ShulkerHologramManager.update(this.hp, this.maxHp);
        this.startIdleTask();
        Location l = this.shulkerBlock.getLocation();
        for (String s : this.plugin.getConfig().getStringList("messages.start")) {
            Bukkit.broadcastMessage((String)ColorUtil.color(s.replace("%x%", String.valueOf(l.getBlockX())).replace("%y%", String.valueOf(l.getBlockY())).replace("%z%", String.valueOf(l.getBlockZ()))));
        }
        this.playSoundToAllPlayers("sounds.start");
        if (this.shulkerBlock != null) {
            Location loc = this.shulkerBlock.getLocation().add(0.5, 0.5, 0.5);
            loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 2.0f, 1.0f);
        }
    }

    public void handleBlockBreak(Player player) {
        if (!this.isActive() || this.finishing || this.shulkerBlock == null) {
            return;
        }
        this.hp = this.hp == 0 ? this.maxHp - 1 : --this.hp;
        this.lastMine = System.currentTimeMillis();
        this.updateBossBar();
        ShulkerHologramManager.update(this.hp, this.maxHp);
        this.playHitSound();
        this.spawnParticleEffect();
        this.checkAbilities();
        if (this.hp <= 0) {
            this.finish();
        }
    }

    private void checkAbilities() {
        if (this.hp == 1500) {
            this.activateAbility1500();
        } else if (this.hp == 1000) {
            this.activateAbility1000();
        } else if (this.hp == 500) {
            this.activateAbility500();
        }
    }

    private void activateAbility1500() {
        Location center = this.shulkerBlock.getLocation();
        String message = this.plugin.getConfig().getString("abilities.1500.message", "&#97E542&ls&#97E542&l\u1d1b&#97E542&l\u1d07&#97E542&l\u1d07&#97E542&l\u029f&#97E542&l\u0280&#97E542&l\u026a&#97E542&ls&#97E542&l\u1d07 &8\u00bb&x&E&3&D&D&D&7 \u043e\u0440\u0435\u0448\u0435\u043a \u0430\u043a\u0442\u0438\u0432\u0438\u0440\u043e\u0432\u0430\u043b \u0441\u043f\u043e\u0441\u043e\u0431\u043d\u043e\u0441\u0442\u044c!");
        Bukkit.broadcastMessage((String)ColorUtil.color(message));
        this.playAbilitySound("abilities.1500.sound", Sound.ENTITY_WITHER_HURT);
        this.spawnAbilityParticles(Particle.SMOKE_LARGE);
        this.spawnAbilityParticles(Particle.CRIT_MAGIC);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().equals((Object)center.getWorld()) || !(player.getLocation().distance(center) <= 20.0)) continue;
            player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 0, true, true));
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 1.0f, 1.0f);
        }
    }

    private void activateAbility1000() {
        Location center = this.shulkerBlock.getLocation();
        String message = this.plugin.getConfig().getString("abilities.1000.message", "&#97E542&ls&#97E542&l\u1d1b&#97E542&l\u1d07&#97E542&l\u1d07&#97E542&l\u029f&#97E542&l\u0280&#97E542&l\u026a&#97E542&ls&#97E542&l\u1d07 &8\u00bb&x&E&3&D&D&D&7 \u043e\u0440\u0435\u0448\u0435\u043a \u0430\u043a\u0442\u0438\u0432\u0438\u0440\u043e\u0432\u0430\u043b \u0441\u043f\u043e\u0441\u043e\u0431\u043d\u043e\u0441\u0442\u044c!");
        Bukkit.broadcastMessage((String)ColorUtil.color(message));
        this.playAbilitySound("abilities.1000.sound", Sound.ENTITY_WITHER_SPAWN);
        this.spawnAbilityParticles(Particle.SPELL_WITCH);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().equals((Object)center.getWorld()) || !(player.getLocation().distance(center) <= 20.0)) continue;
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 400, 2, true, true));
            player.playSound(player.getLocation(), Sound.BLOCK_CONDUIT_AMBIENT, 1.0f, 0.8f);
        }
    }

    private void activateAbility500() {
        Location center = this.shulkerBlock.getLocation();
        String message = this.plugin.getConfig().getString("abilities.500.message", "&#97E542&ls&#97E542&l\u1d1b&#97E542&l\u1d07&#97E542&l\u1d07&#97E542&l\u029f&#97E542&l\u0280&#97E542&l\u026a&#97E542&ls&#97E542&l\u1d07 &8\u00bb&x&E&3&D&D&D&7 \u043e\u0440\u0435\u0448\u0435\u043a \u0430\u043a\u0442\u0438\u0432\u0438\u0440\u043e\u0432\u0430\u043b \u0441\u043f\u043e\u0441\u043e\u0431\u043d\u043e\u0441\u0442\u044c!");
        Bukkit.broadcastMessage((String)ColorUtil.color(message));
        this.playAbilitySound("abilities.500.sound", Sound.ENTITY_WITHER_DEATH);
        this.spawnAbilityParticles(Particle.SPELL_MOB);
        this.spawnAbilityParticles(Particle.SLIME);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().equals((Object)center.getWorld()) || !(player.getLocation().distance(center) <= 20.0)) continue;
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 300, 1, true, true));
            player.playSound(player.getLocation(), Sound.ENTITY_WITCH_AMBIENT, 1.0f, 0.8f);
        }
    }

    private void playAbilitySound(String configPath, Sound defaultSound) {
        String soundName = this.plugin.getConfig().getString(configPath);
        Sound sound = defaultSound;
        if (soundName != null && !soundName.isEmpty()) {
            try {
                sound = Sound.valueOf((String)soundName.toUpperCase());
            }
            catch (IllegalArgumentException e) {
                this.plugin.getLogger().warning("\u041d\u0435\u0432\u0435\u0440\u043d\u043e\u0435 \u0438\u043c\u044f \u0437\u0432\u0443\u043a\u0430: " + soundName);
            }
        }
        Location loc = this.shulkerBlock.getLocation().add(0.5, 0.5, 0.5);
        loc.getWorld().playSound(loc, sound, 2.0f, 1.0f);
    }

    private void spawnAbilityParticles(Particle particle) {
        Location center = this.shulkerBlock.getLocation().add(0.5, 0.5, 0.5);
        Random rand = new Random();
        for (int i = 0; i < 50; ++i) {
            double x = center.getX() + (rand.nextDouble() - 0.5) * 10.0;
            double y = center.getY() + rand.nextDouble() * 5.0;
            double z = center.getZ() + (rand.nextDouble() - 0.5) * 10.0;
            Location particleLoc = new Location(center.getWorld(), x, y, z);
            center.getWorld().spawnParticle(particle, particleLoc, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private void finish() {
        this.finishing = true;
        final ArrayList<ItemStack> loot = new ArrayList<ItemStack>();
        for (ItemStack i : LootGUI.getItems()) {
            loot.add(i.clone());
        }
        if (this.shulkerBlock == null) {
            this.cleanup();
            return;
        }
        final Location d = this.shulkerBlock.getLocation().add(0.5, 1.0, 0.5);
        this.lootTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, new Runnable(){
            int i = 0;

            @Override
            public void run() {
                if (this.i >= loot.size()) {
                    EventManager.this.stopEvent();
                    Bukkit.getScheduler().cancelTask(EventManager.this.lootTask);
                    return;
                }
                Item it = d.getWorld().dropItemNaturally(d, (ItemStack)loot.get(this.i++));
                Random rand = new Random();
                double angle = rand.nextDouble() * 2.0 * Math.PI;
                double distance = 2.0 + rand.nextDouble() * 5.0;
                double x = Math.cos(angle) * distance;
                double z = Math.sin(angle) * distance;
                it.setVelocity(new Vector(x * 0.1, 0.8 + rand.nextDouble() * 0.4, z * 0.1));
                ItemGlow.makeRed(it);
            }
        }, 0L, 10L).getTaskId();
    }

    public void stopEvent() {
        if (this.shulkerBlock != null) {
            Location l = this.shulkerBlock.getLocation();
            for (String s : this.plugin.getConfig().getStringList("messages.stop")) {
                Bukkit.broadcastMessage((String)ColorUtil.color(s.replace("%x%", String.valueOf(l.getBlockX())).replace("%y%", String.valueOf(l.getBlockY())).replace("%z%", String.valueOf(l.getBlockZ()))));
            }
            this.playSoundToAllPlayers("sounds.stop");
            Location loc = this.shulkerBlock.getLocation().add(0.5, 0.5, 0.5);
            loc.getWorld().playSound(loc, Sound.ENTITY_VILLAGER_NO, 2.0f, 1.0f);
        }
        this.cleanup();
    }

    private void startIdleTask() {
        this.idleTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            int autoStopSeconds;
            if (!this.isActive() || this.shulkerBlock == null) {
                return;
            }
            long currentTime = System.currentTimeMillis();
            long idleTime = currentTime - this.lastMine;
            if (idleTime >= (long)(autoStopSeconds = this.plugin.getConfig().getInt("settings.auto-stop-seconds", 180)) * 1000L) {
                this.autoRemoveEvent();
            }
        }, 20L, 20L).getTaskId();
    }

    private void autoRemoveEvent() {
        if (this.shulkerBlock == null) {
            return;
        }
        Location l = this.shulkerBlock.getLocation();
        for (String s : this.plugin.getConfig().getStringList("messages.auto-remove")) {
            Bukkit.broadcastMessage((String)ColorUtil.color(s.replace("%x%", String.valueOf(l.getBlockX())).replace("%y%", String.valueOf(l.getBlockY())).replace("%z%", String.valueOf(l.getBlockZ()))));
        }
        this.playSoundToAllPlayers("sounds.auto-remove");
        this.cleanup();
    }

    private void updateBossBar() {
        if (this.bossBar == null) {
            return;
        }
        if (this.maxHp == 0) {
            return;
        }
        double progress = Math.max(0.0, (double)this.hp / (double)this.maxHp);
        this.bossBar.setProgress(progress);
        this.bossBar.setTitle(ColorUtil.color(this.plugin.getConfig().getString("bossbar.title").replace("%hp%", String.valueOf(this.hp)).replace("%max%", String.valueOf(this.maxHp))));
    }

    private void playSoundToAllPlayers(String configPath) {
        String soundName = this.plugin.getConfig().getString(configPath);
        if (soundName == null || soundName.isEmpty()) {
            return;
        }
        try {
            Sound sound = Sound.valueOf((String)soundName.toUpperCase());
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            }
        }
        catch (IllegalArgumentException e) {
            this.plugin.getLogger().warning("\u041d\u0435\u0432\u0435\u0440\u043d\u043e\u0435 \u0438\u043c\u044f \u0437\u0432\u0443\u043a\u0430: " + soundName);
        }
    }

    private void playHitSound() {
        if (this.shulkerBlock == null) {
            return;
        }
        String soundName = this.plugin.getConfig().getString("sounds.hit", "BLOCK_ANVIL_HIT");
        try {
            Sound sound = Sound.valueOf((String)soundName.toUpperCase());
            Location loc = this.shulkerBlock.getLocation().add(0.5, 0.5, 0.5);
            loc.getWorld().playSound(loc, sound, 0.5f, 1.0f);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void spawnParticleEffect() {
        if (this.shulkerBlock == null) {
            return;
        }
        try {
            Location loc = this.shulkerBlock.getLocation().add(0.5, 0.5, 0.5);
            loc.getWorld().spawnParticle(Particle.CRIT, loc, 5, 0.3, 0.3, 0.3, 0.0);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void cleanup() {
        if (this.shulkerBlock != null) {
            try {
                this.shulkerBlock.setType(Material.AIR);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (this.bossBar != null) {
            this.bossBar.removeAll();
        }
        ShulkerHologramManager.remove();
        if (this.idleTask != -1) {
            try {
                Bukkit.getScheduler().cancelTask(this.idleTask);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (this.lootTask != -1) {
            try {
                Bukkit.getScheduler().cancelTask(this.lootTask);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        this.shulkerBlock = null;
        this.finishing = false;
        this.idleTask = -1;
        this.lootTask = -1;
    }

    public void disable() {
        if (this.autoStartTask != -1) {
            try {
                Bukkit.getScheduler().cancelTask(this.autoStartTask);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        this.cleanup();
    }
}

