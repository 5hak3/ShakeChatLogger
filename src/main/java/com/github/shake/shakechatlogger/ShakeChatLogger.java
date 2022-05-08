package com.github.shake.shakechatlogger;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class ShakeChatLogger extends JavaPlugin {
    private Config config;

    @Override
    public void onEnable() {
        config = new Config(this);
        getLogger().info("ShakeCL Configs Loaded!");
        Objects.requireNonNull(getCommand("sclreload")).setExecutor(config);

        getServer().getPluginManager().registerEvents(new ChatListener(this.config), this);
    }
}
