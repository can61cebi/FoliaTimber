package com.can61cebi.foliatimber.tree;

import com.can61cebi.foliatimber.config.ConfigManager;
import com.can61cebi.foliatimber.util.MaterialUtil;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;

import java.util.*;

import static com.can61cebi.foliatimber.protection.StructureProtection.STRUCTURE_BLOCKS;

/**
 * Detects and validates tree structures using BFS algorithm.
 * Limits horizontal spread to prevent connecting adjacent trees.
 */
public class TreeDetector {

    private final ConfigManager config;

    // Maximum horizontal distance from starting block (prevents connecting adjacent trees)
    private static final int MAX_HORIZONTAL_SPREAD = 3;

    // Fail reason codes (used for localization)
    public static final String REASON_MIN_LEAVES = "MIN_LEAVES";
    public static final String REASON_MIN_LOGS = "MIN_LOGS";
    public static final String REASON_HORIZONTAL = "HORIZONTAL";
    public static final String REASON_MIXED_LOGS = "MIXED_LOGS";
    public static final String REASON_NO_LOGS_ABOVE = "NO_LOGS_ABOVE";
    
    public TreeDetector(ConfigManager config) {
        this.config = config;
    }
    
    /**
     * Scan for a tree starting from the given block.
     *
     * @param startBlock The initial log block
     * @param logType    The type of log to scan for
     * @return TreeData containing scan results
     */
    public TreeData scanTree(Block startBlock, Material logType) {
        Set<Block> logs = new HashSet<>();
        Set<Block> leaves = new HashSet<>();
        Map<Block, Material> potentialStructureBlocks = new HashMap<>();
        Queue<Block> queue = new LinkedList<>();
        Set<Block> visited = new HashSet<>();

        int maxSize = config.getMaxTreeSize();
        int leafRadius = config.getLeafSearchRadius();
        int treehouseRadius = config.getTreehouseCheckRadius();
        Set<Material> validLeaves = MaterialUtil.getValidLeaves(logType);
        
        boolean hasHorizontalLogs = false;
        boolean hasMixedTypes = false;
        
        // Track starting position for horizontal spread limit
        int startX = startBlock.getX();
        int startZ = startBlock.getZ();
        
        queue.add(startBlock);
        visited.add(startBlock);
        
        // BFS to find all connected logs
        while (!queue.isEmpty() && logs.size() < maxSize) {
            Block current = queue.poll();
            Material currentType = current.getType();
            
            if (MaterialUtil.isLogBlock(currentType)) {
                // Check if same wood type
                if (!MaterialUtil.isSameWoodType(currentType, logType)) {
                    hasMixedTypes = true;
                }
                
                // Check for horizontal placement (potential structure)
                if (config.checkHorizontalLogs() && isHorizontalLog(current)) {
                    hasHorizontalLogs = true;
                }
                
                logs.add(current);
                
                // Check neighbors - but limit horizontal spread to prevent connecting adjacent trees
                // Prioritize upward connections (natural tree growth)
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;
                            
                            Block neighbor = current.getRelative(dx, dy, dz);
                            
                            // Check horizontal spread limit
                            int horizDistX = Math.abs(neighbor.getX() - startX);
                            int horizDistZ = Math.abs(neighbor.getZ() - startZ);
                            if (horizDistX > MAX_HORIZONTAL_SPREAD || horizDistZ > MAX_HORIZONTAL_SPREAD) {
                                continue; // Skip blocks too far horizontally
                            }
                            
                            if (!visited.contains(neighbor)) {
                                visited.add(neighbor);
                                Material neighborType = neighbor.getType();
                                
                                if (MaterialUtil.isLogBlock(neighborType) && 
                                    MaterialUtil.isSameWoodType(neighborType, logType)) {
                                    queue.add(neighbor);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Find associated leaves - only include leaves that "belong" to this tree
        // A leaf belongs to this tree if the nearest log is from our tree
        Set<Block> candidateLeaves = new HashSet<>();

        // First, collect all candidate leaves around our logs
        for (Block log : logs) {
            for (int dx = -leafRadius; dx <= leafRadius; dx++) {
                for (int dy = -leafRadius; dy <= leafRadius; dy++) {
                    for (int dz = -leafRadius; dz <= leafRadius; dz++) {
                        Block potential = log.getRelative(dx, dy, dz);
                        if (validLeaves.contains(potential.getType())) {
                            candidateLeaves.add(potential);
                        }
                    }
                }
            }
        }

        // Now filter: only keep leaves whose nearest log is from our tree
        for (Block leaf : candidateLeaves) {
            if (isLeafBelongingToTree(leaf, logs, logType, startX, startZ)) {
                leaves.add(leaf);
            }
        }

        // Collect potential structure blocks around logs (for async treehouse check)
        // This is done on region thread so block.getType() is safe
        if (config.checkTreehouse()) {
            Set<Block> checkedBlocks = new HashSet<>();
            for (Block log : logs) {
                for (int dx = -treehouseRadius; dx <= treehouseRadius; dx++) {
                    for (int dy = -treehouseRadius; dy <= treehouseRadius; dy++) {
                        for (int dz = -treehouseRadius; dz <= treehouseRadius; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;

                            Block nearby = log.getRelative(dx, dy, dz);
                            if (checkedBlocks.contains(nearby)) continue;
                            checkedBlocks.add(nearby);

                            Material type = nearby.getType();

                            // Skip air, logs, and leaves
                            if (type.isAir()) continue;
                            if (MaterialUtil.isLogBlock(type)) continue;
                            if (MaterialUtil.isLeafBlock(type)) continue;

                            // Check if this is a structure block
                            if (isStructureBlock(type)) {
                                potentialStructureBlocks.put(nearby, type);
                            }
                        }
                    }
                }
            }
        }
        
        // Validate if this is a natural tree
        String failReason = null;
        boolean isNatural = true;

        // Check minimum logs first (higher priority)
        if (logs.size() < config.getMinLogs()) {
            isNatural = false;
            failReason = REASON_MIN_LOGS;
        }
        // Check minimum leaves
        else if (leaves.size() < config.getMinLeaves()) {
            isNatural = false;
            failReason = REASON_MIN_LEAVES;
        }
        // Check for horizontal logs (structure indicator)
        // BUT: If tree has many leaves (50+), horizontal logs are likely natural branches
        else if (config.checkHorizontalLogs() && hasHorizontalLogs && leaves.size() < 50) {
            isNatural = false;
            failReason = REASON_HORIZONTAL;
        }
        // Check for mixed log types (structure indicator)
        else if (config.checkMixedLogs() && hasMixedTypes) {
            isNatural = false;
            failReason = REASON_MIXED_LOGS;
        }
        // Check if there are logs ABOVE the starting block
        // Natural trees grow upward, structure columns typically don't have logs directly above
        else {
            boolean hasLogsAbove = false;
            int startY = startBlock.getY();
            for (Block log : logs) {
                if (log.getY() > startY) {
                    hasLogsAbove = true;
                    break;
                }
            }

            if (!hasLogsAbove && logs.size() > 1) {
                isNatural = false;
                failReason = REASON_NO_LOGS_ABOVE;
            }
        }

        return new TreeData(logs, leaves, potentialStructureBlocks, isNatural, failReason);
    }

    /**
     * Check if a log block is placed horizontally.
     */
    private boolean isHorizontalLog(Block block) {
        if (block.getBlockData() instanceof Orientable orientable) {
            Axis axis = orientable.getAxis();
            // Y axis = vertical (natural), X or Z = horizontal (structure)
            return axis == Axis.X || axis == Axis.Z;
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

    /**
     * Check if a leaf block belongs to this tree.
     * A leaf belongs to this tree if:
     * 1. The nearest log (within search radius) is from our tree
     * 2. There's no closer log from another tree
     */
    private boolean isLeafBelongingToTree(Block leaf, Set<Block> ourLogs, Material ourLogType, int treeStartX, int treeStartZ) {
        int leafX = leaf.getX();
        int leafY = leaf.getY();
        int leafZ = leaf.getZ();

        // Find the nearest log to this leaf (search in a radius)
        int searchRadius = config.getLeafSearchRadius();
        Block nearestLog = null;
        double nearestDistSq = Double.MAX_VALUE;
        boolean nearestIsOurs = false;

        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dy = -searchRadius; dy <= searchRadius; dy++) {
                for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                    Block potential = leaf.getRelative(dx, dy, dz);
                    Material potType = potential.getType();

                    if (!MaterialUtil.isLogBlock(potType)) continue;

                    double distSq = dx * dx + dy * dy + dz * dz;

                    if (distSq < nearestDistSq) {
                        nearestDistSq = distSq;
                        nearestLog = potential;
                        nearestIsOurs = ourLogs.contains(potential);
                    } else if (distSq == nearestDistSq && !nearestIsOurs) {
                        // If same distance, prefer our log
                        if (ourLogs.contains(potential)) {
                            nearestLog = potential;
                            nearestIsOurs = true;
                        }
                    }
                }
            }
        }

        // If no log found nearby, don't include this leaf
        if (nearestLog == null) {
            return false;
        }

        // Only include if nearest log is ours
        if (!nearestIsOurs) {
            return false;
        }

        // Additional check: leaf shouldn't be too far horizontally from tree center
        // This prevents edge cases where our log is technically nearest but leaf is very far
        int horizDistX = Math.abs(leafX - treeStartX);
        int horizDistZ = Math.abs(leafZ - treeStartZ);
        int maxLeafSpread = MAX_HORIZONTAL_SPREAD + searchRadius;

        return horizDistX <= maxLeafSpread && horizDistZ <= maxLeafSpread;
    }
}
