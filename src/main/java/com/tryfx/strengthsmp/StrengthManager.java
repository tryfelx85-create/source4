package com.tryfx.strengthsmp;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Manages persistent Strength values for players.
 * Strength is stored in the player's PersistentDataContainer (survives restarts
 * automatically via the player's playerdata NBT) AND cached in memory for fast access.
 */
public final class StrengthManager {

    private final StrengthSMP plugin;
    private final NamespacedKey strengthKey;
    private final Map<UUID, Integer> cache = new ConcurrentHashMap<>();

    public StrengthManager(StrengthSMP plugin) {
        this.plugin = plugin;
        this.strengthKey = new NamespacedKey(plugin, "strength_value");
    }

    public int getMin() {
        return plugin.getConfig().getInt("strength.min", -5);
    }

    public int getMax() {
        return plugin.getConfig().getInt("strength.max", 5);
    }

    public int getDefault() {
        return plugin.getConfig().getInt("strength.default", 0);
    }

    public double getDamagePerPoint() {
        return plugin.getConfig().getDouble("strength.damage-per-point", 0.10);
    }

    /**
     * Loads a player's strength from their PDC into cache. Call on join.
     */
    public void load(Player player) {
        Integer stored = player.getPersistentDataContainer().get(strengthKey, PersistentDataType.INTEGER);
        int value = (stored != null) ? stored : getDefault();
        cache.put(player.getUniqueId(), clamp(value));
    }

    /**
     * Flushes cached value back to PDC. Call on quit (PDC auto-saves with player data).
     */
    public void unload(Player player) {
        Integer value = cache.remove(player.getUniqueId());
        if (value != null) {
            player.getPersistentDataContainer().set(strengthKey, PersistentDataType.INTEGER, value);
        }
    }

    public int getStrength(Player player) {
        return cache.computeIfAbsent(player.getUniqueId(), id -> {
            Integer stored = player.getPersistentDataContainer().get(strengthKey, PersistentDataType.INTEGER);
            return clamp(stored != null ? stored : getDefault());
        });
    }

    /**
     * Sets strength and immediately persists to PDC.
     */
    public void setStrength(Player player, int value) {
        int clamped = clamp(value);
        cache.put(player.getUniqueId(), clamped);
        player.getPersistentDataContainer().set(strengthKey, PersistentDataType.INTEGER, clamped);
    }

    public void addStrength(Player player, int delta) {
        setStrength(player, getStrength(player) + delta);
    }

    public boolean isAtMax(Player player) {
        return getStrength(player) >= getMax();
    }

    public boolean isAtMin(Player player) {
        return getStrength(player) <= getMin();
    }

    /**
     * Returns the damage multiplier for a player's current strength.
     * e.g. strength=3, damage-per-point=0.10 -> 1.30 (base damage * 1.30)
     */
    public double getDamageMultiplier(Player player) {
        int strength = getStrength(player);
        return 1.0 + (strength * getDamagePerPoint());
    }

    private int clamp(int value) {
        return Math.max(getMin(), Math.min(getMax(), value));
    }
}
