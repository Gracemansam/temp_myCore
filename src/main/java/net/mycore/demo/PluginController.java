package net.mycore.demo;

import net.mycore.demo.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
@RestController
public class PluginController {
    @Autowired
    private PluginManager pluginManager;



    @PostMapping("/api/plugins/install")
    public void installPlugin(@RequestParam("file") MultipartFile file) {
        pluginManager.installPlugin(file);
    }

}