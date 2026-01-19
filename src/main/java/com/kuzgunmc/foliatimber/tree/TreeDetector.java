package com.kuzgunmc.foliatimber.tree;

import com.kuzgunmc.foliatimber.config.ConfigManager;
import com.kuzgunmc.foliatimber.util.MaterialUtil;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;

import java.util.*;

/**
 * Detects and validates tree structures using BFS algorithm.
 * Limits horizontal spread to prevent connecting adjacent trees.
 */
public class TreeDetector {
    
    private final ConfigManager config;
    
    // Maximum horizontal distance from starting block (prevents connecting adjacent trees)
    private static final int MAX_HORIZONTAL_SPREAD = 3;
    
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
        Queue<Block> queue = new LinkedList<>();
        Set<Block> visited = new HashSet<>();
        
        int maxSize = config.getMaxTreeSize();
        int leafRadius = config.getLeafSearchRadius();
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
        
        // Find associated leaves around log blocks
        for (Block log : logs) {
            for (int dx = -leafRadius; dx <= leafRadius; dx++) {
                for (int dy = -leafRadius; dy <= leafRadius; dy++) {
                    for (int dz = -leafRadius; dz <= leafRadius; dz++) {
                        Block potential = log.getRelative(dx, dy, dz);
                        if (validLeaves.contains(potential.getType())) {
                            leaves.add(potential);
                        }
                    }
                }
            }
        }
        
        // Validate if this is a natural tree
        String failReason = null;
        boolean isNatural = true;
        
        // Check minimum leaves
        if (leaves.size() < config.getMinLeaves()) {
            isNatural = false;
            failReason = "Yetersiz yaprak (" + leaves.size() + " < " + config.getMinLeaves() + ")";
        }
        
        // Check minimum logs
        if (logs.size() < config.getMinLogs()) {
            isNatural = false;
            failReason = "Yetersiz log (" + logs.size() + " < " + config.getMinLogs() + ")";
        }
        
        // Check for horizontal logs (structure indicator)
        // BUT: If tree has many leaves (50+), horizontal logs are likely natural branches
        if (config.checkHorizontalLogs() && hasHorizontalLogs) {
            if (leaves.size() < 50) {
                isNatural = false;
                failReason = "Yatay loglar tespit edildi, yetersiz yaprak ile (muhtemelen yapı)";
            }
        }
        
        // Check for mixed log types (structure indicator)
        if (config.checkMixedLogs() && hasMixedTypes) {
            isNatural = false;
            failReason = "Karışık log türleri tespit edildi (muhtemelen yapı)";
        }
        
        // NEW: Check if there are logs ABOVE the starting block
        // Natural trees grow upward, structure columns typically don't have logs directly above
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
            failReason = "Başlangıç bloğunun üstünde log yok (muhtemelen yapı kolonu)";
        }
        
        return new TreeData(logs, leaves, isNatural, failReason);
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
}
