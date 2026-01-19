package com.can61cebi.foliatimber.protection;

import com.can61cebi.foliatimber.FoliaTimber;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Hook for CoreProtect API integration.
 * Provides block placement history lookup.
 */
public class CoreProtectHook {
    
    private final FoliaTimber plugin;
    private CoreProtectAPI api;
    private boolean enabled = false;
    
    public CoreProtectHook(FoliaTimber plugin) {
        this.plugin = plugin;
        initialize();
    }
    
    /**
     * Initialize CoreProtect API connection.
     */
    private void initialize() {
        Plugin cpPlugin = plugin.getServer().getPluginManager().getPlugin("CoreProtect");
        
        if (cpPlugin == null || !(cpPlugin instanceof CoreProtect)) {
            plugin.getLogger().info("CoreProtect bulunamadı - entegrasyon devre dışı");
            return;
        }
        
        try {
            this.api = ((CoreProtect) cpPlugin).getAPI();
            
            if (api == null || !api.isEnabled()) {
                plugin.getLogger().warning("CoreProtect API etkin değil");
                return;
            }
            
            // Check API version
            if (api.APIVersion() < 9) {
                plugin.getLogger().warning("CoreProtect API sürümü çok eski (gerekli: 9+)");
                return;
            }
            
            this.enabled = true;
            plugin.getLogger().info("CoreProtect entegrasyonu aktif (API v" + api.APIVersion() + ")");
            
        } catch (Exception e) {
            plugin.getLogger().warning("CoreProtect bağlantı hatası: " + e.getMessage());
        }
    }
    
    /**
     * Check if CoreProtect hook is enabled.
     */
    public boolean isEnabled() {
        return enabled && api != null;
    }
    
    /**
     * Get the CoreProtect API instance.
     */
    public CoreProtectAPI getAPI() {
        return api;
    }
    
    /**
     * Check if a block was placed by a player.
     *
     * @param block       The block to check
     * @param lookupDays  Number of days to look back
     * @return true if placed by a player, false otherwise
     */
    public boolean isPlayerPlacedBlock(Block block, int lookupDays) {
        if (!isEnabled()) return false;
        
        try {
            int lookupSeconds = lookupDays * 24 * 60 * 60;
            List<String[]> lookup = api.blockLookup(block, lookupSeconds);
            
            if (lookup == null || lookup.isEmpty()) {
                return false; // No records = natural block
            }
            
            for (String[] result : lookup) {
                CoreProtectAPI.ParseResult parsed = api.parseResult(result);
                
                // Action: 0 = break, 1 = place
                if (parsed.getActionId() == 1) {
                    String player = parsed.getPlayer();
                    // Filter non-player sources (#entity, #tnt, etc.)
                    if (player != null && !player.startsWith("#")) {
                        return true; // Player placed this block
                    }
                }
            }
        } catch (Exception e) {
            // On error, fail safe - treat as player placed
            plugin.getLogger().fine("CoreProtect lookup hatası: " + e.getMessage());
            return true;
        }
        
        return false;
    }
}
