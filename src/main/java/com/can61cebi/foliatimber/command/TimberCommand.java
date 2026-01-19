package com.can61cebi.foliatimber.command;

import com.can61cebi.foliatimber.FoliaTimber;
import com.can61cebi.foliatimber.config.ConfigManager;
import com.can61cebi.foliatimber.listener.BlockBreakListener;
import com.can61cebi.foliatimber.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles /timber command for toggling, reloading, and language switching.
 */
public class TimberCommand implements CommandExecutor, TabCompleter {
    
    private final FoliaTimber plugin;
    private final ConfigManager config;
    
    public TimberCommand(FoliaTimber plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                           @NotNull String label, @NotNull String[] args) {
        
        if (args.length == 0) {
            return handleToggle(sender);
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "toggle" -> {
                return handleToggle(sender);
            }
            case "reload" -> {
                return handleReload(sender);
            }
            case "lang", "language" -> {
                return handleLanguage(sender, args);
            }
            case "debug" -> {
                return handleDebug(sender);
            }
            case "help" -> {
                return handleHelp(sender);
            }
            default -> {
                sendMessage(sender, config.getPrefix() + "&cUnknown command. Type /timber help");
                return true;
            }
        }
    }
    
    private boolean handleToggle(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        
        if (!player.hasPermission("foliatimber.use")) {
            MessageUtil.send(player, config.getPrefixedMessage("no-permission"));
            return true;
        }
        
        BlockBreakListener listener = plugin.getBlockBreakListener();
        boolean newState = listener.toggleTimber(player);
        
        if (newState) {
            MessageUtil.send(player, config.getPrefixedMessage("enabled"));
        } else {
            MessageUtil.send(player, config.getPrefixedMessage("disabled"));
        }
        
        return true;
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("foliatimber.reload")) {
            sendMessage(sender, config.getPrefixedMessage("no-permission"));
            return true;
        }
        
        plugin.getConfigManager().reload();
        sendMessage(sender, config.getPrefixedMessage("reloaded"));
        
        return true;
    }
    
    private boolean handleLanguage(CommandSender sender, String[] args) {
        if (!sender.hasPermission("foliatimber.reload")) {
            sendMessage(sender, config.getPrefixedMessage("no-permission"));
            return true;
        }
        
        if (args.length < 2) {
            // Show current language
            String msg = config.getMessage("language-current")
                .replace("%lang%", config.getLanguage().toUpperCase());
            sendMessage(sender, config.getPrefix() + msg);
            
            String available = config.getMessage("language-available")
                .replace("%languages%", config.getAvailableLanguages());
            sendMessage(sender, config.getPrefix() + available);
            return true;
        }
        
        String newLang = args[1].toLowerCase();
        
        if (!config.isValidLanguage(newLang)) {
            String msg = config.getMessage("language-invalid")
                .replace("%languages%", config.getAvailableLanguages());
            sendMessage(sender, config.getPrefix() + msg);
            return true;
        }
        
        config.setLanguage(newLang);
        sendMessage(sender, config.getPrefixedMessage("language-changed"));
        
        return true;
    }

    private boolean handleDebug(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("foliatimber.use")) {
            MessageUtil.send(player, config.getPrefixedMessage("no-permission"));
            return true;
        }

        BlockBreakListener listener = plugin.getBlockBreakListener();
        boolean newState = listener.toggleDebug(player);

        if (newState) {
            MessageUtil.send(player, config.getPrefixedMessage("debug-enabled"));
        } else {
            MessageUtil.send(player, config.getPrefixedMessage("debug-disabled"));
        }

        return true;
    }
    
    private boolean handleHelp(CommandSender sender) {
        sendMessage(sender, config.getPrefixedMessage("help-header"));
        sendMessage(sender, config.getMessage("help-toggle"));
        sendMessage(sender, config.getMessage("help-reload"));
        sendMessage(sender, config.getMessage("help-lang"));
        sendMessage(sender, config.getMessage("help-debug"));
        sendMessage(sender, config.getMessage("help-help"));
        
        return true;
    }
    
    private void sendMessage(CommandSender sender, String message) {
        if (sender instanceof Player player) {
            MessageUtil.send(player, message);
        } else {
            sender.sendMessage(message.replaceAll("&[0-9a-fk-or]", ""));
        }
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            
            if ("toggle".startsWith(partial)) completions.add("toggle");
            if ("help".startsWith(partial)) completions.add("help");
            if ("lang".startsWith(partial)) completions.add("lang");
            if ("debug".startsWith(partial)) completions.add("debug");
            
            if (sender.hasPermission("foliatimber.reload")) {
                if ("reload".startsWith(partial)) completions.add("reload");
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("lang") || args[0].equalsIgnoreCase("language"))) {
            String partial = args[1].toLowerCase();
            for (String lang : List.of("en", "tr", "de")) {
                if (lang.startsWith(partial)) completions.add(lang);
            }
        }
        
        return completions;
    }
}
