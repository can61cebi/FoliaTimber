package com.can61cebi.foliatimber.tree;

import com.can61cebi.foliatimber.FoliaTimber;
import com.can61cebi.foliatimber.config.ConfigManager;
import com.can61cebi.foliatimber.util.MaterialUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles the actual tree chopping process.
 * All blocks break instantly, only logs damage the axe.
 */
public class TreeChopper {
    
    private final FoliaTimber plugin;
    private final ConfigManager config;
    private final Player player;
    private final TreeData treeData;
    
    public TreeChopper(FoliaTimber plugin, Player player, TreeData treeData, ItemStack tool) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.player = player;
        this.treeData = treeData;
    }
    
    /**
     * Start the tree chopping process - all blocks break instantly.
     */
    public void startChopping() {
        Set<Block> logs = treeData.logs();
        Set<Block> leaves = treeData.leaves();
        
        // Check if player still has axe
        ItemStack playerTool = player.getInventory().getItemInMainHand();
        if (playerTool == null || !MaterialUtil.isAxe(playerTool.getType())) {
            return;
        }
        
        // Play ONE sound for the whole tree
        if (config.useSounds() && !logs.isEmpty()) {
            Block firstLog = logs.iterator().next();
            firstLog.getWorld().playSound(
                firstLog.getLocation().add(0.5, 0.5, 0.5),
                Sound.BLOCK_WOOD_BREAK, 
                1.0f, 0.8f
            );
        }
        
        // Break all logs and count them for tool damage
        int logsBroken = 0;
        for (Block log : logs) {
            if (log.getType().isAir()) continue;
            breakBlock(log, true);
            logsBroken++;
        }
        
        // Break all leaves if configured (no tool damage)
        if (config.breakLeaves()) {
            for (Block leaf : leaves) {
                if (leaf.getType().isAir()) continue;
                breakBlock(leaf, false);
            }
        }
        
        // Apply tool damage ONLY for logs broken
        if (logsBroken > 0) {
            applyToolDamage(logsBroken);
        }
        
        // Show particles at center of tree
        if (config.useParticles() && !logs.isEmpty()) {
            showTreeParticles(logs);
        }
    }
    
    /**
     * Break a single block (no sound, handled separately).
     */
    private void breakBlock(Block block, boolean isLog) {
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        
        // Get drops using player's actual current tool
        ItemStack playerTool = player.getInventory().getItemInMainHand();
        Collection<ItemStack> drops = block.getDrops(playerTool, player);
        
        // Remove the block
        block.setType(Material.AIR);
        
        // Handle drops
        if (config.autoCollect()) {
            for (ItemStack drop : drops) {
                HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(drop);
                for (ItemStack item : overflow.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
            }
        } else {
            for (ItemStack drop : drops) {
                block.getWorld().dropItemNaturally(loc, drop);
            }
        }
    }
    
    /**
     * Show particles at tree center.
     */
    private void showTreeParticles(Set<Block> logs) {
        // Find average position
        double avgX = 0, avgY = 0, avgZ = 0;
        for (Block log : logs) {
            avgX += log.getX();
            avgY += log.getY();
            avgZ += log.getZ();
        }
        int count = logs.size();
        Location center = new Location(
            logs.iterator().next().getWorld(),
            avgX / count + 0.5,
            avgY / count + 0.5,
            avgZ / count + 0.5
        );
        
        // Spawn particles
        center.getWorld().spawnParticle(
            Particle.BLOCK,
            center,
            30,
            1.0, 1.5, 1.0,
            0.1,
            Material.OAK_LOG.createBlockData()
        );
    }
    
    /**
     * Apply durability damage to the player's tool (only for logs).
     * Respects Unbreaking enchantment manually since Bukkit API doesn't handle it.
     */
    private void applyToolDamage(int logCount) {
        ItemStack playerTool = player.getInventory().getItemInMainHand();

        if (playerTool == null || playerTool.getType().isAir()) return;
        if (!MaterialUtil.isAxe(playerTool.getType())) return;

        ItemMeta meta = playerTool.getItemMeta();
        if (meta instanceof Damageable damageable) {
            // Get Unbreaking level
            int unbreakingLevel = playerTool.getEnchantmentLevel(Enchantment.UNBREAKING);

            // Calculate actual damage with Unbreaking
            // Formula: For each point of damage, there's a 1/(level+1) chance it applies
            // E.g., Unbreaking III = 1/4 = 25% chance per damage point
            int actualDamage = 0;
            int baseDamage = (int) Math.ceil(logCount * config.getToolDamageMultiplier());

            if (unbreakingLevel > 0) {
                // Roll for each damage point
                for (int i = 0; i < baseDamage; i++) {
                    // 1/(level+1) chance to take damage
                    if (ThreadLocalRandom.current().nextInt(unbreakingLevel + 1) == 0) {
                        actualDamage++;
                    }
                }
            } else {
                actualDamage = baseDamage;
            }

            if (actualDamage <= 0) return; // Unbreaking saved all damage

            int newDamage = damageable.getDamage() + actualDamage;
            int maxDurability = playerTool.getType().getMaxDurability();

            if (newDamage >= maxDurability) {
                // Tool breaks
                player.getInventory().setItemInMainHand(null);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            } else {
                damageable.setDamage(newDamage);
                playerTool.setItemMeta(meta);
            }
        }
    }
}
