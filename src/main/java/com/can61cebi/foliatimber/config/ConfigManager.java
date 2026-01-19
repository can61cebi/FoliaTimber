package com.can61cebi.foliatimber.config;

import com.can61cebi.foliatimber.FoliaTimber;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Manages plugin configuration and language files.
 */
public class ConfigManager {
    
    private final FoliaTimber plugin;
    private FileConfiguration config;
    private FileConfiguration langConfig;
    private String language;
    
    private static final List<String> AVAILABLE_LANGUAGES = Arrays.asList("en", "tr", "de");
    
    public ConfigManager(FoliaTimber plugin) {
        this.plugin = plugin;
        reload();
    }
    
    /**
     * Reload configuration and language from files.
     */
    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        
        this.language = config.getString("language", "en");
        loadLanguage();
    }
    
    /**
     * Load language file.
     */
    private void loadLanguage() {
        // Save all default language files
        for (String lang : AVAILABLE_LANGUAGES) {
            saveResource("lang/" + lang + ".yml");
        }
        
        // Load selected language
        File langFile = new File(plugin.getDataFolder(), "lang/" + language + ".yml");
        if (langFile.exists()) {
            langConfig = YamlConfiguration.loadConfiguration(langFile);
        } else {
            langFile = new File(plugin.getDataFolder(), "lang/en.yml");
            langConfig = YamlConfiguration.loadConfiguration(langFile);
            plugin.getLogger().warning("Language '" + language + "' not found, using English");
            language = "en";
        }
        
        // Set defaults from bundled resource
        InputStream defaultStream = plugin.getResource("lang/" + language + ".yml");
        if (defaultStream == null) {
            defaultStream = plugin.getResource("lang/en.yml");
        }
        if (defaultStream != null) {
            langConfig.setDefaults(YamlConfiguration.loadConfiguration(
                new InputStreamReader(defaultStream, StandardCharsets.UTF_8)));
        }
    }
    
    private void saveResource(String path) {
        File file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            if (plugin.getResource(path) != null) {
                plugin.saveResource(path, false);
            }
        }
    }
    
    /**
     * Change language and save to config.
     * @param newLang The new language code (en, tr, de)
     * @return true if successful, false if invalid language
     */
    public boolean setLanguage(String newLang) {
        String lang = newLang.toLowerCase();
        if (!AVAILABLE_LANGUAGES.contains(lang)) {
            return false;
        }
        
        this.language = lang;
        config.set("language", lang);
        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException ignored) {}
        
        loadLanguage();
        return true;
    }
    
    /**
     * Get available languages as comma-separated string.
     */
    public String getAvailableLanguages() {
        return String.join(", ", AVAILABLE_LANGUAGES);
    }
    
    /**
     * Check if a language is valid.
     */
    public boolean isValidLanguage(String lang) {
        return AVAILABLE_LANGUAGES.contains(lang.toLowerCase());
    }
    
    // ========== Debug ==========

    public boolean isDebug() {
        return config.getBoolean("debug", false);
    }

    // ========== General Settings ==========

    public boolean isEnabled() {
        return config.getBoolean("general.enabled", true);
    }
    
    public boolean isDefaultEnabled() {
        return config.getBoolean("general.default-enabled", true);
    }
    
    public boolean requireSneak() {
        return config.getBoolean("general.require-sneak", false);
    }
    
    public boolean requireAxe() {
        return config.getBoolean("general.require-axe", true);
    }
    
    public int getMaxTreeSize() {
        return config.getInt("general.max-tree-size", 256);
    }
    
    // ========== Protection Settings ==========
    
    public boolean useCoreProtect() {
        return config.getBoolean("protection.use-coreprotect", true);
    }
    
    public int getCoreProtectLookupDays() {
        return config.getInt("protection.coreprotect-lookup-days", 30);
    }
    
    public boolean checkTreehouse() {
        return config.getBoolean("protection.check-treehouse", true);
    }
    
    public int getTreehouseCheckRadius() {
        return config.getInt("protection.treehouse-check-radius", 2);
    }
    
    public boolean useWorldGuard() {
        return config.getBoolean("protection.use-worldguard", true);
    }
    
    // ========== Tree Detection Settings ==========
    
    public int getMinLeaves() {
        return config.getInt("tree-detection.min-leaves", 5);
    }
    
    public int getMinLogs() {
        return config.getInt("tree-detection.min-logs", 3);
    }
    
    public int getLeafSearchRadius() {
        return config.getInt("tree-detection.leaf-search-radius", 6);
    }
    
    public boolean checkHorizontalLogs() {
        return config.getBoolean("tree-detection.check-horizontal-logs", true);
    }
    
    public boolean checkMixedLogs() {
        return config.getBoolean("tree-detection.check-mixed-logs", true);
    }
    
    // ========== Chopping Settings ==========
    
    public boolean breakLeaves() {
        return config.getBoolean("chopping.break-leaves", true);
    }
    
    public double getToolDamageMultiplier() {
        return config.getDouble("chopping.tool-damage-multiplier", 1.0);
    }
    
    public boolean autoCollect() {
        return config.getBoolean("chopping.auto-collect", true);
    }
    
    // ========== Effects Settings ==========
    
    public boolean useParticles() {
        return config.getBoolean("effects.particles", true);
    }
    
    public boolean useSounds() {
        return config.getBoolean("effects.sounds", true);
    }
    
    // ========== Messages ==========
    
    public String getPrefix() {
        return langConfig.getString("prefix", "&8[&6FoliaTimber&8] ");
    }
    
    public String getMessage(String key) {
        return langConfig.getString(key, "&cMessage not found: " + key);
    }
    
    public String getPrefixedMessage(String key) {
        return getPrefix() + getMessage(key);
    }
    
    public String getLanguage() {
        return language;
    }

    // ========== Debug Messages ==========

    public String getDebugPrefix() {
        return langConfig.getString("debug-prefix", "&8[&bDebug&8] ");
    }

    public String getDebugMessage(String key) {
        return getDebugPrefix() + langConfig.getString(key, "&cDebug message not found: " + key);
    }

    public String getDebugMessage(String key, String... replacements) {
        String msg = getDebugPrefix() + langConfig.getString(key, "&cDebug message not found: " + key);
        for (int i = 0; i < replacements.length - 1; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }
        return msg;
    }

    public String getRawDebugMessage(String key) {
        return langConfig.getString(key, "");
    }
}
