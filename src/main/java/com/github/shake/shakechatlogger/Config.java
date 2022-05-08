package com.github.shake.shakechatlogger;

import com.earth2me.essentials.Essentials;
import com.github.ucchyocean.lc.LunaChat;
import com.github.ucchyocean.lc.LunaChatAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.mariadb.jdbc.MariaDbPoolDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class Config implements CommandExecutor {
    public final JavaPlugin plugin;
    public boolean isEnabledLC;
    public boolean isEnabledESS;
    public Essentials ess = null;
    public LunaChatAPI lunaChatAPI = null;
    private MariaDbPoolDataSource dataSource;

    public Config(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.loadConfig();
    }

    /**
     * Reload Command
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.loadConfig();
        this.plugin.getLogger().info("ShakeCL Configs Reloaded!");
        return true;
    }

    private void loadConfig() {
        this.isEnabledLC = this.plugin.getServer().getPluginManager().isPluginEnabled("LunaChat");
        this.isEnabledESS = this.plugin.getServer().getPluginManager().isPluginEnabled("Essentials");
        if (this.isEnabledESS) {
            this.ess = (Essentials) this.plugin.getServer().getPluginManager().getPlugin("Essentials");
        }
        if (this.isEnabledLC) {
            this.lunaChatAPI = LunaChat.getInstance().getLunaChatAPI();
        }

        ConfigurationSection dbConf = plugin.getConfig().getConfigurationSection("database");
        String host = Objects.requireNonNull(dbConf.getString("host"));
        String port = Objects.requireNonNull(dbConf.getString("port"));
        String db = Objects.requireNonNull(dbConf.getString("database"));
        String user = Objects.requireNonNull(dbConf.getString("user"));
        String passwd = Objects.requireNonNull(dbConf.getString("password"));

        this.dataSource = new MariaDbPoolDataSource();
        try {
            this.dataSource.setPassword(passwd);
            this.dataSource.setUser(user);
            this.dataSource.setUrl("jdbc:mariadb://" + host + ":" + port + "/" + db);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.plugin.getLogger().info("Database: " + this.dataSource.getUrl());
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }
}
