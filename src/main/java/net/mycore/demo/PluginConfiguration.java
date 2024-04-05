package net.mycore.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class PluginConfiguration {

    @Bean
    public Map<String, PluginInterface> plugins(List<PluginInterface> pluginList) {
        Map<String, PluginInterface> pluginMap = new HashMap<>();
        for (PluginInterface plugin : pluginList) {
            pluginMap.put(plugin.getClass().getSimpleName(), plugin);
        }
        return pluginMap;
    }
}