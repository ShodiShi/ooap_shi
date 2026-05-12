package org.example.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PluginManager {
    private final List<Plugin> plugins = new ArrayList<>();

    public void registerPlugin(Plugin plugin) {
        plugins.add(plugin);
    }

    public List<Plugin> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }

    public void executePlugin(String name, PluginContext context) {
        for (Plugin plugin : plugins) {
            if (plugin.getName().equals(name)) {
                plugin.execute(context);
                return;
            }
        }

        throw new IllegalArgumentException("Plugin not found: " + name);
    }
}
