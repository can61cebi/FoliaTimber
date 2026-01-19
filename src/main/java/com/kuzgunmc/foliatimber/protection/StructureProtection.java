package com.kuzgunmc.foliatimber.protection;

import com.kuzgunmc.foliatimber.FoliaTimber;
import com.kuzgunmc.foliatimber.config.ConfigManager;
import com.kuzgunmc.foliatimber.util.MaterialUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Main structure protection service.
 * Combines CoreProtect checks with treehouse detection.
 */
public class StructureProtection {
    
    private final FoliaTimber plugin;
    private final ConfigManager config;
    private final CoreProtectHook coreProtect;
    
    // Block types that indicate a treehouse/structure when player-placed
    private static final Set<Material> STRUCTURE_BLOCKS = Set.of(
        // Planks
        Material.OAK_PLANKS, Material.SPRUCE_PLANKS, Material.BIRCH_PLANKS,
        Material.JUNGLE_PLANKS, Material.ACACIA_PLANKS, Material.DARK_OAK_PLANKS,
        Material.MANGROVE_PLANKS, Material.CHERRY_PLANKS, Material.BAMBOO_PLANKS,
        Material.CRIMSON_PLANKS, Material.WARPED_PLANKS,
        // Slabs
        Material.OAK_SLAB, Material.SPRUCE_SLAB, Material.BIRCH_SLAB,
        Material.JUNGLE_SLAB, Material.ACACIA_SLAB, Material.DARK_OAK_SLAB,
        // Stairs
        Material.OAK_STAIRS, Material.SPRUCE_STAIRS, Material.BIRCH_STAIRS,
        Material.JUNGLE_STAIRS, Material.ACACIA_STAIRS, Material.DARK_OAK_STAIRS,
        // Fences
        Material.OAK_FENCE, Material.SPRUCE_FENCE, Material.BIRCH_FENCE,
        Material.JUNGLE_FENCE, Material.ACACIA_FENCE, Material.DARK_OAK_FENCE,
        // Ladders and torches
        Material.LADDER, Material.TORCH, Material.WALL_TORCH,
        // Glass
        Material.GLASS, Material.GLASS_PANE,
        // Doors and trapdoors
        Material.OAK_DOOR, Material.SPRUCE_DOOR, Material.OAK_TRAPDOOR,
        // Chests and crafting
        Material.CHEST, Material.CRAFTING_TABLE, Material.FURNACE,
        // Beds
        Material.RED_BED, Material.WHITE_BED, Material.BLUE_BED,
        // Wool and carpets (any color)
        Material.WHITE_WOOL, Material.WHITE_CARPET
    );
    
    public StructureProtection(FoliaTimber plugin, CoreProtectHook coreProtect) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.coreProtect = coreProtect;
    }
    
    /**
     * Check if a single block was placed by a player.
     * This is a BLOCKING call - runs on async thread.
     */
    public boolean isPlayerPlacedBlock(Block block) {
        if (!config.useCoreProtect() || !coreProtect.isEnabled()) {
            return false;
        }
        
        return coreProtect.isPlayerPlacedBlock(block, config.getCoreProtectLookupDays());
    }
    
    /**
     * Check if any block in a set was placed by a player.
     */
    public boolean anyBlockPlacedByPlayer(Set<Block> blocks) {
        if (!config.useCoreProtect() || !coreProtect.isEnabled()) {
            return false;
        }
        
        for (Block block : blocks) {
            if (isPlayerPlacedBlock(block)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if tree has a treehouse (player-placed structure blocks attached).
     * This should be called from async thread but only uses block locations for CoreProtect query.
     * 
     * @param logs The set of log blocks in the tree
     * @return true if treehouse detected, false otherwise
     */
    public boolean hasTreehouse(Set<Block> logs) {
        if (!config.checkTreehouse()) {
            return false;
        }
        
        if (!config.useCoreProtect() || !coreProtect.isEnabled()) {
            return false;
        }
        
        int radius = config.getTreehouseCheckRadius();
        
        // Check area around each log for player-placed structure blocks
        for (Block log : logs) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        
                        Block nearby = log.getRelative(dx, dy, dz);
                        Material type = nearby.getType();
                        
                        // Skip air, logs, and leaves
                        if (type.isAir()) continue;
                        if (MaterialUtil.isLogBlock(type)) continue;
                        if (MaterialUtil.isLeafBlock(type)) continue;
                        
                        // Check if this is a structure block
                        if (isStructureBlock(type)) {
                            if (isPlayerPlacedBlock(nearby)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Check if a material is a typical structure/building block.
     */
    private boolean isStructureBlock(Material material) {
        // Check explicit list
        if (STRUCTURE_BLOCKS.contains(material)) {
            return true;
        }
        
        // Check by name patterns
        String name = material.name();
        return name.contains("PLANK") ||
               name.contains("SLAB") ||
               name.contains("STAIR") ||
               name.contains("FENCE") ||
               name.contains("DOOR") ||
               name.contains("TRAPDOOR") ||
               name.contains("WOOL") ||
               name.contains("CARPET") ||
               name.contains("BED") ||
               name.contains("GLASS") ||
               name.contains("SIGN") ||
               name.contains("BANNER");
    }
}
