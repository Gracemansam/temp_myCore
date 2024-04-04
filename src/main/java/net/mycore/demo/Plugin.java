package net.mycore.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Plugin {
    private Long id;
    private String name;
    private String description;
    private String version;
    private PluginInterface pluginInstance;

}
