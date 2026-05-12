package org.example.plugin;

public interface Plugin {
    String getName();

    void execute(PluginContext context);
}
