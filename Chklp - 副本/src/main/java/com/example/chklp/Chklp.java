package com.example.chklp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Chklp extends JavaPlugin implements Listener {

    private Map<Player, BukkitTask> playerTasks = new HashMap<>();
    private Map<Player, BukkitTask> zombieVirusTasks = new HashMap<>();
    private Random random = new Random();

    @Override
    public void onEnable() {
        // 加载配置文件
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Chklp插件已启用！尸潮将在玩家进入服务器后准备" +
                "！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！" +
                "！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！");
    }

    @Override
    public void onDisable() {
        getLogger().info("Chklp插件已禁用！欢迎下次光临！");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        startZombieWaveTask(player);
    }

    private void startZombieWaveTask(Player player) {
        if (playerTasks.containsKey(player)) {
            playerTasks.get(player).cancel();
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                World world = player.getWorld();
                Location playerLocation = player.getLocation();

                // 发送标题
                player.sendTitle(ChatColor.RED + "尸潮即将来袭！", "", 10, 70, 20);

                // 延迟时间后生成僵尸
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < getConfig().getInt("spawnAmount"); i++) {
                            Location spawnLocation = playerLocation.clone().add(
                                    (Math.random() * getConfig().getInt("spawnRadius")) - (getConfig().getInt("spawnRadius") / 2),
                                    0,
                                    (Math.random() * getConfig().getInt("spawnRadius")) - (getConfig().getInt("spawnRadius") / 2)
                            );
                            Zombie zombie = (Zombie) world.spawnEntity(spawnLocation, EntityType.ZOMBIE);
                            setZombieAttributes(zombie);
                            zombie.setFireTicks(0); // 防止僵尸在白天燃烧
                        }

                        // 生成尸王
                        spawnZombieKing(playerLocation);
                    }
                }.runTaskLater(Chklp.this, getConfig().getInt("delaySeconds") * 20L); // 延迟时间后执行
            }
        }.runTaskTimer(this, 0L, getConfig().getInt("intervalSeconds") * 20L); // 每间隔时间执行一次

        playerTasks.put(player, task);
    }

    private void setZombieAttributes(Zombie zombie) {
        zombie.setCustomName(ChatColor.DARK_RED + getConfig().getString("zombieName"));
        zombie.setCustomNameVisible(true);
        zombie.setMaxHealth(getConfig().getDouble("zombieAttributes.health"));
        zombie.setHealth(getConfig().getDouble("zombieAttributes.health"));
        zombie.getAttribute(org.bukkit.attribute.Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(getConfig().getDouble("zombieAttributes.attackDamage"));
        zombie.getAttribute(org.bukkit.attribute.Attribute.GENERIC_ARMOR).setBaseValue(getConfig().getDouble("zombieAttributes.armor"));
        zombie.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(getConfig().getDouble("zombieAttributes.speed"));

        // 设置僵尸可以破坏方块和门
        if (getConfig().getBoolean("canBreakBlocks")) {
            zombie.setCanPickupItems(false);
            zombie.setRemoveWhenFarAway(false);
            zombie.setCanBreakDoors(true);
            zombie.setAI(true);
        }

        zombie.setFireTicks(0); // 防止僵尸在白天燃烧
    }

    private void spawnZombieKing(Location location) {
        World world = location.getWorld();
        Location spawnLocation = location.clone().add(
                (Math.random() * 20) - 10,
                0,
                (Math.random() * 20) - 10
        );
        Zombie zombieKing = (Zombie) world.spawnEntity(spawnLocation, EntityType.ZOMBIE);
        zombieKing.setCustomName(ChatColor.DARK_RED + getConfig().getString("zombieKing.name"));
        zombieKing.setCustomNameVisible(true);
        zombieKing.setMaxHealth(getConfig().getDouble("zombieKing.health"));
        zombieKing.setHealth(getConfig().getDouble("zombieKing.health"));
        zombieKing.getAttribute(org.bukkit.attribute.Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(getConfig().getDouble("zombieKing.attackDamage"));
        zombieKing.getAttribute(org.bukkit.attribute.Attribute.GENERIC_ARMOR).setBaseValue(getConfig().getDouble("zombieKing.armor"));
        zombieKing.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(getConfig().getDouble("zombieKing.speed"));

        // 给尸王穿上全套钻石装备
        zombieKing.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        zombieKing.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        zombieKing.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        zombieKing.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));

        zombieKing.setFireTicks(0); // 防止尸王在白天燃烧
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntityType() == EntityType.ZOMBIE && event.getEntity().getCustomName() != null && event.getEntity().getCustomName().equals(ChatColor.DARK_RED + getConfig().getString("zombieKing.name"))) {
            // 生成尸剑
            ItemStack zombieSword = new ItemStack(Material.getMaterial(getConfig().getString("zombieKing.dropItem.material")));
            ItemMeta meta = zombieSword.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + getConfig().getString("zombieKing.dropItem.name"));

            // 添加附魔
            List<String> enchantments = getConfig().getStringList("zombieKing.dropItem.enchantments");
            for (String enchantment : enchantments) {
                String[] parts = enchantment.split(":");
                Enchantment ench = Enchantment.getByName(parts[0]);
                int level = Integer.parseInt(parts[1]);
                meta.addEnchant(ench, level, true);
            }

            zombieSword.setItemMeta(meta);
            event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), zombieSword);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Zombie && event.getEntity() instanceof Player) {
            Zombie zombie = (Zombie) event.getDamager();
            Player player = (Player) event.getEntity();

            // 检查僵尸是否具有自定义名称
            if (zombie.getCustomName() != null && zombie.getCustomName().equals(ChatColor.DARK_RED + getConfig().getString("zombieName"))) {
                // 根据配置的概率感染僵尸病毒
                if (random.nextDouble() < getConfig().getDouble("zombieVirus.infectionProbability")) {
                    applyZombieVirus(player);
                }
            } else if (zombie.getCustomName() != null && zombie.getCustomName().equals(ChatColor.DARK_RED + getConfig().getString("zombieKing.name"))) {
                // 尸王攻击玩家时给予凋零效果
                int witherDuration = getConfig().getInt("zombieKing.witherEffectDuration") * 20; // 转换为刻
                int witherLevel = getConfig().getInt("zombieKing.witherEffectLevel");
                PotionEffect witherEffect = new PotionEffect(PotionEffectType.WITHER, witherDuration, witherLevel, false, false, true);
                player.addPotionEffect(witherEffect);

                // 根据配置的概率感染僵尸病毒
                if (random.nextDouble() < getConfig().getDouble("zombieKing.infectionProbability")) {
                    applyZombieVirus(player);
                }
            }
        } else if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();

            // 检查攻击者是否感染了僵尸病毒
            if (hasZombieVirus(damager)) {
                // 根据配置的概率传播僵尸病毒
                if (random.nextDouble() < getConfig().getDouble("infectionSpread.spreadProbability")) {
                    applyZombieVirus(victim);
                }
            }
        }
    }

    private void applyZombieVirus(Player player) {
        // 创建一个自定义的&ldquo;僵尸病毒&rdquo;效果
        int poisonDuration = getConfig().getInt("zombieVirus.poisonDuration") * 20; // 转换为刻
        int poisonLevel = getConfig().getInt("zombieVirus.poisonLevel");
        PotionEffect zombieVirus = new PotionEffect(PotionEffectType.POISON, poisonDuration, poisonLevel, false, false, true);
        player.addPotionEffect(zombieVirus);

        // 发送消息给玩家
        player.sendMessage(ChatColor.RED + "你感染了僵尸病毒！");

        // 启动生命上限减少任务
        startZombieVirusTask(player);

        // 检查是否在白天
        if (player.getWorld().getTime() >= 0 && player.getWorld().getTime() <= 12000) {
            // 触发成就
            triggerAchievement(player, "doomedToDie");

            // 给予虚弱效果
            int weaknessDuration = getConfig().getInt("weaknessEffect.duration") * 20; // 转换为刻
            int weaknessLevel = getConfig().getInt("weaknessEffect.level");
            PotionEffect weaknessEffect = new PotionEffect(PotionEffectType.WEAKNESS, weaknessDuration, weaknessLevel, false, false, true);
            player.addPotionEffect(weaknessEffect);
        }
    }

    private void startZombieVirusTask(Player player) {
        if (zombieVirusTasks.containsKey(player)) {
            zombieVirusTasks.get(player).cancel();
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                double initialMaxHealth = getConfig().getDouble("zombieVirus.initialMaxHealth");
                double healthDecreaseAmount = getConfig().getDouble("zombieVirus.healthDecreaseAmount");

                if (maxHealth > initialMaxHealth) {
                    player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth - healthDecreaseAmount);
                    player.sendMessage(ChatColor.RED + "你的生命上限减少了" + healthDecreaseAmount + "点！");
                } else {
                    this.cancel(); // 生命上限已降至初始值，停止任务
                }
            }
        }.runTaskTimer(this, getConfig().getInt("zombieVirus.healthDecreaseInterval") * 20L, getConfig().getInt("zombieVirus.healthDecreaseInterval") * 20L); // 每隔配置的时间执行一次

        zombieVirusTasks.put(player, task);
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // 获取配置中的去除僵尸病毒的物品
        Material cureItemMaterial = Material.getMaterial(getConfig().getString("cureItem.material"));

        // 检查玩家是否使用了配置中的物品
        if (item.getType() == cureItemMaterial) {
            // 去除僵尸病毒效果
            player.removePotionEffect(PotionEffectType.POISON);

            // 恢复生命上限到默认值（20点）
            player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);

            // 取消生命上限减少任务
            if (zombieVirusTasks.containsKey(player)) {
                zombieVirusTasks.get(player).cancel();
                zombieVirusTasks.remove(player);
            }

            // 发送消息给玩家
            player.sendMessage(ChatColor.GREEN + "你使用了" + item.getType().name() + "，僵尸病毒效果已被去除，生命上限已恢复！");

            // 触发成就
            triggerAchievement(player, "surviveTheCrisis");
        }
    }

    private void triggerAchievement(Player player, String achievementKey) {
        String achievementName = getConfig().getString("achievements." + achievementKey + ".name");
        String achievementColor = getConfig().getString("achievements." + achievementKey + ".color");

        // 发送成就消息给玩家
        player.sendMessage(ChatColor.valueOf(achievementColor) + "成就达成：" + achievementName);
    }

    private boolean hasZombieVirus(Player player) {
        return player.hasPotionEffect(PotionEffectType.POISON);
    }
}