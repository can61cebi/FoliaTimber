package com.kuzgunmc.foliatimber;

import com.kuzgunmc.foliatimber.command.TimberCommand;
import com.kuzgunmc.foliatimber.config.ConfigManager;
import com.kuzgunmc.foliatimber.listener.BlockBreakListener;
import com.kuzgunmc.foliatimber.protection.CoreProtectHook;
import com.kuzgunmc.foliatimber.protection.StructureProtection;
import com.kuzgunmc.foliatimber.protection.WorldGuardHook;
import com.kuzgunmc.foliatimber.scheduler.FoliaSchedulerWrapper;
import com.kuzgunmc.foliatimber.tree.TreeDetector;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * FoliaTimber - Smart tree chopping with structure protection
 * Native Folia support with Paper compatibility
 */
public class FoliaTimber extends JavaPlugin {
    
    private static FoliaTimber instance;
    
    private FoliaSchedulerWrapper scheduler;
    private ConfigManager configManager;
    private CoreProtectHook coreProtectHook;
    private WorldGuardHook worldGuardHook;
    private StructureProtection structureProtection;
    private TreeDetector treeDetector;
    private BlockBreakListener blockBreakListener;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize scheduler wrapper (FoliaLib)
        this.scheduler = new FoliaSchedulerWrapper(this);
        
        // Load configuration
        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        
        // Initialize hooks
        this.coreProtectHook = new CoreProtectHook(this);
        this.worldGuardHook = new WorldGuardHook(this);
        
        // Initialize services
        this.structureProtection = new StructureProtection(this, coreProtectHook);
        this.treeDetector = new TreeDetector(configManager);
        
        // Register listener
        this.blockBreakListener = new BlockBreakListener(this);
        getServer().getPluginManager().registerEvents(blockBreakListener, this);
        
        // Register commands
        TimberCommand timberCommand = new TimberCommand(this);
        var cmd = getCommand("timber");
        if (cmd != null) {
            cmd.setExecutor(timberCommand);
            cmd.setTabCompleter(timberCommand);
        }
        
        // Simple English startup log
        getLogger().info("FoliaTimber v" + getDescription().getVersion() + " enabled!");
        getLogger().info("Folia: " + (scheduler.isFolia() ? "Yes" : "No") + 
                        " | CoreProtect: " + (coreProtectHook.isEnabled() ? "Yes" : "No") +
                        " | WorldGuard: " + (worldGuardHook.isEnabled() ? "Yes" : "No"));
    }
    
    @Override
    public void onDisable() {
        if (scheduler != null) {
            scheduler.cancelAllTasks();
        }
        getLogger().info("FoliaTimber disabled.");
    }
    
    // Scheduler helpers
    public void runAtLocation(Location location, Runnable task) {
        scheduler.runAtLocation(location, task);
    }
    
    public void runLater(Location location, Runnable task, long delayTicks) {
        scheduler.runAtLocationLater(location, task, delayTicks);
    }
    
    public void runAtEntity(Entity entity, Runnable task) {
        scheduler.runAtEntity(entity, task);
    }
    
    public void runAsync(Runnable task) {
        scheduler.runAsync(task);
    }
    
    // Getters
    public static FoliaTimber getInstance() { return instance; }
    public FoliaSchedulerWrapper getScheduler() { return scheduler; }
    public ConfigManager getConfigManager() { return configManager; }
    public CoreProtectHook getCoreProtectHook() { return coreProtectHook; }
    public WorldGuardHook getWorldGuardHook() { return worldGuardHook; }
    public StructureProtection getStructureProtection() { return structureProtection; }
    public TreeDetector getTreeDetector() { return treeDetector; }
    public BlockBreakListener getBlockBreakListener() { return blockBreakListener; }
}
