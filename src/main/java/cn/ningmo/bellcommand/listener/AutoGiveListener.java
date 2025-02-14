package cn.ningmo.bellcommand.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import cn.ningmo.bellcommand.BellCommand;
import cn.ningmo.bellcommand.item.CommandItem;
import cn.ningmo.bellcommand.item.CommandItemManager;
import cn.ningmo.bellcommand.utils.ColorUtils;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class AutoGiveListener implements Listener {
    private final BellCommand plugin;
    private final CommandItemManager itemManager;

    public AutoGiveListener(BellCommand plugin) {
        this.plugin = plugin;
        this.itemManager = plugin.getItemManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean("auto-give.enabled") || 
            !plugin.getConfig().getBoolean("auto-give.join")) {
            return;
        }

        Player player = event.getPlayer();
        giveItems(player);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!plugin.getConfig().getBoolean("auto-give.enabled") || 
            !plugin.getConfig().getBoolean("auto-give.respawn")) {
            return;
        }

        Player player = event.getPlayer();
        // 延迟给予物品以确保玩家完全重生
        new BukkitRunnable() {
            @Override
            public void run() {
                giveItems(player);
            }
        }.runTaskLater(plugin, 20L); // 1秒后给予物品
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (!plugin.getConfig().getBoolean("auto-cleanup.enabled")) {
            return;
        }

        ItemStack droppedItem = event.getItemDrop().getItemStack();
        CommandItem commandItem = itemManager.getCommandItem(droppedItem);
        
        if (commandItem != null) {
            int delay = plugin.getConfig().getInt("auto-cleanup.delay", 30) * 20; // 转换为tick
            new BukkitRunnable() {
                @Override
                public void run() {
                    event.getItemDrop().remove();
                    if (plugin.isDebugEnabled()) {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("player", event.getPlayer().getName());
                        placeholders.put("item", commandItem.getId());
                        plugin.getLogger().info(ColorUtils.translateConsoleColors(
                            plugin.getLanguageManager().getMessage("messages.debug.item.removed", placeholders)
                        ));
                    }
                }
            }.runTaskLater(plugin, delay);
        }
    }

    private void giveItems(Player player) {
        if (!plugin.getConfig().getBoolean("auto-give.enabled")) {
            return;
        }

        List<String> itemIds = plugin.getConfig().getStringList("auto-give.items");
        if (itemIds == null || itemIds.isEmpty()) {
            if (plugin.isDebugEnabled()) {
                plugin.getLogger().warning(ColorUtils.translateConsoleColors(
                    plugin.getLanguageManager().getMessage("messages.debug.config.no-items")
                ));
            }
            return;
        }

        for (String itemId : itemIds) {
            CommandItem item = itemManager.getItem(itemId);
            if (item == null) {
                if (plugin.isDebugEnabled()) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("id", itemId);
                    plugin.getLogger().warning(ColorUtils.translateConsoleColors(
                        plugin.getLanguageManager().getMessage("messages.debug.item.not-found", placeholders)
                    ));
                }
                continue;
            }

            // 检查玩家背包中是否已经有这个命令物品
            boolean hasItem = false;
            for (ItemStack invItem : player.getInventory().getContents()) {
                if (invItem != null && itemManager.getCommandItem(invItem) != null &&
                    itemManager.getCommandItem(invItem).getId().equals(itemId)) {
                    hasItem = true;
                    break;
                }
            }

            // 如果玩家没有这个物品，才给予
            if (!hasItem) {
                ItemStack itemStack = item.createItemStack();
                // 检查背包是否已满
                if (player.getInventory().firstEmpty() == -1) {
                    // 如果背包已满，则发送一条消息给玩家
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("item", item.getName());
                    player.sendMessage(ColorUtils.translateConsoleColors(
                        plugin.getLanguageManager().getMessage("messages.inventory-full", placeholders)
                    ));
                    continue;
                }
                player.getInventory().addItem(itemStack);

                if (plugin.isDebugEnabled()) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("player", player.getName());
                    placeholders.put("item", item.getName());
                    plugin.getLogger().info(ColorUtils.translateConsoleColors(
                        plugin.getLanguageManager().getMessage("messages.plugin.auto-give", placeholders)
                    ));
                }
            } else if (plugin.isDebugEnabled()) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", player.getName());
                placeholders.put("item", item.getName());
                plugin.getLogger().info(ColorUtils.translateConsoleColors(
                    plugin.getLanguageManager().getMessage("messages.debug.item.auto-given", placeholders)
                ));
            }
        }
    }
}
