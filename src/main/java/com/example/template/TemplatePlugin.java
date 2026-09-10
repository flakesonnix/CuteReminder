package com.example.template;

import org.bukkit.plugin.java.JavaPlugin;

public final class TemplatePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        var cmd = getCommand("ping");
        if (cmd != null) {
            var executor = new PingCommand();
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        } else {
            getLogger().warning("ping command not registered — check plugin.yml / paper-plugin.yml");
        }
        getLogger().info("TemplatePlugin enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("TemplatePlugin disabled");
    }
}
