package com.tryfx.strengthsmp;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class PvPListener implements Listener {

    private final StrengthSMP plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public PvPListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    /**
     * Scales outgoing damage based on the attacker's Strength.
     * Applies to direct player-vs-player attacks only. Runs at HIGH priority
     * so it applies after other plugins' base damage calcs, but before the
     * final damage is locked in.
     *
     * IMPORTANT: Citizens' player-type NPCs (e.g. the TryFXTheBot training
     * dummy) also satisfy "instanceof Player", since Citizens spawns them as
     * real Player entities for skin rendering. Those NPCs are not real
     * server members and have no meaningful Strength value, so this
     * listener explicitly excludes any entity tagged with Citizens' "NPC"
     * metadata (the standard way to detect a Citizens NPC without requiring
     * a hard compile-time dependency on Citizens here) from both the
     * attacker and victim side - both the damage-scaling and the kill/death
     * Strength transfer in onDeath() below.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player)) return; // only scale PvP damage
        if (isCitizensNpc(attacker)) return; // training bots don't get/grant Strength scaling

        double multiplier = plugin.getStrengthManager().getDamageMultiplier(attacker);
        event.setDamage(event.getDamage() * multiplier);
    }

    /**
     * Detects a Citizens NPC without requiring a compile-time dependency on
     * the Citizens plugin - Citizens tags every NPC entity with "NPC"
     * metadata, which is readable via plain Bukkit metadata API.
     */
    private boolean isCitizensNpc(Player player) {
        return player.hasMetadata("NPC");
    }

    /**
     * Handles the Strength transfer on player death:
     * - Victim always loses 1 Strength (down to min).
     * - Killer gains 1 Strength if below max.
     * - If killer is already at max, victim drops a Strength Knowledge Book instead.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null || killer.equals(victim)) return; // not a PvP kill
        if (isCitizensNpc(killer)) return; // dying to a training bot grants no Strength

        StrengthManager sm = plugin.getStrengthManager();
        String prefix = plugin.getConfig().getString("messages.prefix", "");

        // Victim always loses 1 strength
        sm.addStrength(victim, -1);

        if (!sm.isAtMax(killer)) {
            // Normal case: killer gains strength
            sm.addStrength(killer, 1);

            String msg = plugin.getConfig().getString("messages.on-kill-gain", "")
                    .replace("%victim%", victim.getName())
                    .replace("%strength%", formatStrength(sm.getStrength(killer)));
            killer.sendMessage(MM.deserialize(prefix + msg));
        } else {
            // Killer capped: victim drops a Strength Knowledge Book instead
            var book = plugin.getStrengthBook().create();
            victim.getWorld().dropItemNaturally(victim.getLocation(), book);

            String msg = plugin.getConfig().getString("messages.on-kill-capped", "")
                    .replace("%victim%", victim.getName())
                    .replace("%max%", String.valueOf(sm.getMax()));
            killer.sendMessage(MM.deserialize(prefix + msg));
        }

        String deathMsg = plugin.getConfig().getString("messages.on-death-loss", "")
                .replace("%killer%", killer.getName())
                .replace("%strength%", formatStrength(sm.getStrength(victim)));
        victim.sendMessage(MM.deserialize(prefix + deathMsg));
    }

    private String formatStrength(int value) {
        return (value > 0 ? "+" : "") + value;
    }
}
