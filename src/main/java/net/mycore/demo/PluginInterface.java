package net.mycore.demo;

import org.springframework.context.ApplicationContext;

public interface PluginInterface {



    void start();

    void stop();
    String getId();

    ApplicationContext getApplicationContext();
}
