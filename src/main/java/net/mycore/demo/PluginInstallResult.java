package net.mycore.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.mycore.demo.PluginInterface;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginInstallResult {
    private boolean success;
    private String message;
    private PluginInterface pluginInstance;

    // Constructor, getters and setters omitted for brevity
}