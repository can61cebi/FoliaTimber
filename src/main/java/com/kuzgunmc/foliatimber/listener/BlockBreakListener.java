package com.kuzgunmc.foliatimber.listener;

import com.kuzgunmc.foliatimber.FoliaTimber;
import com.kuzgunmc.foliatimber.config.ConfigManager;
import com.kuzgunmc.foliatimber.tree.TreeChopper;
import com.kuzgunmc.foliatimber.tree.TreeData;
import com.kuzgunmc.foliatimber.util.MaterialUtil;
import com.kuzgunmc.foliatimber.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles block break events for timber functionality.
 * All block operations happen on the region thread for Folia safety.
 */
public class BlockBreakListener implements Listener {
    
    private final FoliaTimber plugin;
    private final ConfigManager config;
    
    // Prevents double-processing of blocks
    private final Set<UUID> processingPlayers = ConcurrentHashMap.newKeySet();
    
    // Player toggle states (true = enabled)
    private final ConcurrentHashMap<UUID, Boolean> playerStates = new ConcurrentHashMap<>();
    
    public BlockBreakListener(FoliaTimber plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        // Basic checks
        if (!config.isEnabled()) return;
        if (!player.hasPermission("foliatimber.use")) return;
        
        Material blockType = block.getType();
        if (!MaterialUtil.isLogBlock(blockType)) return;
        
        
        // Check if player has timber enabled
        if (!isTimberEnabled(player)) {
            return;
        }
        
        // Axe check
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (config.requireAxe() && !MaterialUtil.isAxe(tool.getType())) {
            return;
        }
        
        // Sneak check
        if (config.requireSneak() && !player.isSneaking()) {
            return;
        }
        
        // Prevent double processing
        if (!processingPlayers.add(player.getUniqueId())) {
            return;
        }
        
        // FOLIA SAFE: All checks and tree scanning done on the REGION THREAD (synchronously)
        // Only CoreProtect database queries will be done async
        
        Location blockLocation = block.getLocation();
        Material logType = blockType;
        
        
        // 1. Scan tree SYNCHRONOUSLY (on region thread - this is safe)
        TreeData treeData = plugin.getTreeDetector().scanTree(block, logType);
        if (treeData.failReason() != null) {
        }
        
        // 2. Check if natural tree
        if (!treeData.isNaturalTree()) {
            processingPlayers.remove(player.getUniqueId());
            return;
        }
        
        // 3. If CoreProtect is enabled and player doesn't have bypass, check async
        boolean hasBypass = player.hasPermission("foliatimber.bypass");
        boolean useCoreProtect = config.useCoreProtect() && plugin.getCoreProtectHook().isEnabled();
        
        if (!hasBypass && useCoreProtect) {
            
            // Run CoreProtect queries async, then come back to region thread
            plugin.runAsync(() -> {
                try {
                    // Check if the starting block is player-placed
                    boolean isPlayerPlaced = plugin.getStructureProtection().isPlayerPlacedBlock(block);
                    
                    if (isPlayerPlaced) {
                        plugin.runAtLocation(blockLocation, () -> {
                            MessageUtil.send(player, config.getPrefixedMessage("structure-protected"));
                        });
                        return;
                    }
                    
                    // Check if any log in tree is player-placed
                    boolean anyPlayerPlaced = plugin.getStructureProtection().anyBlockPlacedByPlayer(treeData.logs());
                    
                    if (anyPlayerPlaced) {
                        plugin.runAtLocation(blockLocation, () -> {
                            MessageUtil.send(player, config.getPrefixedMessage("structure-protected"));
                        });
                        return;
                    }
                    
                    // Check for treehouse (player-placed structure blocks attached to tree)
                    boolean hasTreehouse = plugin.getStructureProtection().hasTreehouse(treeData.logs());
                    
                    if (hasTreehouse) {
                        plugin.runAtLocation(blockLocation, () -> {
                            MessageUtil.send(player, config.getPrefixedMessage("treehouse-protected"));
                        });
                        return;
                    }
                    
                    // All checks passed, start chopping on region thread
                    plugin.runAtLocation(blockLocation, () -> {
                        TreeChopper chopper = new TreeChopper(plugin, player, treeData, tool);
                        chopper.startChopping();
                    });
                    
                } finally {
                    processingPlayers.remove(player.getUniqueId());
                }
            });
        } else {
            // No CoreProtect check needed, start chopping immediately
            TreeChopper chopper = new TreeChopper(plugin, player, treeData, tool);
            chopper.startChopping();
            processingPlayers.remove(player.getUniqueId());
        }
    }
    
    /**
     * Check if timber is enabled for a player.
     */
    public boolean isTimberEnabled(Player player) {
        return playerStates.getOrDefault(player.getUniqueId(), config.isDefaultEnabled());
    }
    
    /**
     * Toggle timber for a player.
     * @return New state (true = enabled)
     */
    public boolean toggleTimber(Player player) {
        boolean current = isTimberEnabled(player);
        boolean newState = !current;
        playerStates.put(player.getUniqueId(), newState);
        return newState;
    }
    
    /**
     * Set timber state for a player.
     */
    public void setTimberEnabled(Player player, boolean enabled) {
        playerStates.put(player.getUniqueId(), enabled);
    }
}
