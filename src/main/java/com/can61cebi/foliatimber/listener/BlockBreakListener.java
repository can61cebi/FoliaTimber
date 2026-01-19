package com.can61cebi.foliatimber.listener;

import com.can61cebi.foliatimber.FoliaTimber;
import com.can61cebi.foliatimber.config.ConfigManager;
import com.can61cebi.foliatimber.tree.TreeChopper;
import com.can61cebi.foliatimber.tree.TreeData;
import com.can61cebi.foliatimber.tree.TreeDetector;
import com.can61cebi.foliatimber.util.MaterialUtil;
import com.can61cebi.foliatimber.util.MessageUtil;
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

    // Player debug states (true = enabled)
    private final ConcurrentHashMap<UUID, Boolean> debugPlayers = new ConcurrentHashMap<>();

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
        boolean debug = isDebugEnabled(player);

        // 1. Scan tree SYNCHRONOUSLY (on region thread - this is safe)
        TreeData treeData = plugin.getTreeDetector().scanTree(block, logType);

        // Debug: Show scan results
        if (debug) {
            String scanMsg = config.getDebugMessage("debug-scan-result",
                    "%logs%", String.valueOf(treeData.logs().size()),
                    "%leaves%", String.valueOf(treeData.leaves().size()),
                    "%structures%", String.valueOf(treeData.potentialStructureBlocks().size()));
            MessageUtil.send(player, scanMsg);
        }

        // 2. Check if natural tree
        if (!treeData.isNaturalTree()) {
            if (debug) {
                String reason = getLocalizedReason(treeData.failReason(), treeData);
                String msg = config.getDebugMessage("debug-not-natural", "%reason%", reason);
                MessageUtil.send(player, msg);
            }
            processingPlayers.remove(player.getUniqueId());
            return;
        }

        // 3. If CoreProtect is enabled and player doesn't have bypass, check async
        boolean hasBypass = player.hasPermission("foliatimber.bypass");
        boolean useCoreProtect = config.useCoreProtect() && plugin.getCoreProtectHook().isEnabled();

        if (!hasBypass && useCoreProtect) {
            if (debug) {
                MessageUtil.send(player, config.getDebugMessage("debug-coreprotect-check"));
            }

            // Run CoreProtect queries async, then come back to region thread
            plugin.runAsync(() -> {
                try {
                    // Check if the starting block is player-placed
                    boolean isPlayerPlaced = plugin.getStructureProtection().isPlayerPlacedBlock(block);

                    if (isPlayerPlaced) {
                        plugin.runAtLocation(blockLocation, () -> {
                            if (debug) {
                                MessageUtil.send(player, config.getDebugMessage("debug-block-player-placed"));
                            } else {
                                MessageUtil.send(player, config.getPrefixedMessage("structure-protected"));
                            }
                        });
                        return;
                    }

                    // Check if any log in tree is player-placed
                    boolean anyPlayerPlaced = plugin.getStructureProtection().anyBlockPlacedByPlayer(treeData.logs());

                    if (anyPlayerPlaced) {
                        plugin.runAtLocation(blockLocation, () -> {
                            if (debug) {
                                MessageUtil.send(player, config.getDebugMessage("debug-logs-player-placed"));
                            } else {
                                MessageUtil.send(player, config.getPrefixedMessage("structure-protected"));
                            }
                        });
                        return;
                    }

                    // Check for treehouse (player-placed structure blocks attached to tree)
                    boolean hasTreehouse = plugin.getStructureProtection().hasTreehouse(treeData.potentialStructureBlocks());

                    if (hasTreehouse) {
                        plugin.runAtLocation(blockLocation, () -> {
                            if (debug) {
                                String msg = config.getDebugMessage("debug-treehouse-detected",
                                        "%count%", String.valueOf(treeData.potentialStructureBlocks().size()));
                                MessageUtil.send(player, msg);
                            } else {
                                MessageUtil.send(player, config.getPrefixedMessage("treehouse-protected"));
                            }
                        });
                        return;
                    }

                    // All checks passed, start chopping on region thread
                    plugin.runAtLocation(blockLocation, () -> {
                        if (debug) {
                            MessageUtil.send(player, config.getDebugMessage("debug-passed"));
                        }
                        TreeChopper chopper = new TreeChopper(plugin, player, treeData, tool);
                        chopper.startChopping();
                    });

                } finally {
                    processingPlayers.remove(player.getUniqueId());
                }
            });
        } else {
            // No CoreProtect check needed, start chopping immediately
            if (debug) {
                MessageUtil.send(player, config.getDebugMessage("debug-passed"));
            }
            TreeChopper chopper = new TreeChopper(plugin, player, treeData, tool);
            chopper.startChopping();
            processingPlayers.remove(player.getUniqueId());
        }
    }

    /**
     * Get localized reason message for tree detection failure.
     */
    private String getLocalizedReason(String reasonCode, TreeData treeData) {
        if (reasonCode == null) return "";

        return switch (reasonCode) {
            case TreeDetector.REASON_MIN_LEAVES -> config.getRawDebugMessage("debug-reason-min-leaves")
                    .replace("%count%", String.valueOf(treeData.leaves().size()))
                    .replace("%min%", String.valueOf(config.getMinLeaves()));
            case TreeDetector.REASON_MIN_LOGS -> config.getRawDebugMessage("debug-reason-min-logs")
                    .replace("%count%", String.valueOf(treeData.logs().size()))
                    .replace("%min%", String.valueOf(config.getMinLogs()));
            case TreeDetector.REASON_HORIZONTAL -> config.getRawDebugMessage("debug-reason-horizontal");
            case TreeDetector.REASON_MIXED_LOGS -> config.getRawDebugMessage("debug-reason-mixed-logs");
            case TreeDetector.REASON_NO_LOGS_ABOVE -> config.getRawDebugMessage("debug-reason-no-logs-above");
            default -> reasonCode;
        };
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

    /**
     * Check if debug is enabled for a player (either global or per-player).
     */
    public boolean isDebugEnabled(Player player) {
        return config.isDebug() || debugPlayers.getOrDefault(player.getUniqueId(), false);
    }

    /**
     * Toggle debug for a player.
     * @return New state (true = enabled)
     */
    public boolean toggleDebug(Player player) {
        boolean current = debugPlayers.getOrDefault(player.getUniqueId(), false);
        boolean newState = !current;
        debugPlayers.put(player.getUniqueId(), newState);
        return newState;
    }
}
