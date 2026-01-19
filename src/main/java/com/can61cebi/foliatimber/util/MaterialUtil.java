package com.can61cebi.foliatimber.util;

import org.bukkit.Material;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for material-related operations.
 * Contains all log/leaf mappings for Minecraft 1.21.
 */
public final class MaterialUtil {
    
    private MaterialUtil() {}
    
    // All log types in Minecraft 1.21
    public static final Set<Material> LOG_MATERIALS = EnumSet.of(
        // Overworld Logs
        Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG,
        Material.ACACIA_LOG, Material.DARK_OAK_LOG, Material.MANGROVE_LOG,
        Material.CHERRY_LOG, Material.PALE_OAK_LOG,
        // Nether Stems
        Material.CRIMSON_STEM, Material.WARPED_STEM,
        // Stripped Variants
        Material.STRIPPED_OAK_LOG, Material.STRIPPED_SPRUCE_LOG, Material.STRIPPED_BIRCH_LOG,
        Material.STRIPPED_JUNGLE_LOG, Material.STRIPPED_ACACIA_LOG, Material.STRIPPED_DARK_OAK_LOG,
        Material.STRIPPED_MANGROVE_LOG, Material.STRIPPED_CHERRY_LOG, Material.STRIPPED_PALE_OAK_LOG,
        Material.STRIPPED_CRIMSON_STEM, Material.STRIPPED_WARPED_STEM,
        // Wood Blocks (6-sided bark)
        Material.OAK_WOOD, Material.SPRUCE_WOOD, Material.BIRCH_WOOD, Material.JUNGLE_WOOD,
        Material.ACACIA_WOOD, Material.DARK_OAK_WOOD, Material.MANGROVE_WOOD,
        Material.CHERRY_WOOD, Material.PALE_OAK_WOOD, Material.CRIMSON_HYPHAE, Material.WARPED_HYPHAE,
        // Stripped Wood
        Material.STRIPPED_OAK_WOOD, Material.STRIPPED_SPRUCE_WOOD, Material.STRIPPED_BIRCH_WOOD,
        Material.STRIPPED_JUNGLE_WOOD, Material.STRIPPED_ACACIA_WOOD, Material.STRIPPED_DARK_OAK_WOOD,
        Material.STRIPPED_MANGROVE_WOOD, Material.STRIPPED_CHERRY_WOOD, Material.STRIPPED_PALE_OAK_WOOD,
        Material.STRIPPED_CRIMSON_HYPHAE, Material.STRIPPED_WARPED_HYPHAE
    );
    
    // All leaf types in Minecraft 1.21
    public static final Set<Material> LEAF_MATERIALS = EnumSet.of(
        Material.OAK_LEAVES, Material.SPRUCE_LEAVES, Material.BIRCH_LEAVES, Material.JUNGLE_LEAVES,
        Material.ACACIA_LEAVES, Material.DARK_OAK_LEAVES, Material.MANGROVE_LEAVES,
        Material.CHERRY_LEAVES, Material.PALE_OAK_LEAVES, Material.AZALEA_LEAVES,
        Material.FLOWERING_AZALEA_LEAVES, Material.NETHER_WART_BLOCK, Material.WARPED_WART_BLOCK
    );
    
    // Axe materials
    public static final Set<Material> AXE_MATERIALS = EnumSet.of(
        Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE,
        Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE
    );
    
    // Log type to corresponding leaf types mapping
    public static final Map<Material, Set<Material>> LOG_LEAF_MAP = Map.ofEntries(
        Map.entry(Material.OAK_LOG, Set.of(Material.OAK_LEAVES, Material.AZALEA_LEAVES, Material.FLOWERING_AZALEA_LEAVES)),
        Map.entry(Material.SPRUCE_LOG, Set.of(Material.SPRUCE_LEAVES)),
        Map.entry(Material.BIRCH_LOG, Set.of(Material.BIRCH_LEAVES)),
        Map.entry(Material.JUNGLE_LOG, Set.of(Material.JUNGLE_LEAVES)),
        Map.entry(Material.ACACIA_LOG, Set.of(Material.ACACIA_LEAVES)),
        Map.entry(Material.DARK_OAK_LOG, Set.of(Material.DARK_OAK_LEAVES)),
        Map.entry(Material.MANGROVE_LOG, Set.of(Material.MANGROVE_LEAVES)),
        Map.entry(Material.CHERRY_LOG, Set.of(Material.CHERRY_LEAVES)),
        Map.entry(Material.PALE_OAK_LOG, Set.of(Material.PALE_OAK_LEAVES)),
        Map.entry(Material.CRIMSON_STEM, Set.of(Material.NETHER_WART_BLOCK)),
        Map.entry(Material.WARPED_STEM, Set.of(Material.WARPED_WART_BLOCK))
    );
    
    /**
     * Check if material is a log block.
     */
    public static boolean isLogBlock(Material material) {
        return LOG_MATERIALS.contains(material);
    }
    
    /**
     * Check if material is a leaf block.
     */
    public static boolean isLeafBlock(Material material) {
        return LEAF_MATERIALS.contains(material);
    }
    
    /**
     * Check if material is an axe.
     */
    public static boolean isAxe(Material material) {
        return AXE_MATERIALS.contains(material);
    }
    
    /**
     * Check if material is any wood-related block (log, wood, stripped).
     */
    public static boolean isWoodBlock(Material material) {
        String name = material.name();
        return name.contains("LOG") || name.contains("STEM") || 
               name.contains("WOOD") || name.contains("HYPHAE");
    }
    
    /**
     * Get the base wood type from any log variant.
     * e.g., STRIPPED_OAK_LOG -> OAK, OAK_WOOD -> OAK
     */
    public static String getWoodType(Material material) {
        String name = material.name();
        return name.replace("STRIPPED_", "")
                   .replace("_LOG", "")
                   .replace("_STEM", "")
                   .replace("_WOOD", "")
                   .replace("_HYPHAE", "");
    }
    
    /**
     * Check if two materials are the same wood type.
     */
    public static boolean isSameWoodType(Material mat1, Material mat2) {
        return getWoodType(mat1).equals(getWoodType(mat2));
    }
    
    /**
     * Get valid leaf types for a given log type.
     */
    public static Set<Material> getValidLeaves(Material logType) {
        // Normalize to base log type
        String woodType = getWoodType(logType);
        
        for (Map.Entry<Material, Set<Material>> entry : LOG_LEAF_MAP.entrySet()) {
            if (getWoodType(entry.getKey()).equals(woodType)) {
                return entry.getValue();
            }
        }
        
        return Set.of();
    }
}
