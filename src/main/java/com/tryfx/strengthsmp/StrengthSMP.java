package com.tryfx.strengthsmp;

import org.bukkit.plugin.java.JavaPlugin;

public final class StrengthSMP extends JavaPlugin {

    private StrengthManager strengthManager;
    private StrengthBook strengthBook;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.strengthManager = new StrengthManager(this);
        this.strengthBook = new StrengthBook(this);

        getServer().getPluginManager().registerEvents(new PvPListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new BookListener(this), this);

        StrengthCommand strengthCmd = new StrengthCommand(this);
        getCommand("strength").setExecutor(strengthCmd);
        getCommand("strength").setTabCompleter(strengthCmd);

        StrengthAdminCommand adminCmd = new StrengthAdminCommand(this);
        getCommand("strengthadmin").setExecutor(adminCmd);
        getCommand("strengthadmin").setTabCompleter(adminCmd);

        // Load already-online players (handles /reload)
        for (var player : getServer().getOnlinePlayers()) {
            strengthManager.load(player);
        }

        getLogger().info("StrengthSMP enabled.");
    }

    @Override
    public void onDisable() {
        if (strengthManager != null) {
            for (var player : getServer().getOnlinePlayers()) {
                strengthManager.unload(player);
            }
        }
        getLogger().info("StrengthSMP disabled.");
    }

    public StrengthManager getStrengthManager() {
        return strengthManager;
    }

    public StrengthBook getStrengthBook() {
        return strengthBook;
    }
}
