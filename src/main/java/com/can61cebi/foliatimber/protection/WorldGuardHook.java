package com.can61cebi.foliatimber.protection;

import com.can61cebi.foliatimber.FoliaTimber;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Hook for WorldGuard API integration.
 * Provides region permission checking.
 */
public class WorldGuardHook {
    
    private final FoliaTimber plugin;
    private boolean enabled = false;
    
    public WorldGuardHook(FoliaTimber plugin) {
        this.plugin = plugin;
        initialize();
    }
    
    /**
     * Initialize WorldGuard API connection.
     */
    private void initialize() {
        Plugin wgPlugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        
        if (wgPlugin == null) {
            plugin.getLogger().info("WorldGuard bulunamadı - entegrasyon devre dışı");
            return;
        }
        
        try {
            // Try to access WorldGuard API to verify it works
            WorldGuard.getInstance().getPlatform().getRegionContainer();
            this.enabled = true;
            plugin.getLogger().info("WorldGuard entegrasyonu aktif");
        } catch (Exception e) {
            plugin.getLogger().warning("WorldGuard bağlantı hatası: " + e.getMessage());
        }
    }
    
    /**
     * Check if WorldGuard hook is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Check if a player can chop a tree at the given block location.
     *
     * @param player The player
     * @param block  The block location to check
     * @return true if allowed, false otherwise
     */
    public boolean canChopTree(Player player, Block block) {
        // If WorldGuard is not enabled, allow
        if (!isEnabled()) {
            return true;
        }
        
        try {
            RegionContainer container = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer();
            
            if (container == null) {
                return true;
            }
            
            RegionQuery query = container.createQuery();
            com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(block.getLocation());
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            
            // Use testBuild which properly checks if player can modify blocks
            // This respects region membership, bypass permissions, etc.
            boolean canBuild = query.testBuild(loc, localPlayer);
            
            if (!canBuild) {
                plugin.getLogger().fine("WorldGuard denied build for " + player.getName() + " at " + block.getLocation());
            }
            
            return canBuild;
            
        } catch (Exception e) {
            plugin.getLogger().warning("WorldGuard check error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            // On error, allow to prevent blocking legitimate use
            return true;
        }
    }
    
    /**
     * Check if any blocks in the tree are in a protected region.
     *
     * @param player The player
     * @param blocks The blocks to check
     * @return true if all blocks are allowed, false if any are protected
     */
    public boolean canChopAllBlocks(Player player, Iterable<Block> blocks) {
        if (!isEnabled()) return true;
        
        for (Block block : blocks) {
            if (!canChopTree(player, block)) {
                return false;
            }
        }
        return true;
    }
}
