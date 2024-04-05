package net.mycore.demo;

import net.mycore.demo.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
@RestController
public class PluginController {
    @Autowired
    private PluginManager pluginManager;





    @PostMapping("/api/plugins/install")
    public ResponseEntity<PluginInstallResult> installPlugin(@RequestParam("file") MultipartFile file) {
        PluginInstallResult result = pluginManager.installPlugin(file);
        if (result.isSuccess()) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}