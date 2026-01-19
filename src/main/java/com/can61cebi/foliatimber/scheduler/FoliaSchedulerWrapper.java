package com.can61cebi.foliatimber.scheduler;

import com.can61cebi.foliatimber.FoliaTimber;
import com.tcoded.folialib.FoliaLib;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * Wrapper for FoliaLib scheduler providing cleaner API access.
 * Abstracts away Folia/Paper differences.
 */
public class FoliaSchedulerWrapper {
    
    private final FoliaTimber plugin;
    private final FoliaLib foliaLib;
    
    public FoliaSchedulerWrapper(FoliaTimber plugin) {
        this.plugin = plugin;
        this.foliaLib = new FoliaLib(plugin);
    }
    
    /**
     * Get the underlying FoliaLib instance.
     */
    public FoliaLib getFoliaLib() {
        return foliaLib;
    }
    
    /**
     * Check if running on Folia.
     */
    public boolean isFolia() {
        return foliaLib.isFolia();
    }
    
    /**
     * Run a task at a specific location (region-safe).
     */
    public void runAtLocation(Location location, Runnable task) {
        foliaLib.getScheduler().runAtLocation(location, t -> task.run());
    }
    
    /**
     * Run a task at a specific location after a delay.
     */
    public void runAtLocationLater(Location location, Runnable task, long delayTicks) {
        foliaLib.getScheduler().runAtLocationLater(location, t -> task.run(), delayTicks);
    }
    
    /**
     * Run a task for a specific entity (entity-safe).
     */
    public void runAtEntity(Entity entity, Runnable task) {
        foliaLib.getScheduler().runAtEntity(entity, t -> task.run());
    }
    
    /**
     * Run a task for a specific entity after a delay.
     */
    public void runAtEntityLater(Entity entity, Runnable task, long delayTicks) {
        foliaLib.getScheduler().runAtEntityLater(entity, t -> task.run(), delayTicks);
    }
    
    /**
     * Run an async task (for database/file operations).
     */
    public void runAsync(Runnable task) {
        foliaLib.getScheduler().runAsync(t -> task.run());
    }
    
    /**
     * Run an async task after a delay (using global scheduler).
     */
    public void runAsyncLater(Runnable task, long delayTicks) {
        // FoliaLib doesn't have runAsyncLater, use runLater on global scheduler instead
        foliaLib.getScheduler().runLater(t -> {
            foliaLib.getScheduler().runAsync(t2 -> task.run());
        }, delayTicks);
    }
    
    /**
     * Run a global task (use sparingly, prefer location-based).
     */
    public void runGlobal(Runnable task) {
        foliaLib.getScheduler().runNextTick(t -> task.run());
    }
    
    /**
     * Cancel all scheduled tasks for this plugin.
     */
    public void cancelAllTasks() {
        foliaLib.getScheduler().cancelAllTasks();
    }
}
