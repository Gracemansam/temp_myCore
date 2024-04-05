package net.mycore.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;

@SpringBootApplication
public class MyCoreApplication {

    private final Map<String, PluginInterface> plugins;

    public MyCoreApplication(Map<String, PluginInterface> plugins) {
        this.plugins = plugins;
    }

    public void printPluginEndpoints(String pluginId) {
        PluginInterface plugin = plugins.get(pluginId);
        if (plugin == null) {
            throw new IllegalArgumentException("No plugin found with ID: " + pluginId);
        }

        RequestMappingHandlerMapping handlerMapping = plugin.getApplicationContext().getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, ?> handlerMethods = handlerMapping.getHandlerMethods();

        for (RequestMappingInfo requestMappingInfo : handlerMethods.keySet()) {
            System.out.println(requestMappingInfo.getPatternsCondition().getPatterns());
        }
    }



    public static void main(String[] args) {
        SpringApplication.run(MyCoreApplication.class, args);
    }



}
