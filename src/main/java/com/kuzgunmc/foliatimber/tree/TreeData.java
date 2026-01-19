package com.kuzgunmc.foliatimber.tree;

import org.bukkit.block.Block;

import java.util.Set;

/**
 * Record containing tree scan results.
 *
 * @param logs          All connected log blocks
 * @param leaves        All associated leaf blocks
 * @param isNaturalTree Whether this appears to be a natural tree
 * @param failReason    If not natural, reason why (null if natural)
 */
public record TreeData(
    Set<Block> logs,
    Set<Block> leaves,
    boolean isNaturalTree,
    String failReason
) {
    /**
     * Get total block count (logs + leaves).
     */
    public int getTotalBlockCount() {
        return logs.size() + leaves.size();
    }
    
    /**
     * Check if tree has any leaves.
     */
    public boolean hasLeaves() {
        return !leaves.isEmpty();
    }
}
