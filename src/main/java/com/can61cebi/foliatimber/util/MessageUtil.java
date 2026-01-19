package com.can61cebi.foliatimber.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

/**
 * Utility class for message formatting and sending.
 */
public final class MessageUtil {
    
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = 
        LegacyComponentSerializer.legacyAmpersand();
    
    private MessageUtil() {}
    
    /**
     * Translate color codes (&) to Adventure components.
     */
    public static Component colorize(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        return LEGACY_SERIALIZER.deserialize(message);
    }
    
    /**
     * Send a colored message to a player.
     */
    public static void send(Player player, String message) {
        player.sendMessage(colorize(message));
    }
    
    /**
     * Send a prefixed message to a player.
     */
    public static void sendPrefixed(Player player, String prefix, String message) {
        player.sendMessage(colorize(prefix + message));
    }
}
