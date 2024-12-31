package cn.ningmo.bellcommand.item;

import cn.ningmo.bellcommand.BellCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CommandItemManager {
    private final BellCommand plugin;
    private final Map<String, CommandItem> items;
    private final Map<String, Map<String, Long>> cooldowns; // playerId -> (itemId -> lastUseTime)
    private int cleanupTaskId = -1;

    public CommandItemManager(BellCommand plugin) {
        this.plugin = plugin;
        this.items = new HashMap<>();
        this.cooldowns = new ConcurrentHashMap<>();
        loadItems();
        startCleanupTask();
    }

    private void loadItems() {
        items.clear();
        ConfigurationSection itemsSection = plugin.getConfig().getConfigurationSection("items");
        if (itemsSection == null) return;

        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("开始加载命令物品...");
        }

        for (String id : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(id);
            if (itemSection != null) {
                try {
                    CommandItem item = new CommandItem(id, itemSection);
                    items.put(id, item);
                    
                    if (plugin.isDebugEnabled()) {
                        plugin.getLogger().info("已加载物品: " + id + " (" + item.getName() + ")");
                    }
                } catch (Exception e) {
                    if (plugin.isDebugEnabled()) {
                        plugin.getLogger().warning("加载物品 " + id + " 时出错: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }

        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("命令物品加载完成，共 " + items.size() + " 个物品");
        }
    }

    public CommandItem getItem(String id) {
        return items.get(id);
    }

    public CommandItem getItem(ItemStack item) {
        if (item == null) return null;
        return items.values().stream()
            .filter(cmdItem -> cmdItem.matches(item))
            .findFirst()
            .orElse(null);
    }

    public Collection<CommandItem> getAllItems() {
        return Collections.unmodifiableCollection(items.values());
    }

    public boolean canUseItem(Player player, CommandItem item) {
        // 基础命令权限检查
        if (!player.hasPermission("bellcommand.clock")) {
            return false;
        }
        
        // 特定物品权限检查
        if (!item.getPermission().isEmpty() && !player.hasPermission(item.getPermission())) {
            return false;
        }
        
        // 通配符权限检查
        if (!player.hasPermission("bellcommand.item.*")) {
            String itemPermission = "bellcommand.item." + item.getId().toLowerCase();
            if (!player.hasPermission(itemPermission)) {
                return false;
            }
        }
        
        return true;
    }

    public boolean isOnCooldown(Player player, CommandItem item) {
        if (item.getCooldown() <= 0) return false;

        Map<String, Long> playerCooldowns = cooldowns.computeIfAbsent(
            player.getUniqueId().toString(), k -> new ConcurrentHashMap<>());
        
        Long lastUse = playerCooldowns.get(item.getId());
        if (lastUse == null) return false;

        long cooldownEnd = lastUse + (item.getCooldown() * 1000L);
        return System.currentTimeMillis() < cooldownEnd;
    }

    public int getRemainingCooldown(Player player, CommandItem item) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId().toString());
        if (playerCooldowns == null) return 0;

        Long lastUse = playerCooldowns.get(item.getId());
        if (lastUse == null) return 0;

        long cooldownEnd = lastUse + (item.getCooldown() * 1000L);
        long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000;
        return remaining > 0 ? (int) remaining : 0;
    }

    public void updateCooldown(Player player, CommandItem item) {
        if (item.getCooldown() <= 0) return;
        
        Map<String, Long> playerCooldowns = cooldowns.computeIfAbsent(
            player.getUniqueId().toString(), k -> new ConcurrentHashMap<>());
        
        playerCooldowns.put(item.getId(), System.currentTimeMillis());
    }

    public void reload() {
        loadItems();
        startCleanupTask();
    }

    public void shutdown() {
        if (cleanupTaskId != -1) {
            plugin.getServer().getScheduler().cancelTask(cleanupTaskId);
            cleanupTaskId = -1;
        }
        cleanupCooldowns();
        items.clear();
        cooldowns.clear();
    }

    public void cleanupCooldowns() {
        long now = System.currentTimeMillis();
        cooldowns.forEach((playerId, playerCooldowns) -> {
            playerCooldowns.entrySet().removeIf(entry -> 
                now >= entry.getValue() + (getItem(entry.getKey()).getCooldown() * 1000L));
        });
        cooldowns.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
    
    private void startCleanupTask() {
        if (cleanupTaskId != -1) {
            plugin.getServer().getScheduler().cancelTask(cleanupTaskId);
        }
        
        cleanupTaskId = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
            plugin, 
            this::cleanupCooldowns, 
            6000L, 
            6000L
        ).getTaskId();
    }

    public void clearCache() {
        items.clear();
        cooldowns.clear();
    }
} 